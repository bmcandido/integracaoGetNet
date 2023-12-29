package br.com.oab.dao;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.Collection;

import com.google.gson.JsonObject;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import br.com.oab.controller.RenegociacaoFinanceiroController;
import br.com.oab.model.RenegociacaoModel;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class GeraRateioDAO {

	public Boolean calculaRateioDAO(JdbcWrapper jdbc, BigDecimal nureneg, int parametroBuscaReneg) throws Exception {

		Boolean retornaSimNao = false;
		int SCALE = 2;

		try {

			System.out.println("Entrou na condição e metodo calculaRateioDAO() para a renegociacao " + nureneg);

			BigDecimal vlrTitulo = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrRateioJuros = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrRateioMulta = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrRateioCorrecao = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrRateioJurosRecDespZero = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrRateioMultaRecDespZero = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrRateioCorrecaoRecDespZero = BigDecimalUtil.ZERO_VALUE;

			for (BigDecimal renegociacaoOrigem : RenegociacaoFinanceiroController.obtemOrigensRenegociacaoOrdenada(jdbc,
					nureneg, parametroBuscaReneg)) {

				// Soma os valores de Juros multa e Correção

				RenegociacaoModel resultSetRecDespZero = RenegociacaoFinanceiroController
						.obtemOrigensRenegociacaoRECDESPZero(jdbc, renegociacaoOrigem);

				vlrRateioJurosRecDespZero = BigDecimalUtil.getValueOrZero(resultSetRecDespZero.getVlrJurosNegoc());
				vlrRateioMultaRecDespZero = BigDecimalUtil.getValueOrZero(resultSetRecDespZero.getVlrMultaNegoc());
				vlrRateioCorrecaoRecDespZero = BigDecimalUtil.getValueOrZero(resultSetRecDespZero.getVlrCorrecao());

				// Quantidade de Parcelas
				BigDecimal qtdParcelasRecDespUm = RenegociacaoFinanceiroController.obtemQuantidadeDeParcelasReneg(jdbc,
						nureneg);

				System.out.println("*****************************************");
				System.out.println("RECDESP = 0 ");
				System.out.println("Juros RecDesp 0 : " + vlrRateioJurosRecDespZero);
				System.out.println("Multa RecDesp 0 : " + vlrRateioMultaRecDespZero);
				System.out.println("Correção RecDesp 0 : " + vlrRateioCorrecaoRecDespZero);
				System.out.println("*****************************************");

				// Busca as Origens na TGFFIN da RECDESP = 1
				Collection<RenegociacaoModel> resultSetRecDespUm = RenegociacaoFinanceiroController
						.obtemOrigensRenegociacao(jdbc, renegociacaoOrigem, new BigDecimal(1));

				for (RenegociacaoModel resultRecDesp1 : resultSetRecDespUm) {

					// Calculando os juros
//					vlrRateioJuros = BigDecimalUtil.getRounded(vlrRateioJuros.add(
//							vlrRateioJurosRecDespZero.divide(qtdParcelasRecDespUm, SCALE, RoundingMode.HALF_UP)), 2);
//					
					vlrRateioJuros = vlrRateioJuros.add(vlrRateioJurosRecDespZero);
					// Calculando a multa
					vlrRateioMulta = vlrRateioMulta.add(vlrRateioMultaRecDespZero);

					vlrRateioCorrecao = vlrRateioCorrecao.add(vlrRateioCorrecaoRecDespZero);

					vlrTitulo = BigDecimalUtil.getRounded(vlrTitulo.add(resultRecDesp1.getVlrDesdob())
							.subtract(vlrRateioJuros).subtract(vlrRateioMulta).subtract(vlrRateioCorrecao), 2);

					// Faz o Insert no rateio do RecDesp = 1;

					System.out.println("*****************************************");
					System.out.println("RECDESP = 1");
					System.out.println("*****************************************");
					System.out.println("NUFIN : " + resultRecDesp1.getNufin());
					System.out.println("Quantidade Parcelas : " + qtdParcelasRecDespUm);
					System.out.println("Juros RecDesp 1 : " + vlrRateioJuros);
					System.out.println("Multa RecDesp 1 : " + vlrRateioMulta);
					System.out.println("Correção RecDesp 1 : " + vlrRateioCorrecao);
					System.out.println("Vlr Natureza RecDesp 1 : " + vlrTitulo);
					System.out.println("Vlr Desdobramento RecDesp 1 : " + resultRecDesp1.getVlrDesdob());
					System.out.println("*****************************************");

					GeraRateioDAO geraRateioDAO = new GeraRateioDAO();

					geraRateioDAO.geraRateio(jdbc, vlrTitulo, vlrRateioJuros, vlrRateioMulta, vlrRateioCorrecao,
							resultRecDesp1, nureneg, qtdParcelasRecDespUm);

					vlrTitulo = BigDecimalUtil.ZERO_VALUE;
					vlrRateioMulta = BigDecimalUtil.ZERO_VALUE;
					vlrRateioJuros = BigDecimalUtil.ZERO_VALUE;
					vlrRateioCorrecao = BigDecimalUtil.ZERO_VALUE;

				}

				vlrRateioCorrecaoRecDespZero = BigDecimalUtil.ZERO_VALUE;
				vlrRateioJurosRecDespZero = BigDecimalUtil.ZERO_VALUE;
				vlrRateioMultaRecDespZero = BigDecimalUtil.ZERO_VALUE;

				retornaSimNao = true;

			}

		} catch (Exception e) {
			MGEModelException.throwMe(e);

			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Erro ao executar o rateio calculaRateioDAO()");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro  : " + e.getMessage() + sw.toString());
			System.out.println("erro - " + e.getMessage() + sw.toString());
		}

		return retornaSimNao;

	}

	public void geraRateio(JdbcWrapper jdbc, BigDecimal vlrTitulo, BigDecimal juros, BigDecimal multa,
			BigDecimal correcao, RenegociacaoModel resultRateio, BigDecimal nureneg,
			BigDecimal quantidadeDeTitulosRenegociados) throws Exception {

		System.out.println("Executando o rateio dos itens");

		int SCALE = 2;

		try {

			int linhas = 0;

			// Deleta o rateio existente

			removeRateioExistente(resultRateio.getNufin(), "RateioRecDesp");

			// Retorna numero de linhas

			int rowCount = retornaNumeLinhasParaRateio(jdbc, nureneg);

			System.out.println("Quantidade de Linhas a ratear : " + rowCount);

			// Executa os dados para o rateio

			NativeSql sqlReturn = new NativeSql(jdbc);
			sqlReturn.appendSql("SELECT SUM(VLRDESDOB) AS VLRDESDOB, GG.CODNAT, GG.CAMPO, GG.TIPO\r\n"
					+ "FROM (SELECT SUM(VLRDESDOB)                                   AS VLRDESDOB,\r\n"
					+ "             CASE WHEN CODNAT = 0 THEN 101010 ELSE CODNAT END AS CODNAT,\r\n"
					+ "             CAMPO,\r\n"
					+ "             CASE\r\n"
					+ "                 WHEN CODNAT IN (SELECT CODNAT\r\n"
					+ "                                 from AD_PARRENEGRATEIO d\r\n"
					+ "                                 where d.NUPAR = 1) THEN 'RP'\r\n"
					+ "                 ELSE TIPO END                                   TIPO\r\n"
					+ "      FROM (SELECT ISNULL((select CAMPO from AD_PARRENEGRATEIO d where d.NUPAR = 1 AND D.CODNAT = V.CODNAT),\r\n"
					+ "                          'VLRDESDOB') CAMPO,\r\n"
					+ "                   V.CODNAT,\r\n"
					+ "                   V.VLRDESDOB,\r\n"
					+ "                   'RA'                TIPO\r\n"
					+ "            FROM VGFFINRAT_OAB V\r\n"
					+ "            WHERE V.NURENEG = :NURENEG\r\n"
					+ "              AND V.RECDESP = 0\r\n"
					+ "            --AND NOT EXISTS (select 1 from AD_PARRENEGRATEIO d where d.NUPAR = 1 AND D.CODNAT = V.CODNAT)\r\n"
					+ "            UNION ALL\r\n"
					+ "            select CAMPO, CODNAT, 0, 'RP' TIPO\r\n"
					+ "            from AD_PARRENEGRATEIO d\r\n"
					+ "            where d.NUPAR = 1) VV\r\n"
					+ "      GROUP BY CODNAT, CAMPO, TIPO) GG\r\n"
					+ "GROUP BY GG.CODNAT, GG.CAMPO, GG.TIPO\r\n"
					+ "ORDER BY TIPO, 1");

			sqlReturn.setNamedParameter("NURENEG", nureneg);

			ResultSet resultSet = sqlReturn.executeQuery();

			BigDecimal percAcumulado = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrRateio = BigDecimalUtil.ZERO_VALUE;
			BigDecimal codnat = BigDecimalUtil.ZERO_VALUE;
			BigDecimal vlrOutrosRateios = BigDecimalUtil.ZERO_VALUE;

			// P = PARAMETRO
			// R = RATEIO

			while (resultSet.next()) {
				
				linhas++;

				System.out.println("Entrou na condição pra fazer o rateio");

				if ("JUROS".equals(resultSet.getString(("CAMPO"))) && "RP".equals(resultSet.getString(("TIPO")))) {
					vlrRateio = juros;

				} else if ("MULTA".equals(resultSet.getString(("CAMPO")))
						&& "RP".equals(resultSet.getString(("TIPO")))) {

					vlrRateio = multa;

				} else if ("CORRECAO".equals(resultSet.getString(("CAMPO")))
						&& "RP".equals(resultSet.getString(("TIPO")))) {

					vlrRateio = correcao;

				} else if ("VLRDESDOB".equals(resultSet.getString(("CAMPO")))
						&& "RA".equals(resultSet.getString(("TIPO")))) {

					vlrOutrosRateios = vlrOutrosRateios.add(resultSet.getBigDecimal("VLRDESDOB"));

					vlrOutrosRateios = BigDecimalUtil.getRounded(
							vlrOutrosRateios.divide(quantidadeDeTitulosRenegociados, SCALE, RoundingMode.HALF_UP), 2);

					vlrRateio = vlrOutrosRateios;
				} else if ("VLRDESDOB".equals(resultSet.getString(("CAMPO")))
						&& "RP".equals(resultSet.getString(("TIPO")))) {

					if (resultSet.getBigDecimal("VLRDESDOB").compareTo(new BigDecimal(0)) > 0) {

						vlrRateio = BigDecimalUtil.getRounded(resultSet.getBigDecimal("VLRDESDOB")
								.divide(quantidadeDeTitulosRenegociados, SCALE, RoundingMode.HALF_UP), 2);

					} else {

						vlrRateio = vlrTitulo.subtract(vlrOutrosRateios);

					}

				}

				if (resultSet.getBigDecimal("CODNAT").compareTo(BigDecimal.ZERO) == 0) {
					codnat = resultRateio.getCodnat();
				} else {
					codnat = resultSet.getBigDecimal("CODNAT");
				}

				if (vlrRateio.compareTo(BigDecimal.ZERO) > 0) {

				

					BigDecimal percentual = BigDecimalUtil.ZERO_VALUE;
					
					
					System.out.println("Linhas do While : " + linhas);
					System.out.println("Linhas Total : " + rowCount);

					if (linhas == rowCount) {

						percentual = BigDecimalUtil.CEM_VALUE.subtract(percAcumulado);

						System.out.println("Ultima Linha, percentual rateado : " + percentual);

					} else {

						percentual = BigDecimalUtil.getRounded(vlrRateio.multiply(BigDecimalUtil.CEM_VALUE)
								.divide(resultRateio.getVlrDesdob(), BigDecimalUtil.MATH_CTX), 4);

						System.out.println("Rateio, percentual rateado : " + percentual);

					}

					percAcumulado = percAcumulado.add(percentual);

					EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

					EntityVO rateioEntityVO = dwfFacade.getDefaultValueObjectInstance("RateioRecDesp");

					DynamicVO rateioVO = (DynamicVO) rateioEntityVO;

					rateioVO.setProperty("NUFIN", resultRateio.getNufin());
					rateioVO.setProperty("ORIGEM", "F");
					rateioVO.setProperty("CODCENCUS", resultRateio.getCodCencus());
					rateioVO.setProperty("CODNAT", codnat);
					rateioVO.setProperty("CODPROJ", resultRateio.getCodProj());
					rateioVO.setProperty("DIGITADO", "N");
					rateioVO.setProperty("CODSITE", BigDecimal.ZERO);
					rateioVO.setProperty("CODPARC", resultRateio.getCodParc());
					rateioVO.setProperty("CODUSU", new BigDecimal(0));
					rateioVO.setProperty("DTALTER", TimeUtils.getNow());
					rateioVO.setProperty("PERCRATEIO", percentual);

					System.out.println("*****************************************");
					System.out.println("Inserindo Rateio");
					System.out.println("Numero Único :" + resultRateio.getNufin());
					System.out.println("Natureza : " + codnat);
					System.out.println("Centro de Resultado : " + resultRateio.getCodCencus());
					System.out.println("Projeto : " + resultRateio.getCodProj());
					System.out.println("Percentual Rateio : " + percentual);
					System.out.println("Vlr Rateio : " + vlrRateio);
					System.out.println("Percentual Acumulado : " + percAcumulado);
					System.out.println("*****************************************");

					PersistentLocalEntity createEntity = dwfFacade.createEntity("RateioRecDesp", rateioEntityVO);
					// Salva

					DynamicVO save = (DynamicVO) createEntity.getValueObject();

					System.out.println("-------------------------------------------");
					System.out.println("Rateio Inserido com sucesso!");
					System.out.println("-------------------------------------------");

				}

			}

		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Erro ao executar o rateio geraRateio()");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro executar o rateio : " + e.getMessage() + sw.toString());
			System.out.println("erro - " + e.getMessage() + sw.toString());
		}

	}

	public int retornaNumeLinhasParaRateio(JdbcWrapper jdbc, BigDecimal nureneg) throws Exception {
		
		System.out.println("Retornando numero de Linhas");

		int rownum = 0;

		try {

			NativeSql sqlReturn = new NativeSql(jdbc);
			sqlReturn.appendSql("SELECT SUM(VLRDESDOB) AS VLRDESDOB, GG.CODNAT, GG.CAMPO, GG.TIPO\r\n"
					+ "FROM (SELECT SUM(VLRDESDOB)                                   AS VLRDESDOB,\r\n"
					+ "             CASE WHEN CODNAT = 0 THEN 101010 ELSE CODNAT END AS CODNAT,\r\n"
					+ "             CAMPO,\r\n"
					+ "             CASE\r\n"
					+ "                 WHEN CODNAT IN (SELECT CODNAT\r\n"
					+ "                                 from AD_PARRENEGRATEIO d\r\n"
					+ "                                 where d.NUPAR = 1) THEN 'RP'\r\n"
					+ "                 ELSE TIPO END                                   TIPO\r\n"
					+ "      FROM (SELECT ISNULL((select CAMPO from AD_PARRENEGRATEIO d where d.NUPAR = 1 AND D.CODNAT = V.CODNAT),\r\n"
					+ "                          'VLRDESDOB') CAMPO,\r\n"
					+ "                   V.CODNAT,\r\n"
					+ "                   V.VLRDESDOB,\r\n"
					+ "                   'RA'                TIPO\r\n"
					+ "            FROM VGFFINRAT_OAB V\r\n"
					+ "            WHERE V.NURENEG = :NURENEG\r\n"
					+ "              AND V.RECDESP = 0\r\n"
					+ "            --AND NOT EXISTS (select 1 from AD_PARRENEGRATEIO d where d.NUPAR = 1 AND D.CODNAT = V.CODNAT)\r\n"
					+ "            UNION ALL\r\n"
					+ "            select CAMPO, CODNAT, 0, 'RP' TIPO\r\n"
					+ "            from AD_PARRENEGRATEIO d\r\n"
					+ "            where d.NUPAR = 1) VV\r\n"
					+ "      GROUP BY CODNAT, CAMPO, TIPO) GG\r\n"
					+ "GROUP BY GG.CODNAT, GG.CAMPO, GG.TIPO\r\n"
					+ "ORDER BY TIPO, 1");

			sqlReturn.setNamedParameter("NURENEG", nureneg);
			ResultSet resultSet = sqlReturn.executeQuery();

			while (resultSet.next()) {

				rownum++;

			}
		} catch (Exception e) {

			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Erro ao executar o retornaNumeLinhasParaRateio()");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro executar o rateio : " + e.getMessage() + sw.toString());
			System.out.println("erro - " + e.getMessage() + sw.toString());
		}
		
		System.out.println("Numero de linhas retornadas : " + rownum);

		return rownum;

	}

	public void gravaNumeroParcelasRecDespZero(JdbcWrapper jdbc, BigDecimal nureneg) throws Exception {

		System.out.println("Grava Número de Parcelas dentro da TGFREN com : gravaNumeroParcelasRecDespZero()");

		try {

			NativeSql sqlReturnRecDespZero = new NativeSql(jdbc);
			sqlReturnRecDespZero.appendSql("SELECT NUFIN FROM TGFREN N WHERE N.NURENEG = :NURENEG");
			sqlReturnRecDespZero.setNamedParameter("NURENEG", nureneg);

			ResultSet resultSetRecDespZero = sqlReturnRecDespZero.executeQuery();

			while (resultSetRecDespZero.next()) {

				NativeSql sqlReturn = new NativeSql(jdbc);
				sqlReturn.appendSql("SELECT MAX(isnull(\r\n"
						+ "                    cast(SUBSTRING(F.PARCRENEG, CHARINDEX('/', F.PARCRENEG) + 1, LEN(F.PARCRENEG)) as int), 1)) PARCELAS  \r\n"
						+ "                   FROM TGFFIN F WHERE F.NURENEG = :NURENEG \r\n"
						+ "                   AND F.RECDESP = 1");

				sqlReturn.setNamedParameter("NURENEG", nureneg);
				ResultSet resultSet = sqlReturn.executeQuery();

				if (resultSet.next()) {

					NativeSql sqlUpdate = new NativeSql(jdbc);
					sqlUpdate.appendSql(" UPDATE TGFREN \r\n" + "            SET AD_NUMEROPARCELAS = :PARCELAS \r\n"
							+ "            WHERE NURENEG = :NURENEG \r\n" + "              AND NUFIN = :NUFIN ");

					sqlUpdate.setNamedParameter("PARCELAS", resultSet.getBigDecimal("PARCELAS"));
					sqlUpdate.setNamedParameter("NURENEG", nureneg);
					sqlUpdate.setNamedParameter("NUFIN", resultSetRecDespZero.getBigDecimal("NUFIN"));

					sqlUpdate.executeUpdate();

				}

			}

		} catch (Exception e) {

			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Erro ao executar o gravaNumeroParcelasRecDespZero()");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro : " + e.getMessage() + sw.toString());
			System.out.println("erro - " + e.getMessage() + sw.toString());
		}

	}

	public void removeRateioExistente(BigDecimal nufin, String entiTyName) throws MGEModelException {

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			System.out.println("Deletando Rateio Existente");

			JapeWrapper configDao = JapeFactory.dao(entiTyName);
			configDao.deleteByCriteria("NUFIN = ?", nufin);

			System.out.println("Deletou rateio " + nufin);

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}
	}
	


}
