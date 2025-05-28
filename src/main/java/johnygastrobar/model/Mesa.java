package johnygastrobar.model;

import java.util.Objects;

public class Mesa {
    private int idMesa;
    private int capacidade;
    private String localizacao;

    // Construtor completo (usado pelo DAO ao ler do ResultSet)
    public Mesa(int idMesa, int capacidade, String localizacao) {
        this.idMesa = idMesa;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
    }

    // Construtor para criar uma nova mesa (antes de inserir no BD, sem ID)
    public Mesa(int capacidade, String localizacao) {
        this.capacidade = capacidade;
        this.localizacao = localizacao;
    }

    // Construtor vazio
    public Mesa() {
    }

    // Getters
    public int getIdMesa() {
        return idMesa;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    // Setters
    public void setIdMesa(int idMesa) { // Geralmente o ID é setado pelo DAO após a inserção
        this.idMesa = idMesa;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    @Override
    public String toString() {
        return "Mesa{" +
                "idMesa=" + idMesa +
                ", capacidade=" + capacidade +
                ", localizacao='" + localizacao + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mesa mesa = (Mesa) o;
        return idMesa == mesa.idMesa; // Chave primária simples
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMesa); // Chave primária simples
    }
}