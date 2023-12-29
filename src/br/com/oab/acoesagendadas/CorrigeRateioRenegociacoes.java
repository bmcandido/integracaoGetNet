package br.com.oab.acoesagendadas;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import br.com.oab.dao.GeraRateioDAO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class CorrigeRateioRenegociacoes implements ScheduledAction {

	@Override
	public void onTime(ScheduledActionContext contexto) {

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		System.out.println("Entrou na ação agendada corrige rateio renegociação");

		JdbcWrapper jdbc = null;

		try {

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			NativeSql sqlReturn = new NativeSql(jdbc);
			sqlReturn.appendSql("SELECT NURENEG FROM TGFFIN F WHERE F.AD_RENEGOCIADO = 'A' GROUP BY NURENEG ");

			ResultSet resultSet = sqlReturn.executeQuery();

			while (resultSet.next()) {

				System.out.println("AlteraTituloAntesReneg Nro Renegociacao : " + resultSet.getBigDecimal("NURENEG"));

				GeraRateioDAO geraRateioDAOMetodo = new GeraRateioDAO();
				
				//Grava a quantidade de Parcelas na TGFREN
				
				geraRateioDAOMetodo.gravaNumeroParcelasRecDespZero(jdbc,resultSet.getBigDecimal("NURENEG"));

				final Boolean gerouRateio = geraRateioDAOMetodo.calculaRateioDAO(jdbc,
						resultSet.getBigDecimal("NURENEG"), 1);

				if (gerouRateio) {
					
					
					System.out.println("Alterou para RATEADO = SIM");

					NativeSql sqlUpdate = new NativeSql(jdbc);
					sqlUpdate.appendSql(
							" UPDATE TGFFIN  SET RATEADO = 'S', AD_RENEGOCIADO = 'R'  WHERE RECDESP = 1 AND NURENEG = "
									+ resultSet.getBigDecimal("NURENEG"));

					sqlUpdate.executeUpdate();

				}

				System.out.println("Executou o metodo calculaRateioDAO : " + resultSet.getBigDecimal("NURENEG"));

			}

			System.out.println("Fim da Rotina de Correção da Renegociação");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		} catch (Exception e) {
			
		
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("Erro Exceção rotina CorrigeRateioRenegociacoes " + e.getMessage() + sw.toString());
			System.out.println("Erro : " + e.getMessage() + sw.toString());
		} finally {

			JdbcWrapper.closeSession(jdbc);

		}

	}

}
