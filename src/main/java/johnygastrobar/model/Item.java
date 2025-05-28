package johnygastrobar.model;

import java.math.BigDecimal; // IMPORT ADICIONADO
import java.util.Objects;

public class Item {
    private int idItem;
    private String nome;
    private String tipo;
    private BigDecimal preco; // ALTERADO PARA BigDecimal

    public Item(int idItem, String nome, String tipo, BigDecimal preco) {
        this.idItem = idItem;
        this.nome = nome;
        this.tipo = tipo;
        this.preco = preco;
    }

    public Item(String nome, String tipo, BigDecimal preco) {
        this.nome = nome;
        this.tipo = tipo;
        this.preco = preco;
    }

    public Item() {
    }

    // Getters
    public int getIdItem() { return idItem; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public BigDecimal getPreco() { return preco; }

    // Setters
    public void setIdItem(int idItem) { this.idItem = idItem; }
    public void setNome(String nome) { this.nome = nome; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    @Override
    public String toString() {
        return "Item{" +
                "idItem=" + idItem +
                ", nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' +
                ", preco=" + (preco != null ? preco.toPlainString() : "N/A") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return idItem == item.idItem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idItem);
    }
}