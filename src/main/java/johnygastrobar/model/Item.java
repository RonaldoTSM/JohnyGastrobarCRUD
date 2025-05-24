package johnygastrobar.model;

public class Item {
    private int idItem;
    private String nome;
    private String tipo;
    private double preco;

    public Item(String nome, String tipo, double preco) {
        this.nome = nome;
        this.tipo = tipo;
        this.preco = preco;
    }

    public Item(int idItem, String nome, String tipo, double preco) {
        this.idItem = idItem;
        this.nome = nome;
        this.tipo = tipo;
        this.preco = preco;
    }

    public int getIdItem() {
        return idItem;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public double getPreco() {
        return preco;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    @Override
    public String toString() {
        return "Item [ID=" + idItem + ", Nome=" + nome + ", Tipo=" + tipo + ", Preco=" + String.format("%.2f", preco) + "]";
    }
}