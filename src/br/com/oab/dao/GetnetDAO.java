package br.com.oab.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.oab.model.LinkOrigModel;

public class GetnetDAO {

	public List<LinkOrigModel> consultaLinksPendentes() throws Exception {

		System.out.println("Entrou consultaIdLotePorIdImvScod");
		List<BigDecimal> lteCodigoList = new ArrayList<>();

		final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

		jdbcWrapper.openSession();
		List<LinkOrigModel> ListalinkOrig = new ArrayList<LinkOrigModel>();

		final NativeSql nativeSql = new NativeSql(jdbcWrapper);
		nativeSql.appendSql("SELECT ori.NURENEG, ori.LINKID, ori.link, ori.NUFIN\n" +
				"FROM AD_GETNETPAYMENTLINK ori\n" +
				"WHERE NOT EXISTS (\n" +
				"    SELECT 1\n" +
				"    FROM AD_GTNLINK L\n" +
				"    WHERE L.LINKID = ori.LINKID\n" +
				"    AND L.PAGSUCESSO > 0\n" +
				")\n" +
				"\n" +
				"AND ori.DHCRIACAO BETWEEN DATEADD(DAY, -3, GETDATE()) AND GETDATE()");

		final ResultSet resultSet = nativeSql.executeQuery();
		while (resultSet.next()) {

			LinkOrigModel linkOrig = new LinkOrigModel();
			linkOrig.setNuReneg(resultSet.getBigDecimal("NURENEG"));
			linkOrig.setLinkId(resultSet.getString("LINKID"));
			linkOrig.setLink(resultSet.getString("LINK"));
			ListalinkOrig.add(linkOrig);

		}
		JdbcWrapper.closeSession(jdbcWrapper);

		return ListalinkOrig;

	}

	public List<String> consultaOrdersPendentes() throws Exception {

		System.out.println("Entrou consultaOrdersPendentes");
		List<BigDecimal> lteCodigoList = new ArrayList<>();

		final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

		jdbcWrapper.openSession();
		List<String> ListalinkOrig = new ArrayList<String>();

		final NativeSql nativeSql = new NativeSql(jdbcWrapper);
		nativeSql.appendSql("select l.LINKID\r\n"
				+ "from AD_GTNLINK l\r\n"
				+ "where l.PAGSUCESSO > 0\r\n"
				+ "  and not exists (select 1 from AD_GTNPMTORD ord where ord.LINKID = l.LINKID and ord.PMT_STATUS in ('SUCCESSFUL'))\r\n"
				+ "  And CONVERT(DATE, l.DHCRIACAO) BETWEEN CONVERT(DATE, DATEADD(DAY, -3, GETDATE())) AND CONVERT(DATE, GETDATE())\r\n"
				+ "order by l.DHCRIACAO");

		final ResultSet resultSet = nativeSql.executeQuery();
		while (resultSet.next()) {
			ListalinkOrig.add(resultSet.getString("LINKID"));
		}
		JdbcWrapper.closeSession(jdbcWrapper);

		return ListalinkOrig;

	}


}
