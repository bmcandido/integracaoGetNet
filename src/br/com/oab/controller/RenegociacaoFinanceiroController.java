package br.com.oab.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sankhya.util.BigDecimalUtil;

import br.com.oab.model.RenegociacaoModel;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;

public class RenegociacaoFinanceiroController {

	public static List<BigDecimal> obtemOrigensRenegociacaoOrdenada(JdbcWrapper jdbc, BigDecimal nureneg, int parametro)
			throws Exception {

		System.out.println("Obtendo obtemOrigensRenegociacaoOrdenada() renegociacao : " + nureneg);
		Collection<BigDecimal> renegociacoes = new ArrayList<>();

		// 1 - Fazer a renegociacao em escalada
		// 2 - Fazer a renegociacao dela mesmo

		if (parametro == 1) {

			List<BigDecimal> list = new ArrayList<>();

			list.add(nureneg);

			return list;

		} else {

			try {
				NativeSql sqlReturn = new NativeSql(jdbc);
				sqlReturn.appendSql("SELECT NURENEG FROM SANKHYA.obterRenegociacaoOrigens(:NURENEG) GG");
				sqlReturn.setNamedParameter("NURENEG", nureneg);

				ResultSet resultSet = sqlReturn.executeQuery();

				while (resultSet.next()) {
					renegociacoes.add(resultSet.getBigDecimal("NURENEG"));
				}
			} catch (Exception e) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("Erro ao obter renegociacoes : obtemOrigensRenegociacaoOrdenada()");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				StringBuffer mensagem = new StringBuffer();
				e.printStackTrace(pw);
				mensagem.append("Erro executar ao obter renegociacoes  obtemOrigensRenegociacaoOrdenada(): "
						+ e.getMessage() + sw.toString());
				System.out.println("erro - " + e.getMessage() + sw.toString());
			}

			// Convertendo a Collection para uma List
			List<BigDecimal> renegociacoesOrdenadas = new ArrayList<>(renegociacoes);

			// Ordenando a lista do menor para o maior
			Collections.sort(renegociacoesOrdenadas);

			return renegociacoesOrdenadas;

		}

	}

	public static Collection<RenegociacaoModel> obtemOrigensRenegociacao(JdbcWrapper jdbc, BigDecimal nureneg,
			BigDecimal recDesp) throws Exception {

		Collection<RenegociacaoModel> renegociacoesSet = new ArrayList<RenegociacaoModel>();

		try {

			System.out.println("Obtendo obtemOrigensRenegociacao() recdesp : " + recDesp);

			NativeSql sqlReturn = new NativeSql(jdbc);

			sqlReturn.appendSql("SELECT DISTINCT *\r\n" 
			        + "FROM (SELECT N.NURENEGORIG,\r\n"
					+ "             F.CODNAT,\r\n" 
			        + "             F.CODCENCUS,\r\n" 
					+ "             F.CODPROJ,\r\n"
					+ "             F.RECDESP,\r\n" 
					+ "             F.NURENEG,\r\n" 
					+ "             F.NUFIN,\r\n"
					+ "             F.VLRDESDOB,\r\n" 
					+ "             F.VLRJURONEGOC,\r\n"
					+ "             F.VLRMULTANEGOC,\r\n" 
					+ "             F.VLRVENDOR CORRECAO, \r\n"
					+ "             F.CODPARC  \r\n" 
					+ "      FROM TGFFIN F\r\n"
					+ "               LEFT JOIN TGFREN N ON N.NURENEG = F.NURENEG\r\n"
					+ "      WHERE F.NURENEG = :NURENEG\r\n" 
					+ "      UNION ALL\r\n" 
					+ "      SELECT N.NURENEGORIG,\r\n"
					+ "             F.CODNAT,\r\n" 
					+ "             F.CODCENCUS,\r\n" 
					+ "             F.CODPROJ,\r\n"
					+ "             CASE WHEN N.NURENEGORIG > 0 THEN 1 ELSE F.RECDESP END                 RECDESP,\r\n"
					+ "             N.NURENEGORIG                                                         NURENEG,\r\n"
					+ "             F.NUFIN,\r\n" 
					+ "             F.VLRDESDOB,\r\n" 
					+ "             F.VLRJURONEGOC,\r\n"
					+ "             F.VLRMULTANEGOC,\r\n" 
					+ "             F.VLRVENDOR CORRECAO, \r\n"
					+ "             F.CODPARC  \r\n" 
					+ "      FROM TGFREN N\r\n"
					+ "               INNER JOIN TGFFIN F ON F.NUFIN = N.NUFIN\r\n"
					+ "      WHERE N.NURENEGORIG = :NURENEG) GG\r\n" 
					+ "WHERE RECDESP = :RECDESP \r\n"
					+ "ORDER BY RECDESP, NUFIN");

			sqlReturn.setNamedParameter("NURENEG", nureneg);
			sqlReturn.setNamedParameter("RECDESP", recDesp);

			System.out.println("EXECUTOU A QUERY :" + sqlReturn.toString());

			ResultSet resultSet = sqlReturn.executeQuery();

			while (resultSet.next()) {

				RenegociacaoModel reneg = new RenegociacaoModel();

				reneg.setCodCencus(resultSet.getBigDecimal("CODCENCUS"));
				reneg.setCodnat(resultSet.getBigDecimal("CODNAT"));
				reneg.setCodProj(resultSet.getBigDecimal("CODPROJ"));
				reneg.setVlrDesdob(resultSet.getBigDecimal("VLRDESDOB"));
				reneg.setVlrCorrecao(resultSet.getBigDecimal("CORRECAO"));
				reneg.setVlrJurosNegoc(resultSet.getBigDecimal("VLRJURONEGOC"));
				reneg.setVlrMultaNegoc(resultSet.getBigDecimal("VLRMULTANEGOC"));
				reneg.setCodparc(resultSet.getBigDecimal("CODPARC"));
				reneg.setNufin(resultSet.getBigDecimal("NUFIN"));

				renegociacoesSet.add(reneg);

			}

		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Erro ao obter obtemRenegociacao()");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append(
					"Erro executar ao obter renegociacoes  obtemRenegociacao(): " + e.getMessage() + sw.toString());
			System.out.println("erro - " + e.getMessage() + sw.toString());

		}

		return renegociacoesSet;

	}

	public static RenegociacaoModel obtemOrigensRenegociacaoRECDESPZero(JdbcWrapper jdbc, BigDecimal nureneg)
			throws Exception {

		RenegociacaoModel renegociacoesSet = new RenegociacaoModel();

		try {

			System.out.println("Obtendo obtemOrigensRenegociacaoRECDESPZero()");

			NativeSql sqlReturn = new NativeSql(jdbc);

			//sqlReturn.appendSql("SELECT  *FROM RetornaValoresCorrigidos(:NURENEG) GG");
			
			sqlReturn.appendSql("SELECT SUM(F.VLRMULTANEGOC) VLRMULTANEGOC , SUM(F.VLRJURONEGOC) VLRJURONEGOC, SUM(F.VLRVENDOR) VLRCORRECAO\r\n"
					+ "FROM TGFFIN F WHERE F.NURENEG = :NURENEG \r\n"
					+ "AND F.RECDESP = 0");
			

			sqlReturn.setNamedParameter("NURENEG", nureneg);

			// System.out.println("EXECUTOU A QUERY :" + sqlReturn.toString());

			ResultSet resultSet = sqlReturn.executeQuery();

			if (resultSet.next()) {

				renegociacoesSet.setVlrJurosNegoc(resultSet.getBigDecimal("VLRJURONEGOC"));
				renegociacoesSet.setVlrCorrecao(resultSet.getBigDecimal("VLRCORRECAO"));
				renegociacoesSet.setVlrMultaNegoc(resultSet.getBigDecimal("VLRMULTANEGOC"));

				
			}
			
			return renegociacoesSet;

		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Erro ao obter obtemRenegociacao()");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append(
					"Erro executar ao obter renegociacoes  obtemRenegociacao(): " + e.getMessage() + sw.toString());
			System.out.println("erro - " + e.getMessage() + sw.toString());

		}

		return renegociacoesSet;

	}

	public static BigDecimal obtemQuantidadeDeParcelasReneg(JdbcWrapper jdbc, BigDecimal nureneg) throws Exception {

		BigDecimal qtdParcelas = BigDecimalUtil.ZERO_VALUE;

		try {

			System.out.println("Obtendo obtemQuantidadeDeParcelasReneg()");

			NativeSql sqlReturn = new NativeSql(jdbc);

			sqlReturn.appendSql("SELECT MAX(GG.PARCELA) PARCELA FROM (SELECT MAX(isnull(N.AD_NUMEROPARCELAS,\r\n"
					+ "                  cast(SUBSTRING(F.PARCRENEG, CHARINDEX('/', F.PARCRENEG) + 1, LEN(F.PARCRENEG)) as int))) PARCELA,\r\n"
					+ "       F.NURENEG\r\n" 
					+ "FROM TGFFIN F\r\n"
					+ "         LEFT JOIN TGFREN N ON F.NUFIN = N.NUFIN\r\n" 
					+ "WHERE F.RECDESP = 1\r\n"
					+ "GROUP BY F.NURENEG\r\n" 
					+ "UNION ALL\r\n"
					+ "SELECT N.AD_NUMEROPARCELAS, N.NURENEG FROM TGFREN N) GG\r\n" 
					+ "WHERE GG.NURENEG = :NURENEG");

			sqlReturn.setNamedParameter("NURENEG", nureneg);

			ResultSet resultSet = sqlReturn.executeQuery();

			if (resultSet.next()) {

				qtdParcelas = resultSet.getBigDecimal("PARCELA");

			}

		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("Erro ao obter obtemQuantidadeDeParcelasReneg()");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro executar ao obter QUANTIDADE DE PARCELAS  obtemQuantidadeDeParcelasReneg(): "
					+ e.getMessage() + sw.toString());
			System.out.println("erro - " + e.getMessage() + sw.toString());

		}

		return qtdParcelas;

	}

}
