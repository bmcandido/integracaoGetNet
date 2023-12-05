package br.com.oab.actions;

import br.com.oab.controller.BuscaTokenController;
import br.com.oab.controller.IntegracaoGetnet;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.oab.dao.ParametrosDAO;
import br.com.oab.model.ParametrosModel;

public class BotaoConsultaLinksGetnet implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        //Consulta Parametros
        ParametrosDAO parDao = new ParametrosDAO();
        ParametrosModel parametros;
        parametros = parDao.consultaParametros();

        // Busca novo Token
        BuscaTokenController tokenController = new BuscaTokenController();
        String token = tokenController.checkToken(parametros);

        System.out.println("Token: " + token);

        IntegracaoGetnet integracaoGetnet = new IntegracaoGetnet();
        integracaoGetnet.consultaLinksGetnet();
        
        contextoAcao.setMensagemRetorno("Links inseridos com sucesso!");

    }

}
