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
import com.sankhya.util.TimeUtils;

import br.com.oab.model.LinkBaixaModel;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.financeiro.helper.BaixaHelper;
import br.com.sankhya.modelcore.financeiro.util.DadosBaixa;
import br.com.sankhya.modelcore.financeiro.util.RecebimentoComCartaoHelper;
import br.com.sankhya.modelcore.helper.CaixaHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;

public class BaixaController {

	public void buscaFinanceiroEBaixa() throws Exception {

		JdbcWrapper jdbc = null;

		try {

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			final JdbcWrapper jdbcWrapper = dwfEntityFacade.getJdbcWrapper();

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

//					final NativeSql nativeSqlDataBaixa = new NativeSql(jdbcWrapper);
//
//					BigDecimal codigoEmpresaParam = finVO.asBigDecimal("CODEMP");
//					Timestamp dataBaixa = finVO.asTimestamp("DTNEG");
//
//					nativeSqlDataBaixa.appendSql("SELECT [SANKHYA].[PROX_DIA_UTIL](:DATE, :CODEMP) AS RESULTADO");
//
//					nativeSqlDataBaixa.setNamedParameter("DATE", dataBaixa);
//					nativeSqlDataBaixa.setNamedParameter("CODEMP", codigoEmpresaParam);
//
//					final ResultSet resultSqlDataBaixa = nativeSqlDataBaixa.executeQuery();
//
//
//					if (resultSqlDataBaixa.next()) {
//
//						dataBaixa = resultSqlDataBaixa.getTimestamp("RESULTADO");
//
//						System.out.println("*********************");
//						System.out.println("*Encontrou a data da baixa entrando na query : " + dataBaixa);
//						System.out.println("*********************");
//
//					}

					final NativeSql nativeSqlResultCartao = new NativeSql(jdbcWrapper);

					// nativeSql.setNamedParameter("LINKID", registro.getIdLink());

					System.out.println("*********************");
					System.out.println(
							"* Vai entrar na query nativeSql =  Busca dados do TEF-Registro : " + registro.getIdLink());
					System.out.println("*********************");

					nativeSqlResultCartao.appendSql("SELECT TOP 1 \r\n" + "       D.TRN_BRAND       AS BANDEIRA,\r\n"
							+ "       D.TRN_TERMINALNSU AS NUSU,\r\n" + "       D.TRN_ACQTRANSID  AS AUTORIZACAO,\r\n"
							+ "       D.TRN_TRANSID     AS NUMDOC, \r\n" + "       D.PMT_VALOR     AS VALOR, \r\n"
							+ "       D.TRN_DHRECEB  AS DATARECEBIMENTO \r\n" + " FROM AD_GTNPMTORD D\r\n"
							+ " WHERE D.LINKID = '" + registro.getIdLink() + "'  \r\n"
							+ "  AND D.PMT_STATUS = 'SUCCESSFUL'\r\n" + "  AND D.TRN_STATUS = 'APPROVED'");

					final ResultSet resultSetCartao = nativeSqlResultCartao.executeQuery();

					System.out.println("*********************");
					System.out.println("* TEF-Registro toString()" + resultSetCartao.toString());
					System.out.println("*********************");

					if (resultSetCartao.next()) {

						final Timestamp dataBaixa = resultSetCartao.getTimestamp("DATARECEBIMENTO");

						Boolean baixou = baixarFinanceiro(dataBaixa, finVO);

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

	private boolean baixarFinanceiro(final Timestamp dhBaixa, final DynamicVO finVO) throws Exception {
		/* LANÇAMENTO - 1 = CREDITO, 2 = DEBITO */

		ServiceContext sc = new ServiceContext(null);
		sc.setAutentication(AuthenticationInfo.getCurrent());
		sc.makeCurrent();
		try {
			SPBeanUtils.setupContext(sc);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error 'baixarFinanceiro' \n" + e.getMessage());
		}
		JapeSession.SessionHandle hnd = null;

		final boolean[] baixadoRet = { false };

		try {

			hnd = JapeSession.open();
			hnd.setCanTimeout(false);

			hnd.execWithTX(new JapeSession.TXBlock() {

				public void doWithTx() throws Exception {

					System.out.println("*********************");
					System.out.println("* Entrou para baixar *");
					System.out.println("* o registro de     *");
					System.out.println("* numero: " + finVO.asBigDecimal("NUFIN") + " dentro de baixarFinanceiro()  *");
					System.out.println("*********************");

					BigDecimal usuarioLogado = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication())
							.getUserID();
					BaixaHelper baixaHelper = new BaixaHelper(finVO.asBigDecimal("NUFIN"), usuarioLogado);
					baixaHelper.setImprimirComprovanteCartao(false);
					Timestamp dtBaixa = new Timestamp(TimeUtils.getToday());
					DadosBaixa dadosBaixa = new DadosBaixa(dhBaixa);

//					DynamicVO caixaVO = CaixaHelper.getVOCaixaAberto(usuarioLogado);
//					if (caixaVO != null) {
//						dadosBaixa = baixaHelper.montaDadosBaixa(dtBaixa, false);
//					} else {
					// dadosBaixa = baixaHelper.montaDadosBaixa(finVO.asTimestamp("DTVENC"), false);
					// }

					dadosBaixa = baixaHelper.montaDadosBaixa(dtBaixa, false);

					// Busca info da conta a ser baixa

					final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
					final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

					final NativeSql nativeSql = new NativeSql(jdbcWrapper);

					nativeSql.appendSql("select d.CODCTABCOINTDEB,\r\n" + "       C1.CODBCO    AS      CODBCOODEB,\r\n"
							+ "       d.CODCTABCOINTCRED,\r\n" + "       C2.CODBCO    AS      CODBCOODEB \r\n"
							+ "  From AD_GTNPAR d \r\n" + " INNER JOIN TSICTA C1 \r\n"
							+ "    ON C1.CODCTABCOINT = D.CODCTABCOINTDEB \r\n" + " INNER JOIN TSICTA C2 \r\n"
							+ "    ON C2.CODCTABCOINT = D.CODCTABCOINTCRED \r\n" + " where d.NUPAR = 1 \r\n" + "");

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

					System.out.println("*********************");
					System.out.println("* Dados da Baixa - Banco etc*");
					System.out.println("* Dados antes de diminuir os juros " + dadosBaixa.toString());
					System.out.println("*********************");

					dadosBaixa.getValoresBaixa().setVlrJuros(0);
					dadosBaixa.getValoresBaixa().setVlrMulta(0);

					System.out.println("*********************");
					System.out.println("* Dados da Baixa - Banco etc*");
					System.out.println("* Dados depois de diminuir os juros " + dadosBaixa.toString());
					System.out.println("*********************");

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
					dadosBaixa.setOrigem("F");
					dadosBaixa.setRecdesp(1);
					dadosBaixa.setVlrDesdobNegociacao(finVO.asDouble("VLRDESDOB"));

					dadosBaixa.getValoresBaixa()
							.setVlrTotal(BaixaHelper.calculaValorBaixa(dadosBaixa, finVO.asDouble("VLRDESDOB"),
									dadosBaixa.getImpostos().getOutrosImpostos(), finVO.asInt("CODMOEDA")));

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

				}
			});

		} catch (Exception e) {

			// MGEModelException.throwMe(e);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção baixarFinanceiro: " + e.getMessage() + sw.toString());
			System.out.println("baixarFinanceiro - " + e.getMessage() + sw.toString());

		} finally {
			JapeSession.close(hnd);
		}
		return baixadoRet[0];
	}

	private List<LinkBaixaModel> retornaRenegociacoes() throws Exception {

		System.out.println("*********************");
		System.out.println("* Entrou para procurar os registros para serem baixados *");
		System.out.println("* em retornaRenegociacoes()  *");
		System.out.println("*********************");

		final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

		final NativeSql nativeSql = new NativeSql(jdbcWrapper);

		// 2867,2992,2662,2996, 2940, 2971

		nativeSql.appendSql("SELECT l.NURENEG, l.LINKID, l.NUFIN\r\n"
				+ "		          FROM AD_GETNETPAYMENTLINK l\r\n" + "				 WHERE (EXISTS (SELECT 1 \r\n"
				+ "		                          FROM tgffin f\r\n"
				+ "				                 WHERE f.NURENEG = l.NURENEG\r\n"
				+ "		                           AND f.DHBAIXA IS NULL\r\n"
				+ "				                   AND f.RECDESP <> 0) \r\n"
				+ "				                   OR EXISTS\r\n"
				+ "				        (SELECT 1            FROM tgffin ff \r\n"
				+ "				         WHERE ff.NUFIN = l.NUFIN\r\n"
				+ "				           AND ff.RECDESP = 1\r\n"
				+ "				           AND ff.DHBAIXA IS NULL) \r\n" + "				            ) \r\n"
				+ "				   AND EXISTS (SELECT 1\r\n" + "				          FROM AD_GTNPMTORD N \r\n"
				+ "				         WHERE N.LINKID = l.LINKID\r\n"
				+ "				           AND N.PMT_STATUS = 'SUCCESSFUL')");

		List<LinkBaixaModel> listaBaixasExecutar = new ArrayList<LinkBaixaModel>();

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

		return listaBaixasExecutar;

	}

	private void geraTef(final BigDecimal nufin, final String numUsu, final String autorizacao, final String bandeira,
			final BigDecimal vlrTansacao, final Timestamp dtTransacao, final String nomeRede, final String numdoc)
			throws Exception {

		System.out.println("*********************");
		System.out.println("* Gerando Info Cartão *");
		System.out.println("* Nro único : " + nufin.toString());
		System.out.println("*********************");
		JapeSession.SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();
			hnd.setCanTimeout(false);

			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {
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

					dwTef.createEntity("TEF", (EntityVO) tefVo);
				}
			});

		} catch (Exception e) {
			// MGEModelException.throwMe(e);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção ao Gerar TEF: " + e.getMessage() + sw.toString());
			System.out.println("baixarFinanceiro - " + e.getMessage() + sw.toString());
		} finally {
			JapeSession.close(hnd);
		}

	}

}
