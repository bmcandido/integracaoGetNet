package br.com.oab.acoesagendadas;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import br.com.oab.controller.BaixaController;
import br.com.oab.controller.IntegracaoGetnet;

public class GetNetAcaoAgendadaBuscaOrders implements ScheduledAction {

	@Override
	public void onTime(ScheduledActionContext ctx) {
		try {

			consultaLinksEbuscaOrdensELinks();

			System.out.println("Ação agendada GETNET de consulta Ordens e Baixas realizada com sucesso!");

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			System.out.println(sw.toString());
		}

	}

	public void executabaixa() throws Exception {

	}

	void consultaLinksEbuscaOrdensELinks() throws Exception {

		IntegracaoGetnet integracaoGetnet = new IntegracaoGetnet();

		// Consulta os Links

		integracaoGetnet.consultaLinksGetnet();

		// Consulta As Ordens
		integracaoGetnet.consultaOrdersGetnet();

	}

}
