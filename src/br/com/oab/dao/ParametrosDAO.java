package br.com.oab.dao;


import br.com.oab.util.Global;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.oab.model.ParametrosModel;

public class ParametrosDAO {

    public ParametrosModel consultaParametros() throws Exception {

        //System.out.println("Entrou consultaParametros");

        JdbcWrapper jdbcWrapper = Global.iniciaSessao();
        jdbcWrapper.openSession();

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);

        ParametrosModel parametros = new ParametrosModel();

        nativeSql.appendSql("SELECT * FROM AD_GTNPAR WHERE NUPAR = 1");

        final ResultSet resultSet = nativeSql.executeQuery();

        if(resultSet.next()) {

            //System.out.println("Entrou para atribuir parametros");

            parametros.setClientId(resultSet.getString("CLIENTID"));
            parametros.setClientSecret(resultSet.getString("SECRETID"));
            parametros.setSellerId(resultSet.getString("SELLERID"));
            parametros.setRefreshToken(resultSet.getString("REFRESHTOKEN"));
            parametros.setAccessToken(resultSet.getString("ACCESSTOKEN"));
            parametros.setExpiraMin(resultSet.getBigDecimal("EXPIRAMIN"));
            parametros.setBasePath(resultSet.getString("BASEPATH"));
            parametros.setUrlAuth(resultSet.getString("URLAUTH"));
            parametros.setDhExpira(resultSet.getDate("DHEXPIRA"));

        }
        //System.out.println("Finalizou atribuição de parametros");

        Global.fechaSessao(jdbcWrapper);

        return parametros;
    }

    /*
     * Atualiza o Token dentro do sistema
     */
    public void atualizarToken(final ParametrosModel parametros) throws SQLException {

        //System.out.println("Entrou atualizarToken");
        JdbcWrapper jdbcWrapper = Global.iniciaSessao();
        jdbcWrapper.openSession();


        System.out.println("parametros.getDtaExpira: " + parametros.getDhExpira().toString());


        final String sql = "UPDATE AD_GTNPAR "
        		+ "SET ACCESSTOKEN = ?, "
        		+ "DHEXPIRA = ?, "
        		+ "EXPIRAMIN = ? "
        		+ "WHERE NUPAR = 1";


        //System.out.println("SQL "+sql);
        final Connection connection = jdbcWrapper.getConnection();

        //System.out.println("ira atualizar token");

        try (final CallableStatement preparedStatement  = connection.prepareCall(sql)) {
        	
        	System.out.println("Os parametros do token são : " + parametros.toString());
        	
        	 preparedStatement.setString(1, parametros.getAccessToken());
             preparedStatement.setTimestamp(2, new java.sql.Timestamp(parametros.getDhExpira().getTime()));
             preparedStatement.setBigDecimal(3, parametros.getExpiraMin());
             
             preparedStatement.execute();
            //System.out.println("atualizou token");
        }

        Global.fechaSessao(jdbcWrapper);

    }

}

