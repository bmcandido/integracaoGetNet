package br.com.oab.acoesagendadas;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import br.com.oab.controller.BaixaController;

public class GetNetAcaoAgendadaBaixarTitulos implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext ctx) {
        try {

        	executaBaixaFinanceiro();


        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println(sw.toString());
        }

    }

    public void executabaixa() throws Exception {

    }

    void executaBaixaFinanceiro() throws Exception {
    	
    	 System.out.println("****************************************************************************");
    	 System.out.println("Ação agendada GETNET Baixas");
    	 System.out.println("****************************************************************************");


        BaixaController baixaTitulos = new BaixaController();
        
       BigDecimal usuarioBaixa = new BigDecimal(225);



        baixaTitulos.buscaFinanceiroEBaixa(usuarioBaixa);

    }

}
