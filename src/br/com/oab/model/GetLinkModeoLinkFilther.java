package br.com.oab.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GetLinkModeoLinkFilther {
	
	
	 @SerializedName("total")
	    private int total;
	    @SerializedName("page_number")
	    private int pageNumber;
	    @SerializedName("total_pages")
	    private int totalPages;
	    @SerializedName("per_page")
	    private int perPage;
	    @SerializedName("result")
	    private List<GetNetPagamentoOnlineFilther> result;
	    
	    public int getTotal() {
	    	
	        return total;
	    }

	    public int getPageNumber() {
	        return pageNumber;
	    }

	    public int getTotalPages() {
	        return totalPages;
	    }

	    public int getPerPage() {
	        return perPage;
	    }

	    public List<GetNetPagamentoOnlineFilther> getResult() {
	        return result;
	    }
	    
	    
	   public static class GetNetPagamentoOnlineFilther {

	        @SerializedName("link_id")
	        private String linkId;
	        @SerializedName("label")
	        private String label;
	        @SerializedName("title")
	        private String title;
	        @SerializedName("amount")
	        private int amount;
	        @SerializedName("shipping_amount")
	        private int shippingAmount;
	        @SerializedName("max_orders")
	        private int maxOrders;
	        @SerializedName("successful_orders")
	        private int successfulOrders;
	        @SerializedName("created_at")
	        private String createdAt;
	        @SerializedName("expiration")
	        private String expiration;
	        @SerializedName("url")
	        private String url;
	        @SerializedName("status")
	        private String status;
	        @SerializedName("access_counter")
	        private int accessCounter;
	        @SerializedName("links")
	        private List<Link> links;

	        public String getLinkId() {
	            return linkId;
	        }

	        public String getLabel() {
	            return label;
	        }

	        public String getTitle() {
	            return title;
	        }

	        public int getAmount() {
	            return amount;
	        }

	        public int getShippingAmount() {
	            return shippingAmount;
	        }

	        public int getMaxOrders() {
	            return maxOrders;
	        }

	        public int getSuccessfulOrders() {
	            return successfulOrders;
	        }

	        public String getCreatedAt() {
	            return createdAt;
	        }

	        public String getExpiration() {
	            return expiration;
	        }

	        public String getUrl() {
	            return url;
	        }

	        public String getStatus() {
	            return status;
	        }

	        public int getAccessCounter() {
	            return accessCounter;
	        }

	        public List<Link> getLinks() {
	            return links;
	        }
	    }

	   public static class Link {

	        @SerializedName("rel")
	        private String rel;

	        @SerializedName("href")
	        private String href;

	        public String getRel() {
	            return rel;
	        }

	        public String getHref() {
	            return href;
	        }
	    }

	}




