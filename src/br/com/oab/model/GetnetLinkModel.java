package br.com.oab.model;


import com.google.gson.annotations.SerializedName;

public class GetnetLinkModel {

    @SerializedName("label")
    private String label;
    @SerializedName("expiration")
    private String expiration;
    @SerializedName("max_orders")
    private Integer maxOrders;
    @SerializedName("order")
    private OrderGTN order;
    @SerializedName("payment")
    private PaymentGTN payment;
    @SerializedName("link_id")
    private String linkId;
    @SerializedName("status")
    private String status;
    @SerializedName("successful_orders")
    private Integer successfulOrders;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("access_counter")
    private Integer accessCounter;
    @SerializedName("payment_orders_created")
    private Integer paymentOrdersCreated;
    @SerializedName("url")
    private String url;
    @SerializedName("seller_id")
    private String sellerId;
    @SerializedName("merchant")
    private MerchantGTN merchant;
    @SerializedName("last_order_at")
    private String lastOrderAt;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public Integer getMaxOrders() {
        return maxOrders;
    }

    public void setMaxOrders(Integer maxOrders) {
        this.maxOrders = maxOrders;
    }

    public OrderGTN getOrder() {
        return order;
    }

    public void setOrder(OrderGTN order) {
        this.order = order;
    }

    public PaymentGTN getPayment() {
        return payment;
    }

    public void setPayment(PaymentGTN payment) {
        this.payment = payment;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSuccessfulOrders() {
        return successfulOrders;
    }

    public void setSuccessfulOrders(Integer successfulOrders) {
        this.successfulOrders = successfulOrders;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getAccessCounter() {
        return accessCounter;
    }

    public void setAccessCounter(Integer accessCounter) {
        this.accessCounter = accessCounter;
    }

    public Integer getPaymentOrdersCreated() {
        return paymentOrdersCreated;
    }

    public void setPaymentOrdersCreated(Integer paymentOrdersCreated) {
        this.paymentOrdersCreated = paymentOrdersCreated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public MerchantGTN getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantGTN merchant) {
        this.merchant = merchant;
    }

    public String getLastOrderAt() {
        return lastOrderAt;
    }

    public void setLastOrderAt(String lastOrderAt) {
        this.lastOrderAt = lastOrderAt;
    }

    public static class OrderGTN {
        @SerializedName("product_type")
        private String productType;
        @SerializedName("title")
        private String title;
        @SerializedName("description")
        private String description;
        @SerializedName("order_prefix")
        private String orderPrefix;
        @SerializedName("shipping_amount")
        private Integer shippingAmount;
        @SerializedName("amount")
        private Integer amount;

        public String getProductType() {
            return productType;
        }

        public void setProductType(String productType) {
            this.productType = productType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOrderPrefix() {
            return orderPrefix;
        }

        public void setOrderPrefix(String orderPrefix) {
            this.orderPrefix = orderPrefix;
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
    }

    public static class PaymentGTN {
        @SerializedName("credit")
        private CreditGTN credit;
        @SerializedName("debit")
        private DebitGTN debit;
        @SerializedName("wallet")
        private WalletGTN wallet;
        @SerializedName("pix")
        private PixGTN pix;

        public CreditGTN getCredit() {
            return credit;
        }

        public void setCredit(CreditGTN credit) {
            this.credit = credit;
        }

        public DebitGTN getDebit() {
            return debit;
        }

        public void setDebit(DebitGTN debit) {
            this.debit = debit;
        }

        public WalletGTN getWallet() {
            return wallet;
        }

        public void setWallet(WalletGTN wallet) {
            this.wallet = wallet;
        }

        public PixGTN getPix() {
            return pix;
        }

        public void setPix(PixGTN pix) {
            this.pix = pix;
        }

        public static class CreditGTN {
            @SerializedName("enable")
            private Boolean enable;
            @SerializedName("max_installments")
            private Integer maxInstallments;
            @SerializedName("not_authenticated")
            private Boolean notAuthenticated;
            @SerializedName("authenticated")
            private Boolean authenticated;

            public Boolean getEnable() {
                return enable;
            }

            public void setEnable(Boolean enable) {
                this.enable = enable;
            }

            public Integer getMaxInstallments() {
                return maxInstallments;
            }

            public void setMaxInstallments(Integer maxInstallments) {
                this.maxInstallments = maxInstallments;
            }

            public Boolean getNotAuthenticated() {
                return notAuthenticated;
            }

            public void setNotAuthenticated(Boolean notAuthenticated) {
                this.notAuthenticated = notAuthenticated;
            }

            public Boolean getAuthenticated() {
                return authenticated;
            }

            public void setAuthenticated(Boolean authenticated) {
                this.authenticated = authenticated;
            }
        }

        public static class DebitGTN {
            @SerializedName("enable")
            private Boolean enable;
            @SerializedName("caixa_virtual_card")
            private Boolean caixaVirtualCard;
            @SerializedName("not_authenticated")
            private Boolean notAuthenticated;
            @SerializedName("authenticated")
            private Boolean authenticated;

            public Boolean getEnable() {
                return enable;
            }

            public void setEnable(Boolean enable) {
                this.enable = enable;
            }

            public Boolean getCaixaVirtualCard() {
                return caixaVirtualCard;
            }

            public void setCaixaVirtualCard(Boolean caixaVirtualCard) {
                this.caixaVirtualCard = caixaVirtualCard;
            }

            public Boolean getNotAuthenticated() {
                return notAuthenticated;
            }

            public void setNotAuthenticated(Boolean notAuthenticated) {
                this.notAuthenticated = notAuthenticated;
            }

            public Boolean getAuthenticated() {
                return authenticated;
            }

            public void setAuthenticated(Boolean authenticated) {
                this.authenticated = authenticated;
            }
        }

        public static class WalletGTN {
            @SerializedName("enable")
            private Boolean enable;

            public Boolean getEnable() {
                return enable;
            }

            public void setEnable(Boolean enable) {
                this.enable = enable;
            }
        }

        public static class PixGTN {
            @SerializedName("enable")
            private Boolean enable;

            public Boolean getEnable() {
                return enable;
            }

            public void setEnable(Boolean enable) {
                this.enable = enable;
            }
        }
    }

    public static class MerchantGTN {
        @SerializedName("document_type")
        private String documentType;
        @SerializedName("document")
        private String document;
        @SerializedName("name")
        private String name;

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getDocument() {
            return document;
        }

        public void setDocument(String document) {
            this.document = document;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
