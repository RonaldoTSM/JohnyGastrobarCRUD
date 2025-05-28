package johnygastrobar.model;

import java.math.BigDecimal;
import java.math.RoundingMode; // Para cálculos com desconto
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {
    private int idPedido;
    private Integer idGarcom;
    private Integer idGerente;
    private int idMesa;
    private LocalDateTime dataHora;
    private boolean entregue;
    private boolean pago;
    private BigDecimal desconto; // ALTERADO PARA BigDecimal

    private List<PedidoItem> itensDoPedido;

    public Pedido(int idPedido, Integer idGarcom, Integer idGerente, int idMesa, LocalDateTime dataHora,
                  boolean entregue, boolean pago, BigDecimal desconto) {
        this.idPedido = idPedido;
        this.idGarcom = idGarcom;
        this.idGerente = idGerente;
        this.idMesa = idMesa;
        this.dataHora = dataHora;
        this.entregue = entregue;
        this.pago = pago;
        this.desconto = desconto;
        this.itensDoPedido = new ArrayList<>();
    }

    public Pedido(Integer idGarcom, Integer idGerente, int idMesa, LocalDateTime dataHora, BigDecimal desconto) {
        this.idGarcom = idGarcom;
        this.idGerente = idGerente;
        this.idMesa = idMesa;
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
        this.entregue = false;
        this.pago = false;
        this.desconto = desconto;
        this.itensDoPedido = new ArrayList<>();
    }

    public Pedido() {
        this.dataHora = LocalDateTime.now();
        this.itensDoPedido = new ArrayList<>();
        this.entregue = false;
        this.pago = false;
    }

    // Getters
    public int getIdPedido() { return idPedido; }
    public Integer getIdGarcom() { return idGarcom; }
    public Integer getIdGerente() { return idGerente; }
    public int getIdMesa() { return idMesa; }
    public LocalDateTime getDataHora() { return dataHora; }
    public boolean isEntregue() { return entregue; }
    public boolean isPago() { return pago; }
    public BigDecimal getDesconto() { return desconto; }
    public List<PedidoItem> getItensDoPedido() { return itensDoPedido; }

    // Setters
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public void setIdGarcom(Integer idGarcom) { this.idGarcom = idGarcom; }
    public void setIdGerente(Integer idGerente) { this.idGerente = idGerente; }
    public void setIdMesa(int idMesa) { this.idMesa = idMesa; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public void setEntregue(boolean entregue) { this.entregue = entregue; }
    public void setPago(boolean pago) { this.pago = pago; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }
    public void setItensDoPedido(List<PedidoItem> itensDoPedido) {
        this.itensDoPedido = (itensDoPedido != null) ? itensDoPedido : new ArrayList<>();
    }

    public void adicionarItem(PedidoItem item) {
        if (this.itensDoPedido == null) {
            this.itensDoPedido = new ArrayList<>();
        }
        this.itensDoPedido.add(item);
    }

    public void adicionarItem(Item item, int quantidade) {
        if (item == null || quantidade <= 0) {
            throw new IllegalArgumentException("Item não pode ser nulo e quantidade deve ser positiva.");
        }
        // item.getPreco() agora retorna BigDecimal
        PedidoItem novoItem = new PedidoItem(item.getIdItem(), item.getNome(), item.getTipo(), quantidade, item.getPreco());
        adicionarItem(novoItem);
    }

    public BigDecimal getValorTotalSemDesconto() {
        if (itensDoPedido == null) {
            return BigDecimal.ZERO;
        }
        return itensDoPedido.stream()
                .map(PedidoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorTotalComDesconto() {
        BigDecimal totalSemDesconto = getValorTotalSemDesconto();
        if (desconto != null && desconto.compareTo(BigDecimal.ZERO) > 0 && desconto.compareTo(new BigDecimal("100")) <= 0) {
            // Calcula fator de desconto: (1 - (descontoPercentual / 100))
            // Ex: desconto = 10 (10%), fator = 1 - (10/100) = 0.90
            BigDecimal fatorDesconto = BigDecimal.ONE.subtract(desconto.divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP));
            return totalSemDesconto.multiply(fatorDesconto).setScale(2, RoundingMode.HALF_UP);
        }
        return totalSemDesconto.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", idMesa=" + idMesa +
                ", dataHora=" + (dataHora != null ? dataHora.format(formatter) : "N/A") +
                ", pago=" + pago +
                ", desconto=" + (desconto != null ? desconto.toPlainString() + "%" : "0.00%") +
                ", valorTotal=" + (getValorTotalComDesconto() != null ? getValorTotalComDesconto().toPlainString() : "N/A") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return idPedido == pedido.idPedido;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPedido);
    }

    public static class PedidoItem {
        private int idItem;
        private String nomeItem;
        private String tipoItem;
        private int quantidade;
        private BigDecimal precoUnitario; // ALTERADO PARA BigDecimal

        public PedidoItem(int idItem, String nomeItem, String tipoItem, int quantidade, BigDecimal precoUnitario) {
            this.idItem = idItem;
            this.nomeItem = nomeItem;
            this.tipoItem = tipoItem;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }

        public PedidoItem() {
        }

        // Getters
        public int getIdItem() { return idItem; }
        public String getNomeItem() { return nomeItem; }
        public String getTipoItem() { return tipoItem; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getPrecoUnitario() { return precoUnitario; }

        // Setters
        public void setIdItem(int idItem) { this.idItem = idItem; }
        public void setNomeItem(String nomeItem) { this.nomeItem = nomeItem; }
        public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

        public BigDecimal getSubtotal() {
            if (precoUnitario == null || quantidade <=0) return BigDecimal.ZERO;
            return precoUnitario.multiply(new BigDecimal(quantidade)).setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public String toString() {
            return "PedidoItem{" +
                    "idItem=" + idItem +
                    ", nome='" + nomeItem + '\'' +
                    ", qtd=" + quantidade +
                    ", precoUnit=R$" + (precoUnitario != null ? precoUnitario.toPlainString() : "N/A") +
                    ", subtotal=R$" + (getSubtotal() != null ? getSubtotal().toPlainString() : "N/A") +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PedidoItem that = (PedidoItem) o;
            // Comparação mais robusta para PedidoItem
            return idItem == that.idItem &&
                    quantidade == that.quantidade &&
                    ((precoUnitario == null && that.precoUnitario == null) ||
                            (precoUnitario != null && precoUnitario.compareTo(that.precoUnitario) == 0));
        }

        @Override
        public int hashCode() {
            return Objects.hash(idItem, quantidade, precoUnitario);
        }
    }
}