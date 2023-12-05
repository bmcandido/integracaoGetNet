package br.com.oab.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.sankhya.util.TimeUtils;

import br.com.oab.dao.ChaveDAO;
import br.com.oab.dao.GetnetDAO;
import br.com.oab.dao.ParametrosDAO;
import br.com.oab.model.GetLinkModeoLinkFilther;
import br.com.oab.model.GetLinkModeoLinkFilther.GetNetPagamentoOnlineFilther;
import br.com.oab.model.GetnetLinkModel;
import br.com.oab.model.GetnetPmtOrderModel;
import br.com.oab.model.LinkOrigModel;
import br.com.oab.model.ParametrosModel;
import br.com.oab.util.Http;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class IntegracaoGetnet {

	private static Http.Response consultaPmtOrderGetnet(final String linkId, final ParametrosModel parametros,
			final String token) throws Exception {
		Http.Response response = null;

		try {

			final Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Bearer " + token);
			headers.put("seller_id", parametros.getSellerId());

			final Http httpArquivo = new Http(
					parametros.getBasePath() + linkId + "/payment-orders/OAB-GO-B2B-." + linkId + ".0");
			response = httpArquivo.get(headers);

		} catch (final IOException exception) {
			System.out.println("Payment Order do LinkId " + linkId + " não existe na Getnet.");

		}

		return response;

	}

	private static Http.Response consultaDadosLinkGetnet(final String linkId, final ParametrosModel parametros,
			final String token) throws Exception {
		Http.Response response = null;

		try {

			final Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Bearer " + token);
			headers.put("seller_id", parametros.getSellerId());

			final Http httpArquivo = new Http(parametros.getBasePath() + linkId);
			response = httpArquivo.get(headers);

		} catch (final IOException exception) {
			System.out.println("LinkId " + linkId + " não existe na Getnet.");

		}

		return response;

	}

	public void consultaLinksGetnet() throws Exception {

		System.out.println("inicio consulta consultaLinksGetnet");

		final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		GetnetDAO getnetDAO = new GetnetDAO();
		List<LinkOrigModel> linkIdList = getnetDAO.consultaLinksPendentes();

		if (linkIdList == null) {
			return;
		}

		// Consulta Parametros
		ParametrosDAO parDao = new ParametrosDAO();
		final ParametrosModel parametros;
		parametros = parDao.consultaParametros();

		// Busca novo Token
		BuscaTokenController tokenController = new BuscaTokenController();
		final String token = tokenController.checkToken(parametros);

		System.out.println("Token: " + token);

		for (final LinkOrigModel link : linkIdList) {

			System.out.println("Link atual: " + link.getLink());

			JapeSession.SessionHandle hnd = null;
			hnd = JapeSession.open();
			hnd.setCanTimeout(false);

			try {

				hnd.execWithTX(new JapeSession.TXBlock() {
					public void doWithTx() throws Exception {

						SimpleDateFormat dateFormatSQL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

						if (link.getLink() != null) {

							// Busca novo Token
							final Map<String, String> headers = new HashMap<>();
							headers.put("Authorization", "Bearer " + token);
							headers.put("seller_id", parametros.getSellerId());

							final Http http = new Http(parametros.getBasePath() + link.getLinkId());
							Http.Response response = http.get(headers);

							String respStr = new String(response.getData(), "utf-8");
							respStr = respStr.replace("\r", "");

							System.out.println("responseCode: " + response.getCode());
							System.out.println("respStr: " + respStr);

							if (response.getCode() >= 200 && response.getCode() <= 299) {

								Gson gson = new Gson();
								GetnetLinkModel LinkGetnet = gson.fromJson(respStr, GetnetLinkModel.class);

								System.out.println("Atribuiu gson Consulta do Link " + link.getLink());

								final DynamicVO dvoLink = (DynamicVO) entityFacade
										.getDefaultValueObjectInstance("AD_GTNLINK");

								dvoLink.setProperty("LINKID", link.getLinkId());
								dvoLink.setProperty("LABEL", LinkGetnet.getLabel());

								String strExpDate = LinkGetnet.getExpiration();

								Date parseDateExpira = dateFormatSQL.parse(strExpDate);
								Timestamp expDate = new Timestamp(parseDateExpira.getTime());

//								strExpDate = strExpDate.replace("T", " ");
//								strExpDate = strExpDate.substring(0, 19);
//								Date parsedDate = dateFormat.parse(strExpDate);
//								Timestamp expDate = new java.sql.Timestamp(parsedDate.getTime());
								dvoLink.setProperty("DTEXPIRA", expDate);
								dvoLink.setProperty("DTINTEGRACAO", TimeUtils.getNow());

								dvoLink.setProperty("STATUS", LinkGetnet.getStatus());
								dvoLink.setProperty("DESCRIPTION", LinkGetnet.getOrder().getDescription());
								dvoLink.setProperty("VALOR",
										new BigDecimal(LinkGetnet.getOrder().getAmount()).divide(new BigDecimal(100)));
								dvoLink.setProperty("MAXPARCELAS",
										new BigDecimal(LinkGetnet.getPayment().getCredit().getMaxInstallments()));
								dvoLink.setProperty("PAGSUCESSO", new BigDecimal(LinkGetnet.getSuccessfulOrders()));

								dvoLink.setProperty("ORIGEM", "ZY");

								if (LinkGetnet.getOrder().getOrderPrefix() != null) {
									dvoLink.setProperty("ORDERPREFIX", LinkGetnet.getOrder().getOrderPrefix());
								}
								String strOrderCreated = LinkGetnet.getCreatedAt();
//								strOrderCreated = strOrderCreated.replace("T", " ");
//								strOrderCreated = strOrderCreated.substring(0, 19);
//								Date parsedCreated = dateFormat.parse(strOrderCreated);
//								Timestamp createdDate = new java.sql.Timestamp(parsedCreated.getTime());

								Date dateRetParseCreate = dateFormatSQL.parse(strOrderCreated);

								Timestamp createdDate = new Timestamp(dateRetParseCreate.getTime());

								dvoLink.setProperty("DHCRIACAO", createdDate);

								if (LinkGetnet.getLastOrderAt() != null) {
									String strLastOrder = LinkGetnet.getLastOrderAt();
//									strLastOrder = strLastOrder.replace("T", " ");
//									strLastOrder = strLastOrder.substring(0, 19);
//									Date parsedLastOrder = dateFormat.parse(strLastOrder);
//									Timestamp lastDate = new java.sql.Timestamp(parsedLastOrder.getTime());

									Date dateRetParseLastOrder = dateFormatSQL.parse(strLastOrder);

									Timestamp createdDateLastOrder = new Timestamp(dateRetParseLastOrder.getTime());

									dvoLink.setProperty("DHALTERACAO", createdDateLastOrder);
								}

								dvoLink.setProperty("QTDACESSOS", new BigDecimal(LinkGetnet.getAccessCounter()));
//                                    dvoLink.setProperty("MD5", LinkGetnet.getHash());

								entityFacade.createEntity("AD_GTNLINK", (EntityVO) dvoLink);
								System.out.println("Criou registro AD_GTNLINK");

							}

						}

					} // tx
				});

			} catch (final Exception exception) {

				System.out.println("Entrou exception: " + exception.getMessage());
				exception.printStackTrace();
				StringWriter errors = new StringWriter();
				exception.printStackTrace(new PrintWriter(errors));

			} finally {
				JapeSession.close(hnd);

			}

		}

	}

	public boolean consultaLinksporFiltroGetnet(final Timestamp dataInicio, final Timestamp dataFim) throws Exception {

		System.out.println("inicio consulta consultaLinksporFiltroGetnet()");

		// Consulta Parametros
		ParametrosDAO parDao = new ParametrosDAO();
		final ParametrosModel parametros;
		parametros = parDao.consultaParametros();

		// Busca novo Token
		BuscaTokenController tokenController = new BuscaTokenController();
		final String token = tokenController.checkToken(parametros);

		System.out.println("Token: " + token);

		JapeSession.SessionHandle hnd = null;
		hnd = JapeSession.open();
		hnd.setCanTimeout(false);

		try {

			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {

					int pageNumber = 1;
					int maxPage = 50;

					do {

						SimpleDateFormat dateFormatSQL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						dateFormatSQL.setTimeZone(TimeZone.getTimeZone("UTC"));

						final Map<String, String> headers = new HashMap<>();

						headers.put("Authorization", "Bearer " + token);
						headers.put("seller_id", parametros.getSellerId());

						final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

						final String linkHttp = "https://api.getnet.com.br/v1/payment-links?created_since="
								+ dateFormat.format(dataInicio) + "&created_until=" + dateFormat.format(dataFim)
								+ "&limit=20&page=" + pageNumber;

						System.out.println("---------------------------------------------------------------");
						System.out.println("Link de Acesso a consultaLinksporFiltroGetnet() é " + linkHttp);
						System.out.println("Pagina Pesquisada consultaLinksporFiltroGetnet() é " + pageNumber);
						System.out.println("---------------------------------------------------------------");

						final Http http = new Http(linkHttp);
						Http.Response response = http.get(headers);

						String respStr = new String(response.getData(), "utf-8");
						respStr = respStr.replace("\r", "");

						System.out.println("responseCode: " + response.getCode());
						System.out.println("respStr: " + respStr);

						if (response.getCode() >= 200 && response.getCode() <= 299) {

							Gson gson = new Gson();
							GetLinkModeoLinkFilther linkGetnet = gson.fromJson(respStr, GetLinkModeoLinkFilther.class);

							System.out.println("Atribuiu gson Consulta do Link " + linkGetnet.getResult());

							if (!linkGetnet.getResult().isEmpty()) {

								// Verifica se existe Link

								for (GetNetPagamentoOnlineFilther retLinks : linkGetnet.getResult()) {

									if (retLinks.getSuccessfulOrders() > 0) {

										final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
										final JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();

										jdbcWrapper.openSession();

										final NativeSql nativeSql = new NativeSql(jdbcWrapper);
										nativeSql.appendSql("select count(1) CONTADOR   from AD_GTNLINK l\r\n"
												+ "where l.LINKID = '" + retLinks.getLinkId() + "'");

										final ResultSet resultSet = nativeSql.executeQuery();

										if (resultSet.next()) {

											if (resultSet.getBigDecimal("CONTADOR").compareTo(new BigDecimal(0)) == 0) {

												final DynamicVO dvoLink = (DynamicVO) entityFacade
														.getDefaultValueObjectInstance("AD_GTNLINK");

												dvoLink.setProperty("LINKID", retLinks.getLinkId());
												dvoLink.setProperty("LABEL", retLinks.getLabel());

												String strExpDate = retLinks.getExpiration();

												Date parseDateExpira = dateFormatSQL.parse(strExpDate);
												Timestamp expDate = new Timestamp(parseDateExpira.getTime());

												dvoLink.setProperty("DTEXPIRA", expDate);
												dvoLink.setProperty("DTINTEGRACAO", TimeUtils.getNow());
												dvoLink.setProperty("ORIGEM", "CF");

												dvoLink.setProperty("STATUS", retLinks.getStatus());
												dvoLink.setProperty("DESCRIPTION", retLinks.getTitle());
												dvoLink.setProperty("VALOR", new BigDecimal(retLinks.getAmount())
														.divide(new BigDecimal(100)));
												dvoLink.setProperty("MAXPARCELAS",
														new BigDecimal(retLinks.getMaxOrders()));
												dvoLink.setProperty("PAGSUCESSO",
														new BigDecimal(retLinks.getSuccessfulOrders()));

												String strOrderCreated = retLinks.getCreatedAt();
//											strOrderCreated = strOrderCreated.replace("T", " ");
//											strOrderCreated = strOrderCreated.substring(0, 19);
//											Date parsedCreated = dateFormat.parse(strOrderCreated);
//											Timestamp createdDate = new java.sql.Timestamp(parsedCreated.getTime());

												Date dateRetParse = dateFormatSQL.parse(strOrderCreated);

												Timestamp createdDate = new Timestamp(dateRetParse.getTime());

												dvoLink.setProperty("DHCRIACAO", createdDate);

												dvoLink.setProperty("QTDACESSOS",
														new BigDecimal(retLinks.getAccessCounter()));

												entityFacade.createEntity("AD_GTNLINK", (EntityVO) dvoLink);

												System.out.println(
														"---------------------------------------------------------------");
												System.out.println(
														"Criou o registro pela rotina criada na tabela AD_GTNLINK consultaLinksporFiltroGetnet()  de Nro: "
																+ retLinks.getLinkId());
												System.out.println(
														"---------------------------------------------------------------");

											} else {
												System.out.println(
														"Não encontrou registro na condição : resultSet.getBigDecimal(\"CONTADOR\").compareTo(new BigDecimal(0)) == 0");
											}

											JdbcWrapper.closeSession(jdbcWrapper);

										}

									} else {

										System.out.println("Não encontrou successful_orders > 0 para o registro "
												+ retLinks.getLinkId());
									}

								}

							}

						}

						pageNumber++;

					} while (pageNumber <= maxPage);

				} // tx
			});

		} catch (final Exception exception) {

			System.out.println("Entrou exception: " + exception.getMessage());
			exception.printStackTrace();
			StringWriter errors = new StringWriter();
			exception.printStackTrace(new PrintWriter(errors));

		} finally {
			JapeSession.close(hnd);

		}
		return false;

	}

	public void consultaOrdersGetnet() throws Exception {

		System.out.println("inicio consulta consultaOrdersGetnet");

		final EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		final JdbcWrapper jdbcWrapperChave = entityFacade.getJdbcWrapper();

		jdbcWrapperChave.openSession();
		// final ChaveDAO chaveDAO = new ChaveDAO(jdbcWrapperChave);

		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		GetnetDAO getnetDAO = new GetnetDAO();
		List<String> linkIdList = getnetDAO.consultaOrdersPendentes();

		if (linkIdList == null) {
			return;
		}

		// Consulta Parametros
		ParametrosDAO parDao = new ParametrosDAO();
		final ParametrosModel parametros;
		parametros = parDao.consultaParametros();

		// Busca novo Token
		BuscaTokenController tokenController = new BuscaTokenController();
		final String token = tokenController.checkToken(parametros);

		System.out.println("Token: " + token);

		for (final String link : linkIdList) {

			System.out.println("Link atual: " + link);

			JapeSession.SessionHandle hnd = null;
			hnd = JapeSession.open();
			hnd.setCanTimeout(false);

			try {

				hnd.execWithTX(new JapeSession.TXBlock() {
					public void doWithTx() throws Exception {

						if (link != null) {

							// Busca novo Token
							final Map<String, String> headers = new HashMap<>();
							headers.put("Authorization", "Bearer " + token);
							headers.put("seller_id", parametros.getSellerId());

							final Http http = new Http(
									parametros.getBasePath() + link + "/payment-orders/OAB-GO-B2B-." + link + ".0");
							Http.Response response = http.get(headers);

							String respStr = new String(response.getData(), "utf-8");
							respStr = respStr.replace("\r", "");

							System.out.println("responseCode: " + response.getCode());
							System.out.println("respStr: " + respStr);

							if (response.getCode() >= 200 && response.getCode() <= 299) {

								Gson gson = new Gson();
								GetnetPmtOrderModel PmtGetnet = gson.fromJson(respStr, GetnetPmtOrderModel.class);

								System.out.println("Atribuiu gson Consulta do Pagamento " + link);

								// TABLE AD_GTNPMTORD

								if (PmtGetnet.getPayments() != null) {

									List<GetnetPmtOrderModel.PaymentsGTN> regPayments = PmtGetnet.getPayments();

									for (final GetnetPmtOrderModel.PaymentsGTN registroPag : regPayments) {

										final DynamicVO dvoOrd = (DynamicVO) entityFacade
												.getDefaultValueObjectInstance("AD_GTNPMTORD");

										// SEQ --> PEGAR NOVA SEQUENCIA VIA CLASSE ChaveDAO.CARREGAR (acho que vai gerar
										// automaticamente por ser auto-incremento)
										ChaveDAO chaveDAO = new ChaveDAO(jdbcWrapperChave);
										BigDecimal seq = chaveDAO.carregaOrdSeq(PmtGetnet.getLinkId(),
												PmtGetnet.getPaymentOrderId());

										dvoOrd.setProperty("LINKID", PmtGetnet.getLinkId());
										dvoOrd.setProperty("PMT_ORDERID", PmtGetnet.getPaymentOrderId());
										dvoOrd.setProperty("SEQ", seq);

										dvoOrd.setProperty("PMT_STATUS", registroPag.getStatus());
										dvoOrd.setProperty("PMT_VALOR",
												new BigDecimal(PmtGetnet.getAmount()).divide(new BigDecimal(100)));

										String strCreatedAt = PmtGetnet.getCreatedAt();
										strCreatedAt = strCreatedAt.replace("T", " ");
										strCreatedAt = strCreatedAt.substring(0, 19);
										Date parsedCreated = dateFormat.parse(strCreatedAt);
										Timestamp createdDate = new java.sql.Timestamp(parsedCreated.getTime());
										dvoOrd.setProperty("PMT_DHCRIACAO", createdDate);

										// INTERAÇÃO PARA GRAVAR UMA OU MAIS TRANSAÇÕES.
										dvoOrd.setProperty("TRN_TRNTDHREG", registroPag.getRegistredAt());
										dvoOrd.setProperty("TRN_PMTTYPE", registroPag.getPaymentType()); // payments.payment_type
										dvoOrd.setProperty("TRN_PMTID", registroPag.getPaymentId()); // -->
										dvoOrd.setProperty("TRN_STATUS", registroPag.getTransaction().getStatus());

										if (registroPag.getTransaction().getAmount() != null) {
											dvoOrd.setProperty("TRN_VALOR",
													new BigDecimal(registroPag.getTransaction().getAmount())
															.divide(new BigDecimal(100)));

										}

										if (registroPag.getTransaction() != null
												&& registroPag.getTransaction().getReceivedAt() != null) {
											String strDhReceb = registroPag.getTransaction().getReceivedAt();
											strDhReceb = strDhReceb.replace("T", " ");
											strDhReceb = strDhReceb.substring(0, 19);
											Date parsedReceb = dateFormat.parse(strDhReceb);
											Timestamp receb_at = new java.sql.Timestamp(parsedReceb.getTime());
											dvoOrd.setProperty("TRN_DHRECEB", receb_at);
										}

										if (!registroPag.getPaymentType().equals("PIX")) {

											if (registroPag.getStatus().equals("SUCCESSFUL")) {

												if (registroPag.getPaymentType().equals("DEBIT")) {

													if (registroPag.getNumberInstallments() != null) {
														dvoOrd.setProperty("TRN_NUMPARC", BigDecimal.valueOf(1)); //

													}

													if (registroPag.getTransaction().getAuthorizationCode() != null) {
														dvoOrd.setProperty("TRN_AUTHCODE",
																registroPag.getTransaction().getAuthorizationCode());
													}

													if (registroPag.getTransaction().getReasonCode() != null) {
														dvoOrd.setProperty("TRN_REASONCODE",
																registroPag.getTransaction().getReasonCode());
													}

													if (registroPag.getTransaction().getBrand() != null) {
														dvoOrd.setProperty("TRN_BRAND",
																registroPag.getTransaction().getBrand());

													}

													if (registroPag.getTransaction().getAcquirerTransactionId() != null

															&& !registroPag.getTransaction().getAuthorizationCode()
																	.isEmpty()) {
														dvoOrd.setProperty("TRN_TERMINALNSU",
																registroPag.getTransaction().getAuthorizationCode());
													}

													if (registroPag.getTransaction().getAcquirerTransactionId() != null

													) {
														dvoOrd.setProperty("TRN_ACQTRANSID", registroPag
																.getTransaction().getAcquirerTransactionId());

													}

													if (registroPag.getTransaction().getTransactionId() != null) {

														dvoOrd.setProperty("TRN_TRANSID",
																registroPag.getTransaction().getTransactionId());

													}

													if (registroPag.getTransaction().getReasonMessage() != null
															&& !registroPag.getTransaction().getReasonMessage()
																	.isEmpty()) {

														dvoOrd.setProperty("TRN_REASONMSG",
																registroPag.getTransaction().getReasonMessage());

													}

												}

												if (registroPag.getPaymentType().equals("CREDIT")) {

													if (registroPag.getNumberInstallments() != null) {
														dvoOrd.setProperty("TRN_NUMPARC", BigDecimal
																.valueOf(registroPag.getNumberInstallments())); //

													}

													if (registroPag.getTransactionType() != null) {
														dvoOrd.setProperty("TRN_TRANSTYPE",
																registroPag.getTransactionType());
													}

													if (registroPag.getTransaction().getCredit()
															.getAuthorizationCode() != null) {
														dvoOrd.setProperty("TRN_AUTHCODE", registroPag.getTransaction()
																.getCredit().getAuthorizationCode());
													}

													if (registroPag.getTransaction().getCredit()
															.getReasonCode() != null) {
														dvoOrd.setProperty("TRN_REASONCODE", registroPag
																.getTransaction().getCredit().getReasonCode());
													}

													if (registroPag.getTransaction().getCredit().getBrand() != null) {
														dvoOrd.setProperty("TRN_BRAND",
																registroPag.getTransaction().getCredit().getBrand());

													}

													if (registroPag.getTransaction().getCredit()
															.getTerminalNsu() != null

															&& !registroPag.getTransaction().getCredit()
																	.getTerminalNsu().isEmpty()) {
														dvoOrd.setProperty("TRN_TERMINALNSU", registroPag
																.getTransaction().getCredit().getTerminalNsu());
													}

													if (registroPag.getTransaction().getCredit()
															.getAcquirerTransactionId() != null

													) {
														dvoOrd.setProperty("TRN_ACQTRANSID",
																registroPag.getTransaction().getCredit()
																		.getAcquirerTransactionId());

													}

													if (registroPag.getTransaction().getCredit()
															.getTransactionId() != null) {

														dvoOrd.setProperty("TRN_TRANSID", registroPag.getTransaction()
																.getCredit().getTransactionId());

													}

													if (registroPag.getTransaction().getCredit()
															.getReasonMessage() != null
															&& !registroPag.getTransaction().getCredit()
																	.getReasonMessage().isEmpty()) {

														dvoOrd.setProperty("TRN_REASONMSG", registroPag.getTransaction()
																.getCredit().getReasonMessage());

													}

												}

											}

										}

										entityFacade.createEntity("AD_GTNPMTORD", (EntityVO) dvoOrd);

									}

									// AO FINAL, ENCONTRAR O LINKID EM AD_GTNLINK E DAR UPDATE
									PersistentLocalEntity entityArqVO = entityFacade
											.findEntityByPrimaryKey("AD_GTNLINK", link);

									EntityVO vo = entityArqVO.getValueObject();
									DynamicVO registroVo = (DynamicVO) vo;

									registroVo.setProperty("EMAIL", PmtGetnet.getCustomer().getEmail().toLowerCase());
									registroVo.setProperty("NOME",
											PmtGetnet.getCustomer().getFirstName().toUpperCase());
									registroVo.setProperty("SOBRENOME",
											PmtGetnet.getCustomer().getLastName().toUpperCase());
									registroVo.setProperty("NOMECOMPLETO",
											PmtGetnet.getCustomer().getFirstName().toUpperCase() + " "
													+ PmtGetnet.getCustomer().getLastName().toUpperCase());
									registroVo.setProperty("DOCNUMBER", PmtGetnet.getCustomer().getDocumentNumber());
									registroVo.setProperty("DOCTYPE",
											PmtGetnet.getCustomer().getDocumentType().toUpperCase());

									entityArqVO.setValueObject(vo);

								}

							}

						}

					} // tx
				});

			} catch (final Exception exception) {

				System.out.println("Entrou exception: " + exception.getMessage());
				exception.printStackTrace();
				StringWriter errors = new StringWriter();
				exception.printStackTrace(new PrintWriter(errors));

			} finally {
				JapeSession.close(hnd);

			}

		}

		JdbcWrapper.closeSession(jdbcWrapperChave);

	}

}
