package br.com.oab.events;

import java.math.BigDecimal;
import java.sql.CallableStatement;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class GeraRateioKits implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent contexto) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterInsert(PersistenceEvent contexto) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterUpdate(PersistenceEvent contexto) throws Exception {
		
		


		//if (contexto.getModifingFields().isModifing("STATUSNOTA") == true) {
			
			System.out.println("-------------------------------------------------");
			System.out.println("Entrou no objeto GeraRateioKits na condição DE ALTAR O STATUSNOTA" );
			System.out.println("-------------------------------------------------");


			SessionHandle hnd = null;
			JdbcWrapper jdbc = null;

			DynamicVO newVO = (DynamicVO) contexto.getVo();
			// DynamicVO oldVO = (DynamicVO)contexto.getOldVO();

			// Entra se estiver confirmada

			if ("L".equals(newVO.asString("STATUSNOTA"))) {
				
				System.out.println("-------------------------------------------------");
				System.out.println("Entrou no objeto GeraRateioKits na condição STATUSNOTA = L" );
				System.out.println("-------------------------------------------------");

				try {

					final BigDecimal nunota = newVO.asBigDecimal("NUNOTA");

					hnd = JapeSession.open();

					JapeWrapper rateioDao = JapeFactory.dao("RateioRecDesp");

					rateioDao.deleteByCriteria("NUFIN = ?", nunota);

					EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

					jdbc = dwfFacade.getJdbcWrapper();
					jdbc.openSession();

					CallableStatement cstmt = jdbc.getConnection().prepareCall("{call STP_GERATEIONOTASKITS(?)}");
					cstmt.setQueryTimeout(60);

					cstmt.setBigDecimal(1, nunota);

					cstmt.execute();

				} catch (Exception e) {
					MGEModelException.throwMe(e);

				} finally {
					JdbcWrapper.closeSession(jdbc);
					JapeSession.close(hnd);

				}

			}

		//}

	}

	@Override
	public void beforeCommit(TransactionContext contexto) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDelete(PersistenceEvent contexto) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeInsert(PersistenceEvent contexto) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeUpdate(PersistenceEvent contexto) throws Exception {
		// TODO Auto-generated method stub

	}

}

/*

CREATE PROCEDURE "STP_GERATEIONOTASKITS"(@NUNOTAPARAM INT) AS
BEGIN
    DECLARE @NURENEG INT,
            @NUFIN INT,
            @VLRTOTAL FLOAT,
            @VLRDESDOBRAT FLOAT,
            @CODNAT INT,
            @CODCENCUS INT,
            @CODPARC INT,
            @QTDTITULOSRECDESP1 INT,
            @SOMATORIAPARAVALIDACAO FLOAT,
            @SEQUENCIALOG INT,
            @HOUVEEXCLUSAO INT,
            @CODBR INT,
            @ORIGEM VARCHAR(MAX),
            @NUNOTA INT,
            @VLRTOTALITE FLOAT,
            @VLRDESDOBTITNOTA FLOAT,
            @CODPROJ INT,
            @VLRDESDOBFIN FLOAT;


    DECLARE CUR_NOTA_RENEG CURSOR FOR 
  SELECT I.NUNOTA,
       CASE WHEN U.CODNAT > 0 THEN U.CODNAT ELSE  P.CODNAT END CODNAT,
       B.CODCENCUS,
       B.CODPROJ,
       SUM(I.VLRTOT) VLRTOT,
       B.CODPARC
  FROM TGFITE I
 INNER JOIN TGFPRO P
    ON P.CODPROD = I.CODPROD
 INNER JOIN TGFGRU U
    ON U.CODGRUPOPROD = P.CODGRUPOPROD
 INNER JOIN TGFCAB B
    ON B.NUNOTA = I.NUNOTA
 WHERE NOT EXISTS (SELECT 1 FROM TGFICP IC WHERE IC.CODPROD = I.CODPROD)
   AND B.TIPMOV = 'V'
   AND B.STATUSNOTA = 'L'
   AND B.CODEMP = 1
   --AND NOT EXISTS (SELECT 1 FROM TGFRAT T WHERE T.NUFIN = B.NUNOTA)
   AND B.NUNOTA = @NUNOTAPARAM 
 GROUP BY I.NUNOTA, 
          P.CODNAT, 
          U.CODNAT, 
          B.CODCENCUS,
          B.CODPROJ, 
          B.CODPARC
       
      OPEN CUR_NOTA_RENEG;

       SET NOCOUNT ON;

      FETCH NEXT FROM CUR_NOTA_RENEG INTO @NUNOTA, @CODNAT, @CODCENCUS, @CODPROJ, @VLRDESDOBRAT, @CODPARC;
        WHILE (@@FETCH_STATUS = 0)
        BEGIN
       
         --VALOR TOTAL DA NOTA
         --PARA PROPORCIONALIZAR NO FINANCEIRO, TALVEZ PODERÁ SER MAIS DE UM FINANCEIRO
         --LOGO BUSCAMOS ESTE VALOR PARA FAZER A PROPORCAO
SELECT @VLRTOTAL  = B.VLRNOTA
  FROM TGFCAB B
 WHERE B.NUNOTA = @NUNOTA

   
   
     DECLARE CUR_RATEIO_FIN_NOTA CURSOR FOR
            SELECT F.NUFIN, F.VLRDESDOB, F.ORIGEM
            FROM TGFFIN F
            WHERE F.NUNOTA = @NUNOTA
            AND F.RECDESP = 1
            AND F.ORIGEM = 'E'
              OPEN CUR_RATEIO_FIN_NOTA;

        SET NOCOUNT ON;

        FETCH NEXT FROM CUR_RATEIO_FIN_NOTA INTO @NUFIN, @VLRDESDOBFIN,  @ORIGEM;

        WHILE (@@FETCH_STATUS = 0)
        BEGIN
      
       --CRIADO POIS PODE SER QUE O VALOR É PARCELADO
      IF NOT EXISTS (SELECT 1 FROM TGFRAT T WHERE T.CODNAT =  @CODNAT AND T.CODCENCUS = @CODCENCUS AND T.CODPROJ =   @CODPROJ AND T.NUFIN = @NUNOTAPARAM  )
      BEGIN
           
                    INSERT INTO TGFRAT (ORIGEM,
                                        NUFIN,
                                        CODNAT,
                                        CODCENCUS,
                                        CODPROJ,
                                        PERCRATEIO,
                                        NUMCONTRATO,
                                        DIGITADO,
                                        CODSITE,
                                        CODPARC,
                                        CODUSU,
                                        DTALTER)
                    VALUES (@ORIGEM,
                            @NUNOTA,
                            @CODNAT,
                            @CODCENCUS,
                            0,
                            (@VLRDESDOBRAT /  @VLRTOTAL * 100),
                            0,
                            'S',
                            0,
                            @CODPARC,
                            0,
                            GETDATE());

       END
            FETCH NEXT FROM CUR_RATEIO_FIN_NOTA INTO @NUFIN, @VLRDESDOBFIN,  @ORIGEM;
        END;
  
      CLOSE CUR_RATEIO_FIN_NOTA;
      DEALLOCATE CUR_RATEIO_FIN_NOTA; 
     
    
               UPDATE TGFFIN SET RATEADO ='S' WHERE NUNOTA =  @NUNOTA
                
            
               UPDATE TGFCAB SET RATEADO = 'S' WHERE NUNOTA = @NUNOTA
            
        
        FETCH NEXT FROM CUR_NOTA_RENEG INTO @NUNOTA, @CODNAT, @CODCENCUS, @CODPROJ, @VLRDESDOBRAT, @CODPARC;
        END;
  
      CLOSE CUR_NOTA_RENEG;
      DEALLOCATE CUR_NOTA_RENEG; 
   
   
END;
 */
