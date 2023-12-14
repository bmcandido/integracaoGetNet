package br.com.oab.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.financeiro.helper.BaixaHelper;
import br.com.sankhya.modelcore.financeiro.util.DadosBaixa;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;

public class BaixaControllerPorFiltro {

	public boolean buscaFinanceiroEBaixaPorFiltro(BigDecimal codigoEmpresa, String idLink) throws Exception {

		JdbcWrapper jdbc = null;

		try {

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			final JdbcWrapper jdbcWrapper = dwfEntityFacade.getJdbcWrapper();

			for (LinkBaixaModel registro : retornaLinksParaBaixas(idLink)) {

				final NativeSql nativeSqlResultCartao = new NativeSql(jdbcWrapper);

				// nativeSql.setNamedParameter("LINKID", registro.getIdLink());

				System.out.println("*********************");
				System.out.println("* Vai entrar na query nativeSql =  Busca dados do TEF-Registro : " + registro.getIdLink());
				System.out.println("*********************");

				/**********************************************************
				 * Faz a busca no registro que está com sucesso
				 *********************************************************/

				nativeSqlResultCartao.appendSql("SELECT TOP 1 \r\n" 
				        + "       D.TRN_BRAND       AS BANDEIRA,\r\n"
						+ "       D.TRN_TERMINALNSU AS NUSU,\r\n" 
				        + "       D.TRN_ACQTRANSID  AS AUTORIZACAO,\r\n"
						+ "       D.TRN_TRANSID     AS NUMDOC, \r\n" 
				        + "       D.PMT_VALOR     AS VALOR, \r\n"
						+ "       D.TRN_DHRECEB  AS DATARECEBIMENTO \r\n" 
				        + " FROM AD_GTNPMTORD D\r\n"
						+ " WHERE D.LINKID = '" + registro.getIdLink() + "'  \r\n"
						+ "  AND D.PMT_STATUS = 'SUCCESSFUL'\r\n" 
						+ "  AND D.TRN_STATUS = 'APPROVED'");

				final ResultSet resultSetCartao = nativeSqlResultCartao.executeQuery();

				System.out.println("*********************");
				System.out.println("* TEF-Registro : " + resultSetCartao.toString());
				System.out.println("*********************");

				if (resultSetCartao.next()) {

					final Timestamp dataBaixa = resultSetCartao.getTimestamp("DATARECEBIMENTO");
					final BigDecimal vlrBaixaRet = resultSetCartao.getBigDecimal("VALOR");
					final String tipoDeMovimento = registro.getDocTip();
					final String cgcCpf = registro.getCpfCgc();
					final String mesAno = new SimpleDateFormat("MM/yyyy").format(new Date(dataBaixa.getTime()));

					/**********************************************************
					 * Faz os financeiros que fazem parte do pagamento localizado
					 * 
					 *********************************************************/

					final NativeSql nativeSqlBuscaReneg = new NativeSql(jdbcWrapper);

					nativeSqlBuscaReneg.appendSql(" SELECT ISNULL(max(F.NURENEG),0) NURENEG, max(NUFIN) NUFIN ");
					nativeSqlBuscaReneg.appendSql(" FROM TGFFIN F  INNER JOIN TGFPAR P ON P.CODPARC = F.CODPARC ");
					nativeSqlBuscaReneg.appendSql(" WHERE F.RECDESP = 1  ");
					nativeSqlBuscaReneg.appendSql(" AND F.DHBAIXA IS NULL ");
					// nativeSqlBuscaReneg.appendSql(" AND F.CODTIPTIT IN (8,7) ");
					nativeSqlBuscaReneg.appendSql(" AND FORMAT(f.DTNEG , 'MM/yyyy') = :DTNEG ");
					nativeSqlBuscaReneg.appendSql(" AND F.CODEMP = :CODEMP");
					nativeSqlBuscaReneg.appendSql("  AND CASE\r\n" + "         WHEN 'CPF' = :TIPCADASTRO THEN \r\n"
							+ "          CAST(P.CGC_CPF AS VARCHAR(50)) \r\n" + "         ELSE \r\n"
							+ "          CAST(P.IDENTINSCESTAD AS VARCHAR(50))\r\n" + "       END = :CPF ");
					nativeSqlBuscaReneg.appendSql(
							" HAVING SUM(F.VLRDESDOB + F.VLRVENDOR + F.VLRMULTANEGOC + F.VLRJURONEGOC + F.VLRJURO + F.VLRMULTA - F.VLRDESC) = :VALOR");

					nativeSqlBuscaReneg.setNamedParameter("DTNEG", mesAno);
					nativeSqlBuscaReneg.setNamedParameter("VALOR", vlrBaixaRet);
					nativeSqlBuscaReneg.setNamedParameter("TIPCADASTRO", tipoDeMovimento);
					nativeSqlBuscaReneg.setNamedParameter("CPF", cgcCpf);
					nativeSqlBuscaReneg.setNamedParameter("CODEMP", codigoEmpresa);

					final ResultSet resultBuscaReneg = nativeSqlBuscaReneg.executeQuery();

					if (resultBuscaReneg.next()) {

						Collection<DynamicVO> financeiros = new ArrayList<>();
						final FinderWrapper findTitABaixar;

						if (resultBuscaReneg.getBigDecimal("NURENEG").compareTo(new BigDecimal(0)) > 0) {

							findTitABaixar = new FinderWrapper("Financeiro", "this.NURENEG = ? AND this.RECDESP = ?",
									new Object[] { resultBuscaReneg.getBigDecimal("NURENEG"),
											BigDecimalUtil.valueOf(1) });

						} else {
							findTitABaixar = new FinderWrapper("Financeiro", "this.NUFIN = ? AND this.RECDESP = ?",
									new Object[] { resultBuscaReneg.getBigDecimal("NUFIN"),
											BigDecimalUtil.valueOf(1) });

						}

						financeiros = dwfEntityFacade.findByDynamicFinderAsVO(findTitABaixar);

						for (DynamicVO finVO : financeiros) {

							Boolean baixou = baixarFinanceiro(dataBaixa, finVO);

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
								sqlUpdateFin.setNamedParameter("NUFIN", resultBuscaReneg.getBigDecimal("NUFIN"));
								sqlUpdateFin.setNamedParameter("LINKID", registro.getIdLink());
								sqlUpdateFin.executeUpdate();

								return true;

							} else {

								System.out.println("*********************");
								System.out.println("* Financeiro não baixado na rotina : baixarFinanceiro(),  Nro Único"
										+ finVO.asBigDecimal("NUFIN"));
								System.out.println("*********************");

								return false;

							}

						}

					}

				}

			}

		} finally {

			JdbcWrapper.closeSession(jdbc);

		}
		return false;

	}

	private List<LinkBaixaModel> retornaLinksParaBaixas(String idLink) throws Exception {

		System.out.println("*********************");
		System.out.println("* Entrou para procurar os registros para serem baixados : " + idLink);
		System.out.println("* em retornaTitulosPorConsulta()   *");
		System.out.println("*********************");

		final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

		final NativeSql nativeSql = new NativeSql(jdbcWrapper);

		nativeSql.appendSql("SELECT l.LINKID, L.DOCNUMBER, L.DOCTYPE, l.NOMECOMPLETO NOME, L.EMAIL\r\n"
				+ "  FROM AD_GTNLINK l\r\n" 
				+ " INNER JOIN AD_GTNPMTORD N\r\n" 
				+ "    ON N.LINKID = L.LINKID\r\n"
				+ " WHERE l.DHBAIXA IS NULL\r\n" 
				+ " AND l.LINKID = '" + idLink + "'  \r\n"
				+ "   AND l.ORIGEM = 'CF'\r\n" + "   AND N.PMT_STATUS = 'SUCCESSFUL'");

		List<LinkBaixaModel> listaBaixasExecutar = new ArrayList<LinkBaixaModel>();

		final ResultSet resultSet = nativeSql.executeQuery();
		while (resultSet.next()) {

			System.out.println("*********************");
			System.out.println("* Achou o Registro de Nro . *" + resultSet.getString("LINKID"));
			System.out.println("* em retornaTitulosPorConsulta()  *");
			System.out.println("*********************");

			LinkBaixaModel listaBaixa = new LinkBaixaModel();

			listaBaixa.setIdLink(resultSet.getString("LINKID"));
			listaBaixa.setCpfCgc(resultSet.getString("DOCNUMBER"));
			listaBaixa.setDocTip(resultSet.getString("DOCTYPE"));
			listaBaixa.setName(resultSet.getString("NOME"));

			listaBaixasExecutar.add(listaBaixa);

		}

		return listaBaixasExecutar;

	}

	private boolean baixarFinanceiro(Timestamp dhBaixa, DynamicVO finVO) throws Exception {
		/* LANÇAMENTO - 1 = CREDITO, 2 = DEBITO */
		boolean baixado = false;
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
		try {

			System.out.println("*********************");
			System.out.println("* Entrou para baixar *");
			System.out.println("* o registro de     *");
			System.out.println("* numero: " + finVO.asBigDecimal("NUFIN") + " dentro de baixarFinanceiro()  *");
			System.out.println("*********************");

			BigDecimal usuarioLogado = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication())
					.getUserID();
			BaixaHelper baixaHelper = new BaixaHelper(finVO.asBigDecimal("NUFIN"), usuarioLogado);
			baixaHelper.setImprimirComprovanteCartao(false);

			DadosBaixa dadosBaixa = new DadosBaixa(dhBaixa);

			dadosBaixa = baixaHelper.montaDadosBaixa(dhBaixa, false);

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
			baixado = JapeFactory.dao("Financeiro").findByPK(finVO.asBigDecimal("NUFIN"))
					.asTimestamp("DHBAIXA") != null;

			if (baixado) {
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

			return baixado;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção baixarFinanceiro: " + e.getMessage() + sw.toString());
			System.out.println("baixarFinanceiro - " + e.getMessage() + sw.toString());

		} finally {
			JapeSession.close(hnd);
		}
		return baixado;
	}

	private void geraTef(BigDecimal nufin, String numUsu, String autorizacao, String bandeira, BigDecimal vlrTansacao,
			Timestamp dtTransacao, String nomeRede, String numdoc) throws Exception {

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

			dwTef.createEntity("TEF", (EntityVO) tefVo);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error 'Nao gerou dados do cartão' \n" + e.getMessage());
		}

	}

}
