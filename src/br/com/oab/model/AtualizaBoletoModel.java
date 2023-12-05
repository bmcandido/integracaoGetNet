package br.com.oab.model;


import java.math.BigDecimal;
import java.sql.Timestamp;

public class AtualizaBoletoModel {

    private BigDecimal nufin;
    private Timestamp dtvenc;
    private String nossoNumero;
    private String linhaDigitavel;
    private String codigoBarra;

    public BigDecimal getNufin() {
        return nufin;
    }

    public void setNufin(BigDecimal nufin) {
        this.nufin = nufin;
    }

    public Timestamp getDtvenc() {
        return dtvenc;
    }

    public void setDtvenc(Timestamp dtvenc) {
        this.dtvenc = dtvenc;
    }

    public String getNossoNumero() {
        return nossoNumero;
    }

    public void setNossoNumero(String nossoNumero) {
        this.nossoNumero = nossoNumero;
    }

    public String getLinhaDigitavel() {
        return linhaDigitavel;
    }

    public void setLinhaDigitavel(String linhaDigitavel) {
        this.linhaDigitavel = linhaDigitavel;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }
}
