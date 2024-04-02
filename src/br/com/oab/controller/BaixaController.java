package br.com.oab.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sankhya.util.BigDecimalUtil;

import br.com.oab.model.LinkBaixaModel;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.financeiro.helper.BaixaHelper;
import br.com.sankhya.modelcore.financeiro.util.DadosBaixa;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class BaixaController {

	public void buscaFinanceiroEBaixa(final BigDecimal usuarioLogado) throws Exception {

		JdbcWrapper jdbc = null;

		try {

			System.out.println("*******************************************************");
			System.out.println("Entrou na rotina : buscaFinanceiroEBaixa()");
			System.out.println("*******************************************************");

			final EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			for (final LinkBaixaModel registro : retornaRenegociacoes()) {

				FinderWrapper findTitABaixar;

				final boolean existeBaixa = verificaExisteRenegociacao(registro.getNureneg(), registro.getNufin(),
						jdbc);

				System.out.println("Existe ou nao título para o registro " + registro.getNufin() + "\n Renegociação : "
						+ registro.getNureneg() + "\n onde o resultado foi : " + existeBaixa);

				if (registro.getNureneg() != null && existeBaixa == false) {
					findTitABaixar = new FinderWrapper("Financeiro",
							"this.NURENEG = ? AND this.RECDESP = ? AND this.DHBAIXA is null",
							new Object[] { registro.getNureneg(), BigDecimalUtil.valueOf(1) });
				} else {
					findTitABaixar = new FinderWrapper("Financeiro",
							"this.NUFIN = ? AND this.RECDESP = ?  AND this.DHBAIXA is null",
							new Object[] { registro.getNufin(), BigDecimalUtil.valueOf(1) });
				}

				Collection<DynamicVO> financeiros = dwfEntityFacade.findByDynamicFinderAsVO(findTitABaixar);

				/*
				 * Alterado dia 24/01/2024, isso foi feito desta forma pois a Zydon quando gera
				 * o pagamento do cartão nao envia todos os NUFINS existentes no pagamento ela
				 * envia somente um, assim não tem como identificar quando o título é
				 * renegociado qual(is) título(s) estão sendo pagos, exemplo, se o usuario
				 * parcelar em 10 x na tabela AD_GETNETPAYMENTLINK é enviado um unico Nro único
				 * e para baixar todos os títulos oriundos da negociacao pegamos o campo NURENEG
				 * 
				 * A alteração ocorreu pq um advogado parcelou no boleto e pagou uma prestacao
				 * no cartao de crédito logo a Zydon enviou para a tabela AD_GETNETPAYMENTLINK o
				 * NURENEG, como nao estamos separando por tipo de título (Zydon nao altera o
				 * tipo de título) a rotina baixou todos os titulos pois o NURENEG está
				 * preenchido alteramos para verificar se o numero de parcelas equivale a
				 * quantidade de títulos, caso for menor ele irá pegar somente o nufin e baixar
				 * somente ele
				 */

				final int[] qtdTitulosABaixar = { 0 };

				final BigDecimal[] vlrTotalBaixado = { new BigDecimal(0) };

				final BigDecimal[] vlrValidaTitulos = { new BigDecimal(0) };

				for (final DynamicVO finVO : financeiros) {

					System.out.println("Fazendo a somatória dos títulos a serem baixados!");

					vlrValidaTitulos[0] = vlrValidaTitulos[0].add(finVO.asBigDecimal("VLRDESDOB"))
							.add(finVO.asBigDecimal("VLRJURONEGOC")).add(finVO.asBigDecimal("VLRMULTANEGOC"))
							.add(finVO.asBigDecimal("VLRVENDOR"));

				}

				System.out.println("Valor total dos títulos encontrados : " + vlrValidaTitulos[0]);

				for (final DynamicVO finVO : financeiros) {

					JapeSession.SessionHandle hnd = null;
					hnd = JapeSession.open();
					hnd.setCanTimeout(false);

					try {

						hnd.execWithTX(new JapeSession.TXBlock() {
							public void doWithTx() throws Exception {

								JdbcWrapper jdbctrx = dwfEntityFacade.getJdbcWrapper();
								jdbctrx.openSession();

								qtdTitulosABaixar[0]++;

								final NativeSql nativeSqlResultCartao = new NativeSql(jdbctrx);

								// nativeSql.setNamedParameter("LINKID", registro.getIdLink());

								System.out.println("*********************");
								System.out.println("* Vai entrar na query nativeSql =  Busca dados do TEF-Registro : "
										+ registro.getIdLink());
								System.out.println("*********************");

								nativeSqlResultCartao.appendSql("SELECT TOP 1 D.TRN_BRAND            AS BANDEIRA,\r\n"
										+ "		             D.TRN_TERMINALNSU      AS NUSU,\r\n"
										+ "		             D.TRN_ACQTRANSID       AS AUTORIZACAO,\r\n"
										+ "		             D.TRN_TRANSID          AS NUMDOC,\r\n"
										+ "		             ISNULL(D.PMT_VALOR, 0) AS VALOR,\r\n"
										+ "		             D.TRN_DHRECEB          AS DATARECEBIMENTO,\r\n"
										+ "		             gg.MAXPARCELAS         as PARCELAS\r\n"
										+ "		from AD_GTNPMTORD d\r\n"
										+ "		         inner join AD_GTNLINK gg on gg.LINKID = d.LINKID\r\n"
										+ "		WHERE D.LINKID = '" + registro.getIdLink() + "' \r\n"
										+ "		  AND D.PMT_STATUS = 'SUCCESSFUL'\r\n"
										+ "		  AND D.TRN_STATUS = 'APPROVED'");

								final ResultSet resultSetCartao = nativeSqlResultCartao.executeQuery();

								System.out.println("*********************");
								System.out.println("* TEF-Registro toString()" + resultSetCartao.toString());
								System.out.println("*********************");

								if (resultSetCartao.next()) {

									final Timestamp dataBaixa = resultSetCartao.getTimestamp("DATARECEBIMENTO");

									BigDecimal vlrBaixa = BigDecimalUtil.ZERO_VALUE;

									BigDecimal parcelas = resultSetCartao.getBigDecimal("PARCELAS");

									BigDecimal valorPagtoCartao = resultSetCartao.getBigDecimal("VALOR");

									if (parcelas.compareTo(BigDecimal.ONE) <= 0
											&& vlrValidaTitulos[0].equals(valorPagtoCartao)) {
										vlrBaixa = resultSetCartao.getBigDecimal("VALOR");

										vlrTotalBaixado[0] = vlrBaixa;
									} else {
										vlrBaixa = finVO.asBigDecimal("VLRDESDOB")
												.add(finVO.asBigDecimal("VLRJURONEGOC"))
												.add(finVO.asBigDecimal("VLRMULTANEGOC"))
												.add(finVO.asBigDecimal("VLRVENDOR"));

										vlrTotalBaixado[0] = vlrTotalBaixado[0].add(vlrBaixa);
									}

									System.out.println(
											"**************************************************************************************");
									System.out.println("*Valor Total da Transação GETNET: "
											+ resultSetCartao.getBigDecimal("VALOR"));
									System.out.println("*Quantidade Total De Parcelas: " + parcelas);
									System.out.println("*Vai ser enviado para baixa : " + vlrBaixa.toString());
									System.out.println("Baixado até o momento  : " + vlrTotalBaixado[0].toString()
											+ "\n Parcela :" + qtdTitulosABaixar[0] + "\n Total a baixar : "
											+ vlrValidaTitulos[0]);

									System.out.println(
											"***************************************************************************************");

									Boolean baixou = false;

									if (qtdTitulosABaixar[0] <= parcelas.intValue()
											|| !vlrTotalBaixado[0].equals(vlrTotalBaixado[0])) {

										baixou = baixarFinanceiro(dataBaixa, finVO, usuarioLogado, vlrBaixa, parcelas);

									}

									System.out.println("*********************");
									System.out.println(
											"* Achou um resultado dentro de  nativeSql =  Busca dados do TEF-Registro : "
													+ registro.getIdLink());
									System.out.println("*********************");

									if (baixou) {

										geraTef(finVO.asBigDecimal("NUFIN"), resultSetCartao.getString("NUSU"),
												resultSetCartao.getString("AUTORIZACAO"),
												resultSetCartao.getString("BANDEIRA"), finVO.asBigDecimal("VLRDESDOB"),
												finVO.asTimestamp("DTNEG"), "GETNET",
												resultSetCartao.getString("NUMDOC"));

										/*******************
										 * Update Linha Registro com a data que foi baixado
										 *******************/
										NativeSql sqlUpdate = new NativeSql(jdbctrx);
										sqlUpdate.appendSql(" UPDATE AD_GTNLINK ");
										sqlUpdate.appendSql(" SET DHBAIXA = :DHBAIXA");
										sqlUpdate.appendSql(" WHERE LINKID = :LINKID ");
										sqlUpdate.setNamedParameter("DHBAIXA", dataBaixa);
										sqlUpdate.setNamedParameter("LINKID", registro.getIdLink());
										sqlUpdate.executeUpdate();

										/*******************
										 * Update LinkID Financeiro
										 *******************/
										NativeSql sqlUpdateFin = new NativeSql(jdbctrx);
										sqlUpdateFin.appendSql(" UPDATE TGFFIN ");
										sqlUpdateFin.appendSql(" SET AD_LINKID = :LINKID");
										sqlUpdateFin.appendSql(" WHERE NUFIN = :NUFIN ");
										sqlUpdateFin.setNamedParameter("NUFIN", finVO.asBigDecimal("NUFIN"));
										sqlUpdateFin.setNamedParameter("LINKID", registro.getIdLink());
										sqlUpdateFin.executeUpdate();

									} else {

										System.out.println("*********************");
										System.out.println(
												"* Financeiro não baixado na rotina : baixarFinanceiro(),  Nro Único"
														+ finVO.asBigDecimal("NUFIN"));
										System.out.println("*********************");

									}

								}

								JdbcWrapper.closeSession(jdbctrx);

							} // tx

						});

					} catch (Exception extrx) {
						extrx.printStackTrace();
						System.out.println("Erro transação baixa títulos cartão Getnet: " + extrx.getMessage());
					}

				}

			}

		} finally {

			JdbcWrapper.closeSession(jdbc);

		}

	}

	private boolean verificaExisteRenegociacao(BigDecimal nureneg, BigDecimal nufin, JdbcWrapper jdbc)
			throws Exception {

		boolean retornoExisteMovimento = false;

		try {

			final NativeSql nativeSqlResult = new NativeSql(jdbc);

			// nativeSql.setNamedParameter("LINKID", registro.getIdLink());

			System.out.println("*********************");
			System.out.println("* Vai entrar na query nativeSql =  verificaExisteRenegociacao()");
			System.out.println(
					"* Verifica se existe título já baixado para o NUFIN : " + nufin + " Renegociacao : " + nureneg);
			System.out.println("*********************");

			nativeSqlResult.appendSql("select count(1) CONTADOR\r\n" + "from tgffin f\r\n"
					+ "where f.NURENEG = :NURENEG AND f.NUFIN != :NUFIN\r\n" + "and f.DHBAIXA is not null");

			nativeSqlResult.setNamedParameter("NURENEG", nureneg);
			nativeSqlResult.setNamedParameter("NUFIN", nufin);

			final ResultSet resultExiste = nativeSqlResult.executeQuery();

			if (resultExiste.next()) {

				System.out.println(
						"Quantidade de registros encontrados no metodo : " + resultExiste.getBigDecimal("CONTADOR"));

				if (resultExiste.getBigDecimal("CONTADOR").compareTo(BigDecimalUtil.ZERO_VALUE) > 0) {

					retornoExisteMovimento = true;
				}

			}

			return retornoExisteMovimento;

		} catch (Exception e) {
			// MGEModelException.throwMe(e);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção baixarFinanceiro: " + e.getMessage() + sw.toString());
			System.out.println("baixarFinanceiro - " + e.getMessage() + sw.toString());

		}

		return retornoExisteMovimento;

	}

	private boolean baixarFinanceiro(final Timestamp dhBaixa, final DynamicVO finVO, BigDecimal usuarioLogado,
			BigDecimal vlrBaixa, BigDecimal totalParcelas) throws Exception {
		/* LANÇAMENTO - 1 = CREDITO, 2 = DEBITO */

		final boolean[] baixadoRet = { false };

		try {

			System.out.println("* Entrou para baixar em baixarFinanceiro() *");

			if (usuarioLogado.compareTo(new BigDecimal(0)) == 0) {
				// Usuário criado para baixa GetNet
				usuarioLogado = new BigDecimal(225);
			}
			BaixaHelper baixaHelper = new BaixaHelper(finVO.asBigDecimal("NUFIN"), usuarioLogado);
			baixaHelper.setImprimirComprovanteCartao(false);

			DadosBaixa dadosBaixa = new DadosBaixa(dhBaixa);

			dadosBaixa = baixaHelper.montaDadosBaixa(dhBaixa, false);

			// Busca info da conta a ser baixa

			final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
			final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

			final NativeSql nativeSql = new NativeSql(jdbcWrapper);

			nativeSql.appendSql("select d.CODCTABCOINTDEB,\r\n" 
			        + "       C1.CODBCO    AS      CODBCOODEB,\r\n"
					+ "       d.CODCTABCOINTCRED,\r\n" 
			        + "       C2.CODBCO    AS      CODBCOODEB \r\n"
					+ "  From AD_GTNPAR d \r\n" 
			        + " INNER JOIN TSICTA C1 \r\n"
					+ "    ON C1.CODCTABCOINT = D.CODCTABCOINTDEB \r\n" 
			        + " INNER JOIN TSICTA C2 \r\n"
					+ "    ON C2.CODCTABCOINT = D.CODCTABCOINTCRED \r\n" 
			        + " where d.NUPAR = 1 \r\n" + "");

			final ResultSet resultSet = nativeSql.executeQuery();

			if (resultSet.next()) {

				if (finVO.asBigDecimal("CODTIPTIT").compareTo(new BigDecimal(8)) == 0) {
					dadosBaixa.getDadosBancarios().setCodConta(resultSet.getBigDecimal("CODCTABCOINTCRED"));
					dadosBaixa.getDadosBancarios().setCodBanco(resultSet.getBigDecimal("CODBCOODEB"));

				} else if (finVO.asBigDecimal("CODTIPTIT").compareTo(new BigDecimal(7)) == 0) {
					dadosBaixa.getDadosBancarios().setCodConta(resultSet.getBigDecimal("CODCTABCOINTDEB"));
					dadosBaixa.getDadosBancarios().setCodBanco(resultSet.getBigDecimal("CODBCOCRED"));

				} else {

					dadosBaixa.getDadosBancarios().setCodConta(resultSet.getBigDecimal("CODCTABCOINTCRED"));
					dadosBaixa.getDadosBancarios().setCodBanco(resultSet.getBigDecimal("CODBCOODEB"));
					dadosBaixa.getDadosBancarios().setCodTipTit(new BigDecimal(8));

				}

			}
			BigDecimal vlrDesconto = BigDecimalUtil.ZERO_VALUE;

			if (finVO.asBigDecimal("VLRDESC").compareTo(new BigDecimal(0)) == 0
					&& totalParcelas.compareTo(BigDecimal.ONE) > 0) {

				final BigDecimal vlrDesdob = finVO.asBigDecimal("VLRDESDOB");
				final BigDecimal vlrMultaNegoc = finVO.asBigDecimal("VLRMULTANEGOC");
				final BigDecimal vlrJuroNegoc = finVO.asBigDecimal("VLRJURONEGOC");
				final BigDecimal vlrCorrecao = finVO.asBigDecimal("VLRVENDOR");
				final BigDecimal vlrJuros = finVO.asBigDecimal("VLRJURO");
				final BigDecimal vlrMulta = finVO.asBigDecimal("VLRMULTA");

				vlrDesconto = vlrDesdob.add(vlrMultaNegoc).add(vlrJuroNegoc).add(vlrCorrecao).add(vlrJuros)
						.add(vlrMulta).subtract(vlrBaixa);

			} else {
				vlrDesconto = finVO.asBigDecimal("VLRDESC");

			}

			dadosBaixa.getValoresBaixa().setVlrJuros(0);
			dadosBaixa.getValoresBaixa().setVlrMulta(0);
			dadosBaixa.getDadosBancarios().setCodLancamento(BigDecimal.ONE);
			dadosBaixa.setAlteraImpostos("N");
			dadosBaixa.setAlteraMoedaBaixa(false);
			dadosBaixa.setAntBxLancBaixa(BigDecimal.ONE);
			// dadosBaixa.getDadosAdicionais().setCodTipoOperacao(finVO.asBigDecimal("CODTIPOPER"));
			dadosBaixa.getDadosAdicionais().setCodTipoOperacao(BigDecimal.valueOf(1400));

			dadosBaixa.setBaixaAutomatica(false);
			dadosBaixa.setCalculoJuro(false);
			dadosBaixa.setDataBaixa(dhBaixa);
			// dadosBaixa.setDataVencimento(finVO.asTimestamp("DTVENC"));
			// dadosBaixa.setDesdobramento(finVO.asString("DESDOBRAMENTO"));
			dadosBaixa.setImprimeRecibo(false);
			dadosBaixa.setIncluirMovimentoBancario(true);
			dadosBaixa.getValoresBaixa().setVlrDesconto(vlrDesconto.doubleValue());

			dadosBaixa.setOrigem("F");
			dadosBaixa.setRecdesp(1);
			dadosBaixa.setVlrDesdobNegociacao(finVO.asDouble("VLRDESDOB"));
			dadosBaixa.getValoresBaixa()
					.setVlrTotal(BaixaHelper.calculaValorBaixa(dadosBaixa, finVO.asDouble("VLRDESDOB"),
							dadosBaixa.getImpostos().getOutrosImpostos(), finVO.asInt("CODMOEDA")));

			System.out.println("??????????????????????????????????????????????????????????????????????????");
			System.out.println("* Dados da Baixa depois do valor da baixa atualizado*");
			System.out.println("*Baixando Nro Único : *" + finVO.asBigDecimal("NUFIN"));
			System.out.println(dadosBaixa.toString());
			System.out.println("????????????????????????????????????????????????????????????????????????????");

//			dadosBaixa.getValoresBaixa()
//					.setVlrTotal(BaixaHelper.calculaValorBaixa(dadosBaixa, finVO.asDouble("VLRDESDOB"),
//							dadosBaixa.getImpostos().getOutrosImpostos(), finVO.asInt("CODMOEDA")));

			baixaHelper.baixar(dadosBaixa);
			// RecebimentoComCartaoHelper.gravarMovimentoCaixaRecebimentoCartao(finVO);
			/* VALIDA BAIXA */
			baixadoRet[0] = JapeFactory.dao("Financeiro").findByPK(finVO.asBigDecimal("NUFIN"))
					.asTimestamp("DHBAIXA") != null;

			if (baixadoRet[0]) {
				System.out.println("*********************");
				System.out.println("* Baixou o Registro *");
				System.out.println("* numero: " + finVO.asBigDecimal("NUFIN") + "  *");
				System.out.println("*********************");

			} else {

				System.out.println("*********************");
				System.out.println("*Não  Baixou o Registro *");
				System.out.println("* numero: " + finVO.asBigDecimal("NUFIN") + "  *");
				System.out.println("*********************");

			}

		} catch (Exception e) {

			// MGEModelException.throwMe(e);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção baixarFinanceiro: " + e.getMessage() + sw.toString());
			System.out.println("baixarFinanceiro - " + e.getMessage() + sw.toString());

		}
		return baixadoRet[0];
	}

	private List<LinkBaixaModel> retornaRenegociacoes() throws Exception {

		List<LinkBaixaModel> listaBaixasExecutar = new ArrayList<LinkBaixaModel>();

		try {

			System.out.println("*********************");
			System.out.println("* Entrou para procurar os registros para serem baixados *");
			System.out.println("* em retornaRenegociacoes()  *");
			System.out.println("*********************");

			final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
			final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

			final NativeSql nativeSql = new NativeSql(jdbcWrapper);

			// 2867,2992,2662,2996, 2940, 2971

			nativeSql.appendSql("SELECT TOP 50\r\n" + "       GG.*\r\n" + "FROM (SELECT l.NURENEG,\r\n"
					+ "             l.LINKID,\r\n" + "             l.NUFIN,\r\n" + "             CASE\r\n"
					+ "                 WHEN L.NURENEG IS NOT NULL AND NOT EXISTS (SELECT 1\r\n"
					+ "                                                            FROM TGFFIN F\r\n"
					+ "                                                            WHERE F.NURENEG = L.NURENEG\r\n"
					+ "                                                              AND F.RECDESP = 1\r\n"
					+ "                                                              AND F.DHBAIXA IS NOT NULL) THEN (SELECT SUM(F.VLRDESDOB)\r\n"
					+ "                                                                                               FROM TGFFIN F\r\n"
					+ "                                                                                               WHERE F.NURENEG = L.NURENEG\r\n"
					+ "                                                                                                 AND F.RECDESP = 1)\r\n"
					+ "                 ELSE (SELECT SUM(F.VLRDESDOB) FROM TGFFIN F WHERE F.NUFIN = L.NUFIN AND F.RECDESP = 1) END AS VLRDESDOB\r\n"
					+ "      FROM AD_GETNETPAYMENTLINK l\r\n" + "      WHERE (EXISTS (SELECT 1\r\n"
					+ "                     FROM tgffin f\r\n" + "                     WHERE f.NURENEG = l.NURENEG\r\n"
					+ "                       AND f.DHBAIXA IS NULL\r\n"
					+ "                       AND f.RECDESP <> 0) OR EXISTS\r\n" + "                 (SELECT 1\r\n"
					+ "                  FROM tgffin ff\r\n" + "                  WHERE ff.NUFIN = l.NUFIN\r\n"
					+ "                    AND ff.RECDESP = 1\r\n" + "                    AND ff.DHBAIXA IS NULL))\r\n"
					+ "        AND EXISTS (SELECT 1\r\n" + "                    FROM AD_GTNPMTORD N\r\n"
					+ "                    WHERE N.LINKID = l.LINKID\r\n"
					+ "                      AND N.PMT_STATUS = 'SUCCESSFUL'\r\n"
					+ "                      AND EXISTS (SELECT 1\r\n"
					+ "                                  FROM AD_GTNLINK DDD\r\n"
					+ "                                  WHERE DDD.LINKID = L.LINKID\r\n"
					+ "                                    AND DDD.DHBAIXA IS NULL))\r\n" + "      union all\r\n"
					+ "      select FINDEST.nureneg, l.LINKID, null NUFIN, sum(FINDEST.VLRDESDOB) VLRDESDOB\r\n"
					+ "      FROM AD_GETNETPAYMENTLINK l\r\n"
					+ "               inner join tgffin finori on finori.NUFIN = l.NUFIN\r\n"
					+ "               inner join ad_rensimtit rt on finori.NUFIN = rt.NUFIN\r\n"
					+ "               inner join ad_rensim rs on rt.NURENSIM = rs.NURENSIM\r\n"
					+ "               inner join ad_rensimfin rf on rs.NURENSIM = rf.NURENSIM\r\n"
					+ "               inner join tgffin findest on rf.NUFIN = findest.NUFIN\r\n"
					+ "          and finori.NURENEG = findest.NURENEG\r\n" + "      where findest.RECDESP = 1\r\n"
					+ "        and findest.DHBAIXA is null\r\n" + "        and EXISTS (SELECT 1\r\n"
					+ "                    FROM AD_GTNPMTORD N\r\n"
					+ "                    WHERE N.LINKID = l.LINKID\r\n"
					+ "                      AND N.PMT_STATUS = 'SUCCESSFUL'\r\n"
					+ "                      AND EXISTS (SELECT 1\r\n"
					+ "                                  FROM AD_GTNLINK DDD\r\n"
					+ "                                  WHERE DDD.LINKID = L.LINKID\r\n"
					+ "                                    AND DDD.DHBAIXA IS NULL))\r\n"
					+ "      group by FINDEST.nureneg, l.LINKID) GG\r\n"
					+ " where  GG.NUFIN IS NOT NULL");

			// where LINKID = 'IBUc0j8fC

			final ResultSet resultSet = nativeSql.executeQuery();
			while (resultSet.next()) {

				LinkBaixaModel listaBaixa = new LinkBaixaModel();

				System.out.println("*********************");
				System.out.println("* Achou o Registro de Nro . *" + resultSet.getString("LINKID"));
				System.out.println("* em retornaRenegociacoes()  *");
				System.out.println("*********************");

				listaBaixa.setNureneg(resultSet.getBigDecimal("NURENEG"));
				listaBaixa.setIdLink(resultSet.getString("LINKID"));
				listaBaixa.setNufin(resultSet.getBigDecimal("NUFIN"));

				listaBaixasExecutar.add(listaBaixa);

			}

		} catch (Exception e) {
			// MGEModelException.throwMe(e);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção retornaRenegociacoes(): " + e.getMessage() + sw.toString());
			System.out.println("Erro : " + e.getMessage() + sw.toString());
		}

		return listaBaixasExecutar;

	}

	private void geraTef(final BigDecimal nufin, final String numUsu, final String autorizacao, final String bandeira,
			final BigDecimal vlrTansacao, final Timestamp dtTransacao, final String nomeRede, final String numdoc)
			throws Exception {

		System.out.println("*********************");
		System.out.println("* Gerando Info Cartão *");
		System.out.println("* Nro único : " + nufin.toString());
		System.out.println("*********************");

		try {

			EntityFacade dwTef = EntityFacadeFactory.getDWFFacade();

			DynamicVO tefVo = (DynamicVO) dwTef.getDefaultValueObjectInstance("TEF");

			tefVo.setProperty("NUFIN", nufin);
			tefVo.setProperty("BANDEIRA", bandeira);
			tefVo.setProperty("NUMNSU", numUsu);
			tefVo.setProperty("VLRTRANSACAO", vlrTansacao);
			tefVo.setProperty("NOMEREDE", nomeRede);
			tefVo.setProperty("DTTRANSACAO", dtTransacao);
			tefVo.setProperty("NUMDOC", numdoc);
			tefVo.setProperty("AUTORIZACAO", autorizacao);
			tefVo.setProperty("REDE", new BigDecimal(4));
			tefVo.setProperty("VLRTAXA", BigDecimalUtil.ZERO_VALUE);
			tefVo.setProperty("DESDOBRAMENTO", vlrTansacao);

			dwTef.createEntity("TEF", (EntityVO) tefVo);

		} catch (Exception e) {
			// MGEModelException.throwMe(e);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção ao Gerar TEF: " + e.getMessage() + sw.toString());
			System.out.println("baixarFinanceiro - " + e.getMessage() + sw.toString());
		}

	}
}