package johnygastrobar.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Adicionado para formatação no toString()

public class Pagamento {
    private int idPagamento;
    private int idPedido; // Chave estrangeira para Pedido
    private double valorTotal;
    private String metodoPagamento;
    private LocalDateTime dataPagamento;

    // Construtor para criar um novo Pagamento (sem ID, e com dataPagamento atual)
    public Pagamento(int idPedido, double valorTotal, String metodoPagamento) {
        this.idPedido = idPedido;
        this.valorTotal = valorTotal;
        this.metodoPagamento = metodoPagamento;
        this.dataPagamento = LocalDateTime.now(); // Define a data e hora atuais ao criar o pagamento
    }

    // Construtor para recuperar um Pagamento do BD (com ID e dataPagamento do BD)
    public Pagamento(int idPagamento, int idPedido, double valorTotal, String metodoPagamento, LocalDateTime dataPagamento) {
        this.idPagamento = idPagamento;
        this.idPedido = idPedido;
        this.valorTotal = valorTotal;
        this.metodoPagamento = metodoPagamento;
        this.dataPagamento = dataPagamento;
    }

    // Getters
    public int getIdPagamento() {
        return idPagamento;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    // Setters (para permitir atualização de dados em objetos)
    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    @Override
    public String toString() {
        return "Pagamento [ID=" + idPagamento + ", Pedido ID=" + idPedido +
                ", Valor Total=R$" + String.format("%.2f", valorTotal) +
                ", Método=" + metodoPagamento + ", Data/Hora=" + dataPagamento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "]";
    }
}