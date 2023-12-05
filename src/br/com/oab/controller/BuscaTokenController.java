package br.com.oab.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.gson.Gson;

import br.com.oab.dao.ParametrosDAO;
import br.com.oab.model.ParametrosModel;
import br.com.oab.model.TokenModel;
import br.com.oab.util.Http;

public class BuscaTokenController {

	public String checkToken(final ParametrosModel parametros) throws Exception {

		String token;

		if (parametros.getAccessToken() == null || parametros.getDhExpira() == null || (parametros.getDhExpira() != null
				&& parametros.getDhExpira().before(new Date(System.currentTimeMillis())))) {

			token = atualizaToken(parametros);

		} else {

			token = parametros.getAccessToken();
		}

		return token;

	}

	/*
	 * Atualiza o Token caso o mesmo seja inv�lido
	 */
	public String atualizaToken(ParametrosModel parametros) throws Exception {

		Gson gson = new Gson();
		TokenModel tokenModel = new TokenModel();

		try {

			System.out.println("*********************");
			System.out.println("* Atualizando Token *");
			System.out.println("*********************");

			String epAuthPath = parametros.getUrlAuth();

			final Map<String, String> headers = new HashMap<>();
			headers.put("content-type", "application/x-www-form-urlencoded");
			headers.put("authorization", "Basic " + parametros.getRefreshToken());

			final Http httpArquivo = new Http(epAuthPath);
			Http.Response responseArquivo = null;

			System.out.println("Ira fazer chamada http");

			String jsonAuth = "";

			responseArquivo = httpArquivo.post(headers, jsonAuth.getBytes("UTF-8"));

			// System.out.println("Fez chamada http: ");
			System.out.println("responseArquivo.getCode(): " + responseArquivo.getCode());
			System.out.println("responseArquivo.getMessage(): " + responseArquivo.getMessage());

			String retornoArquivo = new String(responseArquivo.getData());
			System.out.println("retornoArquivo: " + retornoArquivo);

			tokenModel = gson.fromJson(retornoArquivo, TokenModel.class);

			parametros.setAccessToken(tokenModel.getAccess_token());
			parametros.setTokenType(tokenModel.getToken_type());

			BigDecimal expiresInSecondsBigDecimal = tokenModel.getExpires_in().divide(new BigDecimal(60));

			parametros.setExpiraMin(expiresInSecondsBigDecimal);

			// converte a data exemplo "Tue Oct 24 08:00:21 BRT 2023" e atribui para
			// variavel Date dhExpira
			// String dataExpira = "Tue Oct 26 08:00:21 BRT 2023";
			// Date dhExpira = new Date(dataExpira);

			DateTime dateTimeAtual = DateTime.now();

			int expiresInSeconds = expiresInSecondsBigDecimal.intValue();

			DateTime dateTimeFuturo = dateTimeAtual.plusMinutes(expiresInSeconds);

			// parametros.setDhExpira(new
			// Date(System.currentTimeMillis()+(tokenModel.getExpires_in().longValue() *
			// 1000)));

			java.util.Date utilDate = dateTimeFuturo.toDate();

			parametros.setDhExpira(utilDate);

			// Atualiza no sistema no novo token
			System.out.println("Ira atualizar o token no db");
			ParametrosDAO dao = new ParametrosDAO();
			dao.atualizarToken(parametros);

		} catch (MalformedURLException e1) {
			throw new Exception("Erro ao Recuperar Access Token! " + e1.getMessage());
		} catch (IOException e2) {
			throw new Exception("Erro ao Recuperar Access Token! " + e2.getMessage());
		}

		return tokenModel.getAccess_token();

	}

}
