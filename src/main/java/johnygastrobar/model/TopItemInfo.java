package johnygastrobar.model;

import java.math.BigDecimal;

public class TopItemInfo {
    private String nomeItem;
    private BigDecimal valorAgregado; // Pode ser quantidade total ou faturamento total

    public TopItemInfo(String nomeItem, BigDecimal valorAgregado) {
        this.nomeItem = nomeItem;
        this.valorAgregado = valorAgregado;
    }

    // Getters
    public String getNomeItem() {
        return nomeItem;
    }

    public BigDecimal getValorAgregado() {
        return valorAgregado;
    }

    // Setters (opcionais, dependendo do uso)
    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;
    }

    public void setValorAgregado(BigDecimal valorAgregado) {
        this.valorAgregado = valorAgregado;
    }

    @Override
    public String toString() {
        return "TopItemInfo{" +
                "nomeItem='" + nomeItem + '\'' +
                ", valorAgregado=" + (valorAgregado != null ? valorAgregado.toPlainString() : "N/A") +
                '}';
    }
}