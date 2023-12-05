package br.com.oab.actions;

import java.sql.Timestamp;

import br.com.oab.controller.IntegracaoGetnet;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class BotaoConsultaOrdersELinksPorFiltro implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {

		if (contexto.getParam("DTINI") != null && contexto.getParam("DTFIM") != null) {

			Timestamp dataInicio = (Timestamp) contexto.getParam("DTINI");
			Timestamp dataFim = (Timestamp) contexto.getParam("DTFIM");

			consultaOrdersELinks(dataInicio, dataFim);

			contexto.setMensagemRetorno("Registros Importados com sucesso!");

		} else {

			contexto.setMensagemRetorno("Não encontrados");
		}

	}

	void consultaOrdersELinks(Timestamp dataInicio, Timestamp dataFim) throws Exception {

		IntegracaoGetnet integracaoGetnet = new IntegracaoGetnet();

		// Consulta os Links por filtro

		integracaoGetnet.consultaLinksporFiltroGetnet(dataInicio, dataFim);

		// Consulta As Ordens
		integracaoGetnet.consultaOrdersGetnet();

	}

}
