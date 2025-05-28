package johnygastrobar.model;

import java.math.BigDecimal; // IMPORT ADICIONADO
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Pagamento {
    private int idPagamento;
    private int idPedido;
    private BigDecimal valorTotal; // ALTERADO PARA BigDecimal
    private String metodoPagamento;
    private LocalDateTime dataPagamento;

    public Pagamento(int idPagamento, int idPedido, BigDecimal valorTotal, String metodoPagamento, LocalDateTime dataPagamento) { // TIPO ALTERADO
        this.idPagamento = idPagamento;
        this.idPedido = idPedido;
        this.valorTotal = valorTotal; // TIPO ALTERADO
        this.metodoPagamento = metodoPagamento;
        this.dataPagamento = dataPagamento;
    }

    public Pagamento(int idPedido, BigDecimal valorTotal, String metodoPagamento, LocalDateTime dataPagamento) { // TIPO ALTERADO
        this.idPedido = idPedido;
        this.valorTotal = valorTotal; // TIPO ALTERADO
        this.metodoPagamento = metodoPagamento;
        this.dataPagamento = dataPagamento != null ? dataPagamento : LocalDateTime.now();
    }

    public Pagamento() {
        this.dataPagamento = LocalDateTime.now();
    }

    // Getters
    public int getIdPagamento() { return idPagamento; }
    public int getIdPedido() { return idPedido; }
    public BigDecimal getValorTotal() { return valorTotal; } // TIPO ALTERADO
    public String getMetodoPagamento() { return metodoPagamento; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }

    // Setters
    public void setIdPagamento(int idPagamento) { this.idPagamento = idPagamento; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; } // TIPO ALTERADO
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "Pagamento{" +
                "idPagamento=" + idPagamento +
                ", idPedido=" + idPedido +
                ", valorTotal=R$" + (valorTotal != null ? valorTotal.toPlainString() : "N/A") +
                ", metodoPagamento='" + metodoPagamento + '\'' +
                ", dataPagamento=" + (dataPagamento != null ? dataPagamento.format(formatter) : "N/A") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return idPagamento == pagamento.idPagamento;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPagamento);
    }
}