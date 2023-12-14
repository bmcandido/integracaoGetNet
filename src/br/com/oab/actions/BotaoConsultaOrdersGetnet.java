package br.com.oab.actions;

import java.sql.Timestamp;

import br.com.oab.controller.BuscaTokenController;
import br.com.oab.controller.IntegracaoGetnet;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.oab.dao.ParametrosDAO;
import br.com.oab.model.ParametrosModel;

public class BotaoConsultaOrdersGetnet implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
    	
    	

		if (contextoAcao.getParam("DTINI") != null && contextoAcao.getParam("DTFIM") != null) {

			Timestamp dataInicio = (Timestamp) contextoAcao.getParam("DTINI");
			Timestamp dataFim = (Timestamp) contextoAcao.getParam("DTFIM");

			consultaOrders(dataInicio, dataFim);

			contextoAcao.setMensagemRetorno("Registros Importados com sucesso!");

		} else {
			
			

	        //Consulta Parametros
	        ParametrosDAO parDao = new ParametrosDAO();
	        ParametrosModel parametros;
	        parametros = parDao.consultaParametros();

	        // Busca novo Token
	        BuscaTokenController tokenController = new BuscaTokenController();
	        String token = tokenController.checkToken(parametros);

	        System.out.println("Token: " + token);

	        IntegracaoGetnet integracaoGetnet = new IntegracaoGetnet();
	        integracaoGetnet.consultaOrdersGetnet();
	        
	        contextoAcao.setMensagemRetorno("Ordens inseridas com sucesso!");
			
		}


    }
    
    
	void consultaOrders(Timestamp dataInicio, Timestamp dataFim) throws Exception {

		IntegracaoGetnet integracaoGetnet = new IntegracaoGetnet();

		// Consulta As Ordens
		integracaoGetnet.consultaOrdersGetnet();

	}

}
