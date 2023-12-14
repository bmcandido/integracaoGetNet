package br.com.oab.actions;

import java.sql.Timestamp;

import br.com.oab.controller.BuscaTokenController;
import br.com.oab.controller.IntegracaoGetnet;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.oab.dao.ParametrosDAO;
import br.com.oab.model.ParametrosModel;

public class BotaoConsultaLinksGetnet implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {

		if (contexto.getParam("DTINI") != null && contexto.getParam("DTFIM") != null) {

			Timestamp dataInicio = (Timestamp) contexto.getParam("DTINI");
			Timestamp dataFim = (Timestamp) contexto.getParam("DTFIM");

			consultaLinks(dataInicio, dataFim);

			contexto.setMensagemRetorno("Registros Importados com sucesso!");

		} else {

			// Consulta Parametros
			ParametrosDAO parDao = new ParametrosDAO();
			ParametrosModel parametros;
			parametros = parDao.consultaParametros();

			// Busca novo Token
			BuscaTokenController tokenController = new BuscaTokenController();
			String token = tokenController.checkToken(parametros);

			System.out.println("Token: " + token);

			IntegracaoGetnet integracaoGetnet = new IntegracaoGetnet();
			integracaoGetnet.consultaLinksGetnet();

			contexto.setMensagemRetorno("Links inseridos com sucesso!");
		}

	}

	void consultaLinks(Timestamp dataInicio, Timestamp dataFim) throws Exception {

		IntegracaoGetnet integracaoGetnet = new IntegracaoGetnet();

		// Consulta os Links por filtro

		integracaoGetnet.consultaLinksporFiltroGetnet(dataInicio, dataFim);

		// Consulta As Ordens
		// integracaoGetnet.consultaOrdersGetnet();

	}

}
