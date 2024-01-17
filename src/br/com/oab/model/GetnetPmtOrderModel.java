package br.com.oab.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GetnetPmtOrderModel {

	@SerializedName("created_at")
	private String createdAt;
	@SerializedName("seller_id")
	private String sellerId;
	@SerializedName("link_id")
	private String linkId;
	@SerializedName("shipping_amount")
	private Integer shippingAmount;
	@SerializedName("amount")
	private Integer amount;
	@SerializedName("status")
	private String status;
	@SerializedName("payment_order_id")
	private String paymentOrderId;
	@SerializedName("quantity")
	private Integer quantity;
	@SerializedName("customer")
	private CustomerGTN customer;
	@SerializedName("shippings")
	private ShippingsGTN shippings;
	@SerializedName("payments")
	private List<PaymentsGTN> payments;
	@SerializedName("updated_at")
	private String updatedAt;

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public Integer getShippingAmount() {
		return shippingAmount;
	}

	public void setShippingAmount(Integer shippingAmount) {
		this.shippingAmount = shippingAmount;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentOrderId() {
		return paymentOrderId;
	}

	public void setPaymentOrderId(String paymentOrderId) {
		this.paymentOrderId = paymentOrderId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public CustomerGTN getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerGTN customer) {
		this.customer = customer;
	}

	public ShippingsGTN getShippings() {
		return shippings;
	}

	public void setShippings(ShippingsGTN shippings) {
		this.shippings = shippings;
	}

	public List<PaymentsGTN> getPayments() {
		return payments;
	}

	public void setPayments(List<PaymentsGTN> payments) {
		this.payments = payments;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public static class CustomerGTN {
		@SerializedName("first_name")
		private String firstName;
		@SerializedName("last_name")
		private String lastName;
		@SerializedName("date_of_birth")
		private String dateOfBirth;
		@SerializedName("email")
		private String email;
		@SerializedName("document_type")
		private String documentType;
		@SerializedName("document_number")
		private String documentNumber;
		@SerializedName("phone_number")
		private String phoneNumber;
		@SerializedName("opt_in")
		private Boolean optIn;
		@SerializedName("billing_address")
		private BillingAddressGTN billingAddress;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getDateOfBirth() {
			return dateOfBirth;
		}

		public void setDateOfBirth(String dateOfBirth) {
			this.dateOfBirth = dateOfBirth;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getDocumentType() {
			return documentType;
		}

		public void setDocumentType(String documentType) {
			this.documentType = documentType;
		}

		public String getDocumentNumber() {
			return documentNumber;
		}

		public void setDocumentNumber(String documentNumber) {
			this.documentNumber = documentNumber;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public Boolean getOptIn() {
			return optIn;
		}

		public void setOptIn(Boolean optIn) {
			this.optIn = optIn;
		}

		public BillingAddressGTN getBillingAddress() {
			return billingAddress;
		}

		public void setBillingAddress(BillingAddressGTN billingAddress) {
			this.billingAddress = billingAddress;
		}

		public static class BillingAddressGTN {
			@SerializedName("postal_code")
			private String postalCode;
			@SerializedName("street")
			private String street;
			@SerializedName("number")
			private String number;
			@SerializedName("district")
			private String district;
			@SerializedName("city")
			private String city;
			@SerializedName("state")
			private String state;
			@SerializedName("country")
			private String country;

			public String getPostalCode() {
				return postalCode;
			}

			public void setPostalCode(String postalCode) {
				this.postalCode = postalCode;
			}

			public String getStreet() {
				return street;
			}

			public void setStreet(String street) {
				this.street = street;
			}

			public String getNumber() {
				return number;
			}

			public void setNumber(String number) {
				this.number = number;
			}

			public String getDistrict() {
				return district;
			}

			public void setDistrict(String district) {
				this.district = district;
			}

			public String getCity() {
				return city;
			}

			public void setCity(String city) {
				this.city = city;
			}

			public String getState() {
				return state;
			}

			public void setState(String state) {
				this.state = state;
			}

			public String getCountry() {
				return country;
			}

			public void setCountry(String country) {
				this.country = country;
			}
		}
	}

	public static class ShippingsGTN {
		@SerializedName("address")
		private AddressGTN address;
		@SerializedName("same_as_billing_address")
		private Boolean sameAsBillingAddress;

		public AddressGTN getAddress() {
			return address;
		}

		public void setAddress(AddressGTN address) {
			this.address = address;
		}

		public Boolean getSameAsBillingAddress() {
			return sameAsBillingAddress;
		}

		public void setSameAsBillingAddress(Boolean sameAsBillingAddress) {
			this.sameAsBillingAddress = sameAsBillingAddress;
		}

		public static class AddressGTN {
			@SerializedName("postal_code")
			private String postalCode;
			@SerializedName("street")
			private String street;
			@SerializedName("number")
			private String number;
			@SerializedName("district")
			private String district;
			@SerializedName("city")
			private String city;
			@SerializedName("state")
			private String state;
			@SerializedName("country")
			private String country;

			public String getPostalCode() {
				return postalCode;
			}

			public void setPostalCode(String postalCode) {
				this.postalCode = postalCode;
			}

			public String getStreet() {
				return street;
			}

			public void setStreet(String street) {
				this.street = street;
			}

			public String getNumber() {
				return number;
			}

			public void setNumber(String number) {
				this.number = number;
			}

			public String getDistrict() {
				return district;
			}

			public void setDistrict(String district) {
				this.district = district;
			}

			public String getCity() {
				return city;
			}

			public void setCity(String city) {
				this.city = city;
			}

			public String getState() {
				return state;
			}

			public void setState(String state) {
				this.state = state;
			}

			public String getCountry() {
				return country;
			}

			public void setCountry(String country) {
				this.country = country;
			}
		}
	}

	public static class PaymentsGTN {
		@SerializedName("registred_at")
		private String registredAt;
		@SerializedName("status")
		private String status;
		@SerializedName("payment_type")
		private String paymentType;
		@SerializedName("amount")
		private Integer amount;
		@SerializedName("payment_id")
		private String paymentId;
		@SerializedName("number_installments")
		private Integer numberInstallments;
		@SerializedName("transaction_type")
		private String transactionType;
		@SerializedName("transaction")
		private TransactionGTN transaction;
		@SerializedName("wallet")
		private Boolean wallet;

		public String getRegistredAt() {
			return registredAt;
		}

		public void setRegistredAt(String registredAt) {
			this.registredAt = registredAt;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getPaymentType() {
			return paymentType;
		}

		public void setPaymentType(String paymentType) {
			this.paymentType = paymentType;
		}

		public Integer getAmount() {
			return amount;
		}

		public void setAmount(Integer amount) {
			this.amount = amount;
		}

		public String getPaymentId() {
			return paymentId;
		}

		public void setPaymentId(String paymentId) {
			this.paymentId = paymentId;
		}

		public Integer getNumberInstallments() {
			return numberInstallments;
		}

		public void setNumberInstallments(Integer numberInstallments) {
			this.numberInstallments = numberInstallments;
		}

		public String getTransactionType() {
			return transactionType;
		}

		public void setTransactionType(String transactionType) {
			this.transactionType = transactionType;
		}

		public TransactionGTN getTransaction() {
			return transaction;
		}

		public void setTransaction(TransactionGTN transaction) {
			this.transaction = transaction;
		}

		public Boolean getWallet() {
			return wallet;
		}

		public void setWallet(Boolean wallet) {
			this.wallet = wallet;
		}

		public static class TransactionGTN {

			@SerializedName("seller_id")
			private String sellerId;
			@SerializedName("amount")
			private Integer amount;
			@SerializedName("currency")
			private String currency;
			@SerializedName("order_id")
			private String orderId;
			@SerializedName("status")
			private String status;
			@SerializedName("payment_id")
			private String paymentId;
			@SerializedName("received_at")
			private String receivedAt;
			@SerializedName("payment_method")
			private String paymentMethod;
			@SerializedName("transaction_id")
			private String transactionId;
			@SerializedName("original_transaction_id")
			private String originalTransactionId;
			@SerializedName("authorized_at")
			private String authorizedAt;
			@SerializedName("reason_code")
			private String reasonCode;
			@SerializedName("reason_message")
			private String reasonMessage;
			@SerializedName("acquirer")
			private String acquirer;
			@SerializedName("brand")
			private String brand;
			@SerializedName("authorization_code")
			private String authorizationCode;
			@SerializedName("acquirer_transaction_id")
			private String acquirerTransactionId;
			@SerializedName("eci")
			private String eci;
			@SerializedName("brand_type")
			private String brandType;
			@SerializedName("credit")
			private CreditGTN credit;
			@SerializedName("debit")
			private DebitGNT debit;

			public String getPaymentMethod() {
				return paymentMethod;
			}

			public void setPaymentMethod(String paymentMethod) {
				this.paymentMethod = paymentMethod;
			}

			public String getTransactionId() {
				return transactionId;
			}

			public void setTransactionId(String transactionId) {
				this.transactionId = transactionId;
			}

			public String getOriginalTransactionId() {
				return originalTransactionId;
			}

			public void setOriginalTransactionId(String originalTransactionId) {
				this.originalTransactionId = originalTransactionId;
			}

			public String getAuthorizedAt() {
				return authorizedAt;
			}

			public void setAuthorizedAt(String authorizedAt) {
				this.authorizedAt = authorizedAt;
			}

			public String getReasonCode() {
				return reasonCode;
			}

			public void setReasonCode(String reasonCode) {
				this.reasonCode = reasonCode;
			}

			public String getReasonMessage() {
				return reasonMessage;
			}

			public void setReasonMessage(String reasonMessage) {
				this.reasonMessage = reasonMessage;
			}

			public String getAcquirer() {
				return acquirer;
			}

			public void setAcquirer(String acquirer) {
				this.acquirer = acquirer;
			}

			public String getBrand() {
				return brand;
			}

			public void setBrand(String brand) {
				this.brand = brand;
			}

			public String getAuthorizationCode() {
				return authorizationCode;
			}

			public void setAuthorizationCode(String authorizationCode) {
				this.authorizationCode = authorizationCode;
			}

			public String getAcquirerTransactionId() {
				return acquirerTransactionId;
			}

			public void setAcquirerTransactionId(String acquirerTransactionId) {
				this.acquirerTransactionId = acquirerTransactionId;
			}

			public String getEci() {
				return eci;
			}

			public void setEci(String eci) {
				this.eci = eci;
			}

			public String getBrandType() {
				return brandType;
			}

			public void setBrandType(String brandType) {
				this.brandType = brandType;
			}

			public String getSellerId() {
				return sellerId;
			}

			public void setSellerId(String sellerId) {
				this.sellerId = sellerId;
			}

			public Integer getAmount() {
				return amount;
			}

			public void setAmount(Integer amount) {
				this.amount = amount;
			}

			public String getCurrency() {
				return currency;
			}

			public void setCurrency(String currency) {
				this.currency = currency;
			}

			public String getOrderId() {
				return orderId;
			}

			public void setOrderId(String orderId) {
				this.orderId = orderId;
			}

			public String getStatus() {
				return status;
			}

			public void setStatus(String status) {
				this.status = status;
			}

			public String getPaymentId() {
				return paymentId;
			}

			public void setPaymentId(String paymentId) {
				this.paymentId = paymentId;
			}

			public String getReceivedAt() {
				return receivedAt;
			}

			public void setReceivedAt(String receivedAt) {
				this.receivedAt = receivedAt;
			}

			public CreditGTN getCredit() {
				return credit;
			}

			public void setCredit(CreditGTN credit) {
				this.credit = credit;
			}

			public DebitGNT getDebit() {
				return debit;
			}

			public void setDebit(DebitGNT debit) {
				this.debit = debit;
			}

			public static class CreditGTN {
				@SerializedName("delayed")
				private Boolean delayed;
				@SerializedName("authorization_code")
				private String authorizationCode;
				@SerializedName("authorized_at")
				private String authorizedAt;
				@SerializedName("reason_code")
				private String reasonCode;
				@SerializedName("reason_message")
				private String reasonMessage;
				@SerializedName("acquirer")
				private String acquirer;
				@SerializedName("soft_descriptor")
				private String softDescriptor;
				@SerializedName("brand")
				private String brand;
				@SerializedName("terminal_nsu")
				private String terminalNsu;
				@SerializedName("acquirer_transaction_id")
				private String acquirerTransactionId;
				@SerializedName("transaction_id")
				private String transactionId;

				public Boolean getDelayed() {
					return delayed;
				}

				public void setDelayed(Boolean delayed) {
					this.delayed = delayed;
				}

				public String getAuthorizationCode() {
					return authorizationCode;
				}

				public void setAuthorizationCode(String authorizationCode) {
					this.authorizationCode = authorizationCode;
				}

				public String getAuthorizedAt() {
					return authorizedAt;
				}

				public void setAuthorizedAt(String authorizedAt) {
					this.authorizedAt = authorizedAt;
				}

				public String getReasonCode() {
					return reasonCode;
				}

				public void setReasonCode(String reasonCode) {
					this.reasonCode = reasonCode;
				}

				public String getReasonMessage() {
					return reasonMessage;
				}

				public void setReasonMessage(String reasonMessage) {
					this.reasonMessage = reasonMessage;
				}

				public String getAcquirer() {
					return acquirer;
				}

				public void setAcquirer(String acquirer) {
					this.acquirer = acquirer;
				}

				public String getSoftDescriptor() {
					return softDescriptor;
				}

				public void setSoftDescriptor(String softDescriptor) {
					this.softDescriptor = softDescriptor;
				}

				public String getBrand() {
					return brand;
				}

				public void setBrand(String brand) {
					this.brand = brand;
				}

				public String getTerminalNsu() {
					return terminalNsu;
				}

				public void setTerminalNsu(String terminalNsu) {
					this.terminalNsu = terminalNsu;
				}

				public String getAcquirerTransactionId() {
					return acquirerTransactionId;
				}

				public void setAcquirerTransactionId(String acquirerTransactionId) {
					this.acquirerTransactionId = acquirerTransactionId;
				}

				public String getTransactionId() {
					return transactionId;
				}

				public void setTransactionId(String transactionId) {
					this.transactionId = transactionId;
				}
			}

			public static class DebitGNT {

				@SerializedName("authorization_code")
				private String authorizationCode;
				@SerializedName("authorized_timestamp")
				private String authorizedtimestamp;
				@SerializedName("reason_code")
				private String reasonCode;
				@SerializedName("reason_message")
				private String reasonMessage;
				@SerializedName("acquirer")
				private String acquirer;
				@SerializedName("soft_descriptor")
				private String softDescriptor;
				@SerializedName("brand")
				private String brand;
				@SerializedName("terminal_nsu")
				private String terminalNsu;
				@SerializedName("acquirer_transaction_id")
				private String acquirerTransactionId;
				@SerializedName("transaction_id")
				private String transactionId;
				

				public String getAuthorizationCode() {
					return authorizationCode;
				}

				public void setAuthorizationCode(String authorizationCode) {
					this.authorizationCode = authorizationCode;
				}

				public String getAuthorizedtimestamp() {
					return authorizedtimestamp;
				}

				public void setAuthorizedtimestamp(String authorizedtimestamp) {
					this.authorizedtimestamp = authorizedtimestamp;
				}

				public String getReasonCode() {
					return reasonCode;
				}

				public void setReasonCode(String reasonCode) {
					this.reasonCode = reasonCode;
				}

				public String getReasonMessage() {
					return reasonMessage;
				}

				public void setReasonMessage(String reasonMessage) {
					this.reasonMessage = reasonMessage;
				}

				public String getAcquirer() {
					return acquirer;
				}

				public void setAcquirer(String acquirer) {
					this.acquirer = acquirer;
				}

				public String getSoftDescriptor() {
					return softDescriptor;
				}

				public void setSoftDescriptor(String softDescriptor) {
					this.softDescriptor = softDescriptor;
				}

				public String getBrand() {
					return brand;
				}

				public void setBrand(String brand) {
					this.brand = brand;
				}

				public String getTerminalNsu() {
					return terminalNsu;
				}

				public void setTerminalNsu(String terminalNsu) {
					this.terminalNsu = terminalNsu;
				}

				public String getAcquirerTransactionId() {
					return acquirerTransactionId;
				}

				public void setAcquirerTransactionId(String acquirerTransactionId) {
					this.acquirerTransactionId = acquirerTransactionId;
				}

				public String getTransactionId() {
					return transactionId;
				}

				public void setTransactionId(String transactionId) {
					this.transactionId = transactionId;
				}
			}

		}
	}
}
