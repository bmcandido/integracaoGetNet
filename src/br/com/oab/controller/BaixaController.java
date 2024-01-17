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

	public void buscaFinanceiroEBaixa(BigDecimal usuarioLogado) throws Exception {

		JdbcWrapper jdbc = null;

		try {

			System.out.println("*******************************************************");
			System.out.println("Entrou na rotina : buscaFinanceiroEBaixa()");
			System.out.println("*******************************************************");

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			for (LinkBaixaModel registro : retornaRenegociacoes()) {

				FinderWrapper findTitABaixar;

				if (registro.getNureneg() != null) {
					findTitABaixar = new FinderWrapper("Financeiro", "this.NURENEG = ? AND this.RECDESP = ?",
							new Object[] { registro.getNureneg(), BigDecimalUtil.valueOf(1) });
				} else {
					findTitABaixar = new FinderWrapper("Financeiro", "this.NUFIN = ? AND this.RECDESP = ?",
							new Object[] { registro.getNufin(), BigDecimalUtil.valueOf(1) });
				}

				Collection<DynamicVO> financeiros = dwfEntityFacade.findByDynamicFinderAsVO(findTitABaixar);

				for (DynamicVO finVO : financeiros) {

					final NativeSql nativeSqlResultCartao = new NativeSql(jdbc);

					// nativeSql.setNamedParameter("LINKID", registro.getIdLink());

					System.out.println("*********************");
					System.out.println(
							"* Vai entrar na query nativeSql =  Busca dados do TEF-Registro : " + registro.getIdLink());
					System.out.println("*********************");

					nativeSqlResultCartao.appendSql(
							"SELECT TOP 1 \r\n" 
					        + "       D.TRN_BRAND       AS BANDEIRA,\r\n"
							+ "       D.TRN_TERMINALNSU AS NUSU,\r\n" 
					        + "       D.TRN_ACQTRANSID  AS AUTORIZACAO,\r\n"
							+ "       D.TRN_TRANSID     AS NUMDOC, \r\n" 
					        + "       ISNULL(D.PMT_VALOR,0)     AS VALOR, \r\n"
							+ "       D.TRN_DHRECEB  AS DATARECEBIMENTO, \r\n" 
							+ "       isnull(D.TRN_NUMPARC,1) AS PARCELAS \r\n" 
					        + " FROM AD_GTNPMTORD D\r\n"
							+ " WHERE D.LINKID = '" + registro.getIdLink() 
							+ "'  \r\n"
							+ "  AND D.PMT_STATUS = 'SUCCESSFUL'\r\n" 
							+ "  AND D.TRN_STATUS = 'APPROVED'");

					final ResultSet resultSetCartao = nativeSqlResultCartao.executeQuery();

					System.out.println("*********************");
					System.out.println("* TEF-Registro toString()" + resultSetCartao.toString());
					System.out.println("*********************");

					if (resultSetCartao.next()) {

						final Timestamp dataBaixa = resultSetCartao.getTimestamp("DATARECEBIMENTO");
						
						 BigDecimal vlrBaixa = BigDecimalUtil.ZERO_VALUE;
						
						 BigDecimal parcelas = resultSetCartao.getBigDecimal("PARCELAS");

						 if (parcelas.compareTo(BigDecimal.ONE) <= 0) {
						     vlrBaixa = resultSetCartao.getBigDecimal("VALOR");
						 } else {
						     vlrBaixa = finVO.asBigDecimal("VLRDESDOB");
						 }
						
						System.out.println("*********************");
						System.out.println("*Vai ser enviado para baixa : " + vlrBaixa.toString());
						System.out.println("*********************");

						Boolean baixou = baixarFinanceiro(dataBaixa, finVO, usuarioLogado, vlrBaixa);

						System.out.println("*********************");
						System.out.println("* Achou um resultado dentro de  nativeSql =  Busca dados do TEF-Registro : "
								+ registro.getIdLink());
						System.out.println("*********************");

						if (baixou) {

							geraTef(finVO.asBigDecimal("NUFIN"), resultSetCartao.getString("NUSU"),
									resultSetCartao.getString("AUTORIZACAO"), resultSetCartao.getString("BANDEIRA"),
									finVO.asBigDecimal("VLRDESDOB"), finVO.asTimestamp("DTNEG"), "GETNET",
									resultSetCartao.getString("NUMDOC"));

							/*******************
							 * Update Linha Registro com a data que foi baixado
							 *******************/
							NativeSql sqlUpdate = new NativeSql(jdbc);
							sqlUpdate.appendSql(" UPDATE AD_GTNLINK ");
							sqlUpdate.appendSql(" SET DHBAIXA = :DHBAIXA");
							sqlUpdate.appendSql(" WHERE LINKID = :LINKID ");
							sqlUpdate.setNamedParameter("DHBAIXA", dataBaixa);
							sqlUpdate.setNamedParameter("LINKID", registro.getIdLink());
							sqlUpdate.executeUpdate();

							/*******************
							 * Update LinkID Financeiro
							 *******************/
							NativeSql sqlUpdateFin = new NativeSql(jdbc);
							sqlUpdateFin.appendSql(" UPDATE TGFFIN ");
							sqlUpdateFin.appendSql(" SET AD_LINKID = :LINKID");
							sqlUpdateFin.appendSql(" WHERE NUFIN = :NUFIN ");
							sqlUpdateFin.setNamedParameter("NUFIN", finVO.asBigDecimal("NUFIN"));
							sqlUpdateFin.setNamedParameter("LINKID", registro.getIdLink());
							sqlUpdateFin.executeUpdate();

						} else {

							System.out.println("*********************");
							System.out.println("* Financeiro não baixado na rotina : baixarFinanceiro(),  Nro Único"
									+ finVO.asBigDecimal("NUFIN"));
							System.out.println("*********************");

						}

					}

				}

			}

		} finally {

			JdbcWrapper.closeSession(jdbc);

		}

	}

	private boolean baixarFinanceiro(final Timestamp dhBaixa, final DynamicVO finVO, BigDecimal usuarioLogado, BigDecimal vlrBaixa)
			throws Exception {
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
			BigDecimal vlrDesconto  = BigDecimalUtil.ZERO_VALUE;
			
			if(finVO.asBigDecimal("VLRDESC").compareTo(new BigDecimal(0)) == 0) {
				
				vlrDesconto	= finVO.asBigDecimal("VLRDESDOB").subtract(vlrBaixa);
				
			} else {
				vlrDesconto = finVO.asBigDecimal("VLRDESC");
				
			}
			
		

			dadosBaixa.getValoresBaixa().setVlrJuros(0);
			dadosBaixa.getValoresBaixa().setVlrMulta(0);
			dadosBaixa.getDadosBancarios().setCodLancamento(BigDecimal.ONE);
			dadosBaixa.setAlteraImpostos("N");
			dadosBaixa.setAlteraMoedaBaixa(false);
			dadosBaixa.setAntBxLancBaixa(BigDecimal.ONE);
			dadosBaixa.setAntBxTop(finVO.asBigDecimal("CODTIPOPER"));
			dadosBaixa.setAntBxTopBaixa(BigDecimal.valueOf(1400));
			dadosBaixa.setBaixaAutomatica(false);
			dadosBaixa.setCalculoJuro(false);
			dadosBaixa.setDataBaixa(dhBaixa);
			dadosBaixa.setDataVencimento(finVO.asTimestamp("DTVENC"));
			dadosBaixa.setDesdobramento(finVO.asString("DESDOBRAMENTO"));
			dadosBaixa.setImprimeRecibo(false);
			dadosBaixa.setIncluirMovimentoBancario(true);
			dadosBaixa.getValoresBaixa().setVlrDesconto(vlrDesconto.doubleValue());
		
			dadosBaixa.setOrigem("F");
			dadosBaixa.setRecdesp(1);
			dadosBaixa.setVlrDesdobNegociacao(finVO.asDouble("VLRDESDOB"));
			 dadosBaixa.getValoresBaixa().setVlrTotal(BaixaHelper.calculaValorBaixa(dadosBaixa, finVO.asDouble("VLRDESDOB"), 
			    		dadosBaixa.getImpostos().getOutrosImpostos(), finVO.asInt("CODMOEDA")));  
			 
			 
				System.out.println("??????????????????????????????????????????????????????????????????????????");
				System.out.println("* Dados da Baixa depois do valor da baixa atualizado*");
				System.out.println("*Baixando Nro Único : *" + finVO.asBigDecimal("NUFIN"));
				System.out.println( dadosBaixa.toString());
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

			nativeSql.appendSql("SELECT l.NURENEG, l.LINKID, l.NUFIN\r\n"
					+ "					          FROM AD_GETNETPAYMENTLINK l			 WHERE (EXISTS (SELECT 1\r\n"
					+ "					                          FROM tgffin f\r\n"
					+ "							                 WHERE f.NURENEG = l.NURENEG\r\n"
					+ "					                           AND f.DHBAIXA IS NULL\r\n" + "\r\n"
					+ "							                   AND f.RECDESP <> 0)\r\n"
					+ "							                   OR EXISTS\r\n"
					+ "							        (SELECT 1            FROM tgffin ff\r\n"
					+ "							         WHERE ff.NUFIN = l.NUFIN\r\n"
					+ "							           AND ff.RECDESP = 1\r\n"
					+ "							           AND ff.DHBAIXA IS NULL) 			            )\r\n"
					//+ " AND L.LINKID = 'RjPwm7izS'\r\n"
					+ "							   AND EXISTS (SELECT 1			          FROM AD_GTNPMTORD N\r\n"
					+ "							         WHERE N.LINKID = l.LINKID\r\n"
					+ "							           AND N.PMT_STATUS = 'SUCCESSFUL')");

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
