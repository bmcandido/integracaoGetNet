package br.com.oab.util;


import java.io.*;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;


public class Global {

	public static JdbcWrapper iniciaSessao() {
		final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

		return jdbcWrapper;
	}

	public static void fechaSessao(JdbcWrapper jdbcWrapper) {
		JdbcWrapper.closeSession(jdbcWrapper);
	}

	public static BigDecimal codigoPai(ContextoAcao contexto, String campo) {
		Registro reg = contexto.getLinhaPai();
		BigDecimal codPai = (BigDecimal) reg.getCampo(campo);

		return codPai;
	}

	//Converte de CLOB para String 
	public static String ClobToString(Clob cl) throws IOException, SQLException {
		if (cl == null)
			return "";

		StringBuffer strOut = new StringBuffer();
		String buff;

		BufferedReader br = new BufferedReader(cl.getCharacterStream());

		while ((buff = br.readLine()) != null)
			strOut.append(buff);

		return strOut.toString();
	}

	public static String getValueMaskFormat(String pMask, String pValue,
											boolean pReturnValueEmpty) {

		/*
		 * Verifica se se foi configurado para nao retornar a
		 * mascara se a string for nulo ou vazia se nao
		 * retorna somente a mascara.
		 */
		if (pReturnValueEmpty == true
				&& (pValue == null || pValue.trim().equals("")))
			return "";

		/*
		 * Substituir as mascaras passadas como  9, X, * por # para efetuar a formatcao
		 */
		//pMask = pMask.replaceAll("*", "#");
		pMask = pMask.replaceAll("9", "#");
		pMask = pMask.toUpperCase().replaceAll("X", "#");

		/*
		 * Formata valor com a mascara passada
		 */
		for (int i = 0; i < pValue.length(); i++) {
			pMask = pMask.replaceFirst("#", pValue.substring(i, i + 1));
		}

		/*
		 * Subistitui por string vazia os digitos restantes da mascara
		 * quando o valor passado é menor que a mascara
		 */
		return pMask.replaceAll("#", "");
	}


	public static String FileLastModifiedTime(final File arquivo) throws Exception{

		String dlm=null;

		try {
			DateFormat df = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a");
			String dateModified = df.format (new Date(arquivo.lastModified()));

			dlm = dateModified.substring(0,1).toUpperCase() + dateModified.substring(1);

		} catch (final Exception exception) {
			exception.printStackTrace();

			StringWriter errors = new StringWriter();
			exception.printStackTrace(new PrintWriter(errors));

		}

		return dlm;

	}

}

