package br.com.oab.actions;

import br.com.oab.controller.BaixaController;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class BotaoBaixaTitulosGetNet implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao context) throws Exception {

		BaixaController baixaTitulos = new BaixaController();
		baixaTitulos.buscaFinanceiroEBaixa();
		
		context.setMensagemRetorno("Títulos baixados com sucesso!");

	}

}
