package br.com.oab.actions;

import br.com.oab.controller.BaixaController;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class BotaoBaixaTitulosGetNet implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {

		//BigDecimal codEmp = StringUtils.convertToBigDecimal(contexto.getParam("CODEMP").toString());
		//String getTipo = (String) contexto.getParam("TIPO");

//		if (contexto.getLinhas().length > 0 && getTipo.equals("PORDATA")) {
//			
//			System.out.println("*************************************************");
//			System.out.println("Baixa Executada de forma Manual");
//			System.out.println("*************************************************");
//
//			BaixaControllerPorFiltro baixaTitulos = new BaixaControllerPorFiltro();
//
//			boolean retornoBaixa = false;
//
//			for (Registro registro : contexto.getLinhas()) {
//
//				final String linkId = (String) registro.getCampo("LINKID");
//
//				retornoBaixa = baixaTitulos.buscaFinanceiroEBaixaPorFiltro(codEmp, linkId);
//
//			}
//
//			if (retornoBaixa == true) {
//
//				contexto.setMensagemRetorno("Movimentos baixados com sucesso!");
//
//			} else {
//
//				contexto.setMensagemRetorno("Não foram encotrados títulos a serem baixados");
//
//			}
//
//		} else {
			
			System.out.println("*************************************************");
			System.out.println("Baixa Executada pelo Agendador");
			System.out.println("*************************************************");

			BaixaController baixaTitulos = new BaixaController();
			baixaTitulos.buscaFinanceiroEBaixa();

			contexto.setMensagemRetorno("Títulos baixados com sucesso!");

	//	}

	}

}
