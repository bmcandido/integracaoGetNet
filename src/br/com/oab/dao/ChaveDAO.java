package br.com.oab.dao;


import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;

public class ChaveDAO {

    private final JdbcWrapper jdbcWrapper;

    public ChaveDAO(final JdbcWrapper jdbcWrapper) {
        this.jdbcWrapper = jdbcWrapper;
    }

    public BigDecimal carregar(final String tabela, final String coluna) throws Exception {
        BigDecimal valor = null;

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);
        nativeSql.appendSql("select nvl(max(" + coluna + "),0) valor ");
        nativeSql.appendSql("  from " + tabela);

        final ResultSet resultSet = nativeSql.executeQuery();
        if (resultSet.next()) {
            valor = resultSet.getBigDecimal("valor");
        }

        return new BigDecimal((valor != null ? valor.longValue() : 0L) + 1L);
    }

    public BigDecimal carregar(final String tabela, final String coluna1, final BigDecimal vlrcol1, final String coluna2) throws Exception {
        BigDecimal valor = null;

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);
        nativeSql.appendSql("select nvl(max(" + coluna2 + "),0) valor ");
        nativeSql.appendSql("  from " + tabela);
        nativeSql.appendSql(" where " + coluna1 + " = " + vlrcol1);
        
        final ResultSet resultSet = nativeSql.executeQuery();
        if (resultSet.next()) {
            valor = resultSet.getBigDecimal("valor");
        }

        return new BigDecimal((valor != null ? valor.longValue() : 0L) + 1L);
    }

    public BigDecimal carregaOrdSeq(String p_linkid, String p_orderid) throws Exception {
        BigDecimal valor = null;

        final NativeSql nativeSql = new NativeSql(jdbcWrapper);
        nativeSql.appendSql("select max(ISNULL(seq,0))+1 valor " +
                            "  from AD_GTNPMTORD " +
                            " where LINKID = '" + p_linkid + "' " +
                            "   and PMT_ORDERID = '" + p_orderid + "' ");

        final ResultSet resultSet = nativeSql.executeQuery();
        if (resultSet.next()) {
            valor = resultSet.getBigDecimal("valor");
        }

        return valor;
    }


    
    public BigDecimal geraTGFNUM(final String tabela, final String coluna) throws Exception {

    	BigDecimal valor = null;
    	
    	jdbcWrapper.openSession();
        
		final CallableStatement callableStatement = jdbcWrapper.getConnection()
				.prepareCall("{call STP_KEYGEN_TGFNUM(?, ?, ?, ?, ?, ?)}");
		
		callableStatement.setQueryTimeout(60);

		callableStatement.setString("p_arquivo", tabela);
		callableStatement.setInt("p_codemp", 1);
		callableStatement.setString("p_tabela", tabela);
		callableStatement.setString("p_campo", coluna);
		callableStatement.setInt("p_dsync", 0);
		callableStatement.registerOutParameter("p_ultcod", Types.DECIMAL);
		callableStatement.execute();

		valor = (BigDecimal) callableStatement.getObject("p_ultcod");
        return new BigDecimal((valor != null ? valor.longValue() : 0L) + 1L);
        
    }
   
}