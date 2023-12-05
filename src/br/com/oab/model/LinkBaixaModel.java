package br.com.oab.model;

import java.math.BigDecimal;

public class LinkBaixaModel {

	BigDecimal nureneg;
	BigDecimal nufin;
	String idLink;
	String cpfCgc;
	BigDecimal vlrTitulo;
	String docTip;
	String name;
	String dataDocumento;

	public String getCpfCgc() {
		return cpfCgc;
	}

	public void setCpfCgc(String cpfCgc) {
		this.cpfCgc = cpfCgc;
	}

	public BigDecimal getVlrTitulo() {
		return vlrTitulo;
	}

	public void setVlrTitulo(BigDecimal vlrTitulo) {
		this.vlrTitulo = vlrTitulo;
	}

	public String getDocTip() {
		return docTip;
	}

	public void setDocTip(String docTip) {
		this.docTip = docTip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataDocumento() {
		return dataDocumento;
	}

	public void setDataDocumento(String dataDocumento) {
		this.dataDocumento = dataDocumento;
	}

	public BigDecimal getNureneg() {
		return nureneg;
	}

	public void setNureneg(BigDecimal nureneg) {
		this.nureneg = nureneg;
	}

	public String getIdLink() {
		return idLink;
	}

	public void setIdLink(String idLink) {
		this.idLink = idLink;
	}

	public BigDecimal getNufin() {
		return nufin;
	}

	public void setNufin(BigDecimal nufin) {
		this.nufin = nufin;
	}

}
