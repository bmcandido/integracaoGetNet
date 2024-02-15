package br.com.oab.model;


import java.math.BigDecimal;

public class LinkOrigModel {



	BigDecimal nuReneg;
    String linkId;
    String link;
    String linkIdOrder;
    String dhBaixa;

    public String getDhBaixa() {
		return dhBaixa;
	}

	public void setDhBaixa(String dhBaixa) {
		this.dhBaixa = dhBaixa;
	}

	public BigDecimal getNuReneg() {
        return nuReneg;
    }

    public void setNuReneg(BigDecimal nuReneg) {
        this.nuReneg = nuReneg;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
    public String getLinkIdOrder() {
		return linkIdOrder;
	}

	public void setLinkIdOrder(String linkIdOrder) {
		this.linkIdOrder = linkIdOrder;
	}
}

