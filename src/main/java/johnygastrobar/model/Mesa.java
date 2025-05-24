package johnygastrobar.model;

public class Mesa {
    private int idMesa;
    private int capacidade;
    private String localizacao;

    public Mesa(int capacidade, String localizacao) {
        this.capacidade = capacidade;
        this.localizacao = localizacao;
    }

    public Mesa(int idMesa, int capacidade, String localizacao) {
        this.idMesa = idMesa;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    @Override
    public String toString() {
        return "Mesa [ID=" + idMesa + ", Capacidade=" + capacidade + ", Localização=" + localizacao + "]";
    }
}