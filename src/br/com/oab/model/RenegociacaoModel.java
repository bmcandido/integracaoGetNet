package br.com.oab.model;

import java.math.BigDecimal;

public class RenegociacaoModel {

	private BigDecimal codCencus;
	private BigDecimal codProj;
	private BigDecimal nureneg;
	private BigDecimal nufin;
	private BigDecimal vlrDesdob;
	private BigDecimal vlrJurosNegoc;
	private BigDecimal vlrMultaNegoc;
	private BigDecimal percRateio;
	private BigDecimal vlrCorrecao;
	private BigDecimal codnat;
	private BigDecimal codParc;
	private BigDecimal parametro;
	private BigDecimal quantidadeParcelas;
	
	

	public BigDecimal getQuantidadeParcelas() {
		return quantidadeParcelas;
	}

	public void setQuantidadeParcelas(BigDecimal quantidadeParcelas) {
		this.quantidadeParcelas = quantidadeParcelas;
	}

	public void setCodParc(BigDecimal codParc) {
		this.codParc = codParc;
	}

	public BigDecimal getParametro() {
		return parametro;
	}

	public void setParametro(BigDecimal parametro) {
		this.parametro = parametro;
	}

	public BigDecimal getCodnat() {
		return codnat;
	}

	public void setCodnat(BigDecimal codnat) {
		this.codnat = codnat;
	}

	public BigDecimal getCodCencus() {
		return codCencus;
	}

	public void setCodCencus(BigDecimal codCencus) {
		this.codCencus = codCencus;
	}

	public BigDecimal getCodProj() {
		return codProj;
	}

	public void setCodProj(BigDecimal codProj) {
		this.codProj = codProj;
	}

	public BigDecimal getNureneg() {
		return nureneg;
	}
	
	public BigDecimal getCodParc() {
		return codParc;
	}

	public void setNureneg(BigDecimal nureneg) {
		this.nureneg = nureneg;
	}

	public BigDecimal getNufin() {
		return nufin;
	}

	public void setNufin(BigDecimal nufin) {
		this.nufin = nufin;
	}

	public BigDecimal getVlrDesdob() {
		return vlrDesdob;
	}

	public void setVlrDesdob(BigDecimal vlrDesdob) {
		this.vlrDesdob = vlrDesdob;
	}

	public BigDecimal getVlrJurosNegoc() {
		return vlrJurosNegoc;
	}

	public void setVlrJurosNegoc(BigDecimal vlrJurosNegoc) {
		this.vlrJurosNegoc = vlrJurosNegoc;
	}

	public BigDecimal getVlrMultaNegoc() {
		return vlrMultaNegoc;
	}

	public void setVlrMultaNegoc(BigDecimal vlrMultaNegoc) {
		this.vlrMultaNegoc = vlrMultaNegoc;
	}

	public BigDecimal getPercRateio() {
		return percRateio;
	}

	public void setPercRateio(BigDecimal percRateio) {
		this.percRateio = percRateio;
	}

	public BigDecimal getVlrCorrecao() {
		return vlrCorrecao;
	}

	public void setVlrCorrecao(BigDecimal vlrCorrecao) {
		this.vlrCorrecao = vlrCorrecao;
	}
	
	public void setCodparc(BigDecimal codparc) {
		this.codParc = codparc;
	}

}
