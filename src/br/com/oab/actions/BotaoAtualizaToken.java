package br.com.oab.actions;


import java.io.PrintWriter;
import java.io.StringWriter;
import br.com.oab.controller.BuscaTokenController;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.oab.dao.ParametrosDAO;
import br.com.oab.model.ParametrosModel;

public class BotaoAtualizaToken implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        try {
            //Consulta Parametros
            ParametrosDAO parDao = new ParametrosDAO();
            ParametrosModel parametros = parDao.consultaParametros();

            //System.out.println("Entrou AtualizaToken");
            BuscaTokenController tokenController = new BuscaTokenController();
            String token = tokenController.checkToken(parametros);
            contextoAcao.setMensagemRetorno("Token: " + token);

        } catch (Exception exception) {
            //System.out.println("Entrou exception AtualizaToken");
            exception.printStackTrace();

            StringWriter errors = new StringWriter();
            exception.printStackTrace(new PrintWriter(errors));

            contextoAcao.mostraErro("Erro: " + exception.getMessage());

        }
    }
}
