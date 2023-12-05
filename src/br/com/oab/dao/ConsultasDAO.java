package br.com.oab.dao;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.oab.model.AtualizaBoletoModel;
import br.com.oab.model.UserModel;

public class ConsultasDAO {

    public static String danfeBase64(final BigDecimal nunota) throws Exception {

        final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

        String danfe = null;

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);
        try {
            nativeSql.appendSql("SELECT SNK_CONVERTE_BLOB_EM_BASE64(PDFDANFE) DANFE" +
                                "  FROM TGFPDF" +
                                " WHERE NUNOTA = " + nunota.toString());

            final ResultSet resultSet = nativeSql.executeQuery();
            if (resultSet.next()) {
                danfe = resultSet.getString("DANFE");
            }

        } finally {
            JdbcWrapper.closeSession(jdbcWrapper);
        }

        return danfe;

    }

    public static List<AtualizaBoletoModel> consultaBoletosPend() throws Exception{

        System.out.println("Entrou carregaEmpData");

        final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

        final NativeSql nsBoletos = new NativeSql(jdbcWrapper);

        String sql = "SELECT FIN.NUFIN, FIN.DTVENC, FIN.NOSSONUM, FIN.CODIGOBARRA, FIN.LINHADIGITAVEL" +
                     "  FROM AD_CONTBOLAOB CON," +
                     "       TGFFIN FIN" +
                     " WHERE ISNULL(CON.ATUALIZADO,'N') = 'N'" +
                     "   AND CON.NUFIN = FIN.NUFIN" +
                     " ORDER BY NUFIN, DTVENC";

        nsBoletos.appendSql(sql);

        final List<AtualizaBoletoModel> listBoletos = new ArrayList<>();
        try (final ResultSet rsList = nsBoletos.executeQuery()) {
            while (rsList.next()) {
                AtualizaBoletoModel atubol = new AtualizaBoletoModel();
                atubol.setNufin(rsList.getBigDecimal("NUFIN"));
                atubol.setDtvenc(rsList.getTimestamp("DTVENC"));
                atubol.setCodigoBarra(rsList.getString("CODIGOBARRA"));
                atubol.setLinhaDigitavel(rsList.getString("LINHADIGITAVEL"));
                atubol.setNossoNumero(rsList.getString("NOSSONUM"));
                listBoletos.add(atubol);
            }
        }

        JdbcWrapper.closeSession(jdbcWrapper);

        System.out.println("Preencheu listBoletos");

        return listBoletos;

    }

    public static String OrigemWebPedido(final BigDecimal nufin) throws Exception {

        final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

        String origemWeb = null;

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);
        try {
            nativeSql.appendSql("SELECT ISNULL(CAB.AD_WEB,'N') AD_WEB" +
                                "  FROM TGFFIN FIN," +
                                "       TGFCAB CAB" +
                                " WHERE FIN.NUFIN  = " + nufin.toString() +
                                "   AND FIN.NUNOTA = CAB.NUNOTA");

            final ResultSet resultSet = nativeSql.executeQuery();
            if (resultSet.next()) {
                origemWeb = resultSet.getString("AD_WEB");
            }

        } finally {
            JdbcWrapper.closeSession(jdbcWrapper);
        }

        return origemWeb;

    }


    public static UserModel dadosUsuario(final BigDecimal codUsu) throws Exception {

        final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

        UserModel user = new UserModel();

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);
        try {
            nativeSql.appendSql("SELECT NOMEUSU, INTERNO" +
                    "  FROM TSIUSU" +
                    " WHERE CODUSU = " + codUsu.toString());

            final ResultSet resultSet = nativeSql.executeQuery();
            if (resultSet.next()) {
                user.setUserId(codUsu);
                user.setNomeUsu(resultSet.getString("NOMEUSU"));
                user.setPass(resultSet.getString("INTERNO"));
            }

        } finally {
            JdbcWrapper.closeSession(jdbcWrapper);
        }

        return user;
    }

    public static BigDecimal consNufinRemessa(final BigDecimal nufin) throws Exception {

        final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

        BigDecimal qtd = BigDecimal.ZERO;

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);
        try {
            nativeSql.appendSql("SELECT COUNT(1) QTD FROM TGFFIN FIN WHERE FIN.NUFIN = " + nufin.toString() + " AND FIN.NUMREMESSA IS NOT NULL");

            final ResultSet resultSet = nativeSql.executeQuery();
            if (resultSet.next()) {
                qtd = resultSet.getBigDecimal("QTD");
            }

        } finally {
            JdbcWrapper.closeSession(jdbcWrapper);
        }

        return qtd;
    }

}

