package johnygastrobar.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Adicionado para formatação no toString()
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int idPedido;
    private Integer idGarcom;
    private Integer idGerente;
    private int idMesa;
    private LocalDateTime dataHora;
    private boolean entregue;
    private Double desconto;
    private boolean pago; // NOVO: Campo para indicar se o pedido foi pago

    // Lista para armazenar os itens associados a este pedido
    private List<PedidoItem> itensDoPedido;

    // Construtor para criar um novo Pedido (sem ID, e com dataHora atual)
    // NOVO: Adicionado 'pago' com default FALSE
    public Pedido(Integer idGarcom, Integer idGerente, int idMesa, boolean entregue, Double desconto) {
        this.idGarcom = idGarcom;
        this.idGerente = idGerente;
        this.idMesa = idMesa;
        this.dataHora = LocalDateTime.now(); // Define a data e hora atuais ao criar o pedido
        this.entregue = entregue;
        this.desconto = desconto;
        this.pago = false; // Por padrão, um novo pedido não está pago
        this.itensDoPedido = new ArrayList<>();
    }

    // Construtor para recuperar um Pedido do BD (com ID e dataHora do BD)
    // NOVO: Adicionado 'pago'
    public Pedido(int idPedido, Integer idGarcom, Integer idGerente, int idMesa, LocalDateTime dataHora, boolean entregue, Double desconto, boolean pago) {
        this.idPedido = idPedido;
        this.idGarcom = idGarcom;
        this.idGerente = idGerente;
        this.idMesa = idMesa;
        this.dataHora = dataHora;
        this.entregue = entregue;
        this.desconto = desconto;
        this.pago = pago; // Valor vem do banco de dados
        this.itensDoPedido = new ArrayList<>();
    }

    // Classe interna para representar um item dentro de um pedido (N:M)
    public static class PedidoItem {
        private int idItem;
        private String nomeItem; // Para exibir na UI, não diretamente no BD Pedido_Item
        private String tipoItem; // Para exibir na UI
        private int quantidade;
        private double precoUnitario; // Preço do item no momento do pedido

        public PedidoItem(int idItem, String nomeItem, String tipoItem, int quantidade, double precoUnitario) {
            this.idItem = idItem;
            this.nomeItem = nomeItem;
            this.tipoItem = tipoItem;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }

        // Getters
        public int getIdItem() { return idItem; }
        public String getNomeItem() { return nomeItem; }
        public String getTipoItem() { return tipoItem; }
        public int getQuantidade() { return quantidade; }
        public double getPrecoUnitario() { return precoUnitario; }

        // Setters (se necessário para edição na UI)
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }

        public double getSubtotal() {
            return quantidade * precoUnitario;
        }

        @Override
        public String toString() {
            return nomeItem + " (" + quantidade + "x R$" + String.format("%.2f", precoUnitario) + ")";
        }
    }


    // Getters
    public int getIdPedido() { return idPedido; }
    public Integer getIdGarcom() { return idGarcom; }
    public Integer getIdGerente() { return idGerente; }
    public int getIdMesa() { return idMesa; }
    public LocalDateTime getDataHora() { return dataHora; }
    public boolean isEntregue() { return entregue; }
    public Double getDesconto() { return desconto; }
    public boolean isPago() { return pago; } // NOVO: Getter para 'pago'
    public List<PedidoItem> getItensDoPedido() { return itensDoPedido; }

    // Setters
    public void setIdGarcom(Integer idGarcom) { this.idGarcom = idGarcom; }
    public void setIdGerente(Integer idGerente) { this.idGerente = idGerente; }
    public void setIdMesa(int idMesa) { this.idMesa = idMesa; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public void setEntregue(boolean entregue) { this.entregue = entregue; }
    public void setDesconto(Double desconto) { this.desconto = desconto; }
    public void setPago(boolean pago) { this.pago = pago; } // NOVO: Setter para 'pago'
    public void setItensDoPedido(List<PedidoItem> itensDoPedido) { this.itensDoPedido = itensDoPedido; }

    // Método para adicionar um item ao pedido
    public void adicionarItem(int idItem, String nomeItem, String tipoItem, int quantidade, double precoUnitario) {
        this.itensDoPedido.add(new PedidoItem(idItem, nomeItem, tipoItem, quantidade, precoUnitario));
    }

    public double getValorTotalSemDesconto() {
        return itensDoPedido.stream().mapToDouble(PedidoItem::getSubtotal).sum();
    }

    public double getValorTotalComDesconto() {
        double total = getValorTotalSemDesconto();
        if (desconto != null && desconto > 0 && total > 0) {
            return total * (1 - (desconto / 100.0));
        }
        return total;
    }

    @Override
    public String toString() {
        return "Pedido [ID=" + idPedido + ", Mesa=" + idMesa + ", Garçom=" + (idGarcom != null ? idGarcom : "N/A") +
                ", Data/Hora=" + dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                ", Entregue=" + (entregue ? "Sim" : "Não") +
                ", Desconto=" + (desconto != null ? String.format("%.2f", desconto) + "%" : "Nenhum") +
                ", Pago=" + (pago ? "Sim" : "Não") + // NOVO: Exibe o status de pago
                ", Total=R$" + String.format("%.2f", getValorTotalComDesconto()) + "]";
    }
}