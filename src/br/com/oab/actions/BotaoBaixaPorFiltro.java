package br.com.oab.actions;

import java.math.BigDecimal;
import com.sankhya.util.StringUtils;
import br.com.oab.controller.BaixaControllerPorFiltro;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

public class BotaoBaixaPorFiltro implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {

		if (contexto.getLinhas().length > 0) {

			BigDecimal codEmp = StringUtils.convertToBigDecimal(contexto.getParam("CODEMP").toString());
			BaixaControllerPorFiltro baixaTitulos = new BaixaControllerPorFiltro();

			boolean retornoBaixa = false;

			for (Registro registro : contexto.getLinhas()) {

				final String linkId = (String) registro.getCampo("LINKID");

				retornoBaixa = baixaTitulos.buscaFinanceiroEBaixaPorFiltro(codEmp, linkId);

			}

			if (retornoBaixa == true) {

				contexto.setMensagemRetorno("Movimentos baixados com sucesso!");

			} else {

				contexto.setMensagemRetorno("Não foram encotrados títulos a serem baixados");

			}

		} else {

			contexto.setMensagemRetorno("Selecione um registro!");

		}

	}

}
