package br.com.oab.model;


import java.math.BigDecimal;
import java.util.Date;

public class ParametrosModel {

    private String clientId;
    private String clientSecret;
    private String sellerId;
    private BigDecimal expiraMin;
    private String basePath;
    private String urlAuth;
    private String accessToken;
    private String refreshToken;
    private Date dhExpira;
    private String tokenType;

    public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getUrlAuth() {
        return urlAuth;
    }

    public void setUrlAuth(String urlAuth) {
        this.urlAuth = urlAuth;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public BigDecimal getExpiraMin() {
        return expiraMin;
    }

    public void setExpiraMin(BigDecimal expiraMin) {
        this.expiraMin = expiraMin;
    }

    public Date getDhExpira() {
        return dhExpira;
    }

    public void setDhExpira(Date dhExpira) {
        this.dhExpira = dhExpira;
    }

}

