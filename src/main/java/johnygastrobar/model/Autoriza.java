package johnygastrobar.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Autoriza {
    private int idAutorizacao;
    private int idPedido;    // FK para Pedido
    private int idGerente;   // FK para Gerente (que é um Funcionario)
    private LocalDateTime dataAutorizacao;
    private String observacaoAutorizacao;

    // Construtor completo (usado pelo DAO ao ler do ResultSet)
    public Autoriza(int idAutorizacao, int idPedido, int idGerente,
                    LocalDateTime dataAutorizacao, String observacaoAutorizacao) {
        this.idAutorizacao = idAutorizacao;
        this.idPedido = idPedido;
        this.idGerente = idGerente;
        this.dataAutorizacao = dataAutorizacao;
        this.observacaoAutorizacao = observacaoAutorizacao;
    }

    // Construtor para criar uma nova autorização (antes de inserir no BD)
    // dataAutorizacao pode ser LocalDateTime.now() ou vir de um parâmetro.
    public Autoriza(int idPedido, int idGerente, LocalDateTime dataAutorizacao, String observacaoAutorizacao) {
        this.idPedido = idPedido;
        this.idGerente = idGerente;
        this.dataAutorizacao = dataAutorizacao != null ? dataAutorizacao : LocalDateTime.now();
        this.observacaoAutorizacao = observacaoAutorizacao;
    }

    // Construtor vazio
    public Autoriza() {
        this.dataAutorizacao = LocalDateTime.now(); // Padrão para data atual
    }

    // Getters
    public int getIdAutorizacao() {
        return idAutorizacao;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getIdGerente() {
        return idGerente;
    }

    public LocalDateTime getDataAutorizacao() {
        return dataAutorizacao;
    }

    public String getObservacaoAutorizacao() {
        return observacaoAutorizacao;
    }

    // Setters
    public void setIdAutorizacao(int idAutorizacao) {
        this.idAutorizacao = idAutorizacao;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public void setIdGerente(int idGerente) {
        this.idGerente = idGerente;
    }

    public void setDataAutorizacao(LocalDateTime dataAutorizacao) {
        this.dataAutorizacao = dataAutorizacao;
    }

    public void setObservacaoAutorizacao(String observacaoAutorizacao) {
        this.observacaoAutorizacao = observacaoAutorizacao;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "Autoriza{" +
                "idAutorizacao=" + idAutorizacao +
                ", idPedido=" + idPedido +
                ", idGerente=" + idGerente +
                ", dataAutorizacao=" + (dataAutorizacao != null ? dataAutorizacao.format(formatter) : "N/A") +
                ", observacao='" + (observacaoAutorizacao != null ? observacaoAutorizacao : "") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Autoriza autoriza = (Autoriza) o;
        return idAutorizacao == autoriza.idAutorizacao; // Chave primária
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAutorizacao); // Chave primária
    }
}