package johnygastrobar.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FeedbackPedido {
    private int idFeedback;
    private int idPedido;    // FK para Pedido
    private int idMesa;      // FK para Mesa (presente no BD, mesmo que possa ser derivada do Pedido)
    private String nomeClienteFeedback;
    private Integer notaComida;       // Pode ser nulo (1-5)
    private Integer notaAtendimento;  // Pode ser nulo (1-5)
    private String comentarioTexto;
    private LocalDateTime dataFeedback;

    // Construtor completo (usado pelo DAO ao ler do ResultSet)
    public FeedbackPedido(int idFeedback, int idPedido, int idMesa, String nomeClienteFeedback,
                          Integer notaComida, Integer notaAtendimento, String comentarioTexto,
                          LocalDateTime dataFeedback) {
        this.idFeedback = idFeedback;
        this.idPedido = idPedido;
        this.idMesa = idMesa;
        this.nomeClienteFeedback = nomeClienteFeedback;
        this.notaComida = notaComida;
        this.notaAtendimento = notaAtendimento;
        this.comentarioTexto = comentarioTexto;
        this.dataFeedback = dataFeedback;
    }

    // Construtor para criar um novo feedback (antes de inserir no BD)
    public FeedbackPedido(int idPedido, int idMesa, String nomeClienteFeedback,
                          Integer notaComida, Integer notaAtendimento, String comentarioTexto,
                          LocalDateTime dataFeedback) {
        this.idPedido = idPedido;
        this.idMesa = idMesa;
        this.nomeClienteFeedback = nomeClienteFeedback;
        this.notaComida = notaComida;
        this.notaAtendimento = notaAtendimento;
        this.comentarioTexto = comentarioTexto;
        this.dataFeedback = dataFeedback != null ? dataFeedback : LocalDateTime.now();
    }

    // Construtor vazio
    public FeedbackPedido() {
        this.dataFeedback = LocalDateTime.now(); // Padrão para data atual
    }

    // Getters
    public int getIdFeedback() {
        return idFeedback;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public String getNomeClienteFeedback() {
        return nomeClienteFeedback;
    }

    public Integer getNotaComida() {
        return notaComida;
    }

    public Integer getNotaAtendimento() {
        return notaAtendimento;
    }

    public String getComentarioTexto() {
        return comentarioTexto;
    }

    public LocalDateTime getDataFeedback() {
        return dataFeedback;
    }

    // Setters
    public void setIdFeedback(int idFeedback) {
        this.idFeedback = idFeedback;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public void setNomeClienteFeedback(String nomeClienteFeedback) {
        this.nomeClienteFeedback = nomeClienteFeedback;
    }

    public void setNotaComida(Integer notaComida) {
        this.notaComida = notaComida;
    }

    public void setNotaAtendimento(Integer notaAtendimento) {
        this.notaAtendimento = notaAtendimento;
    }

    public void setComentarioTexto(String comentarioTexto) {
        this.comentarioTexto = comentarioTexto;
    }

    public void setDataFeedback(LocalDateTime dataFeedback) {
        this.dataFeedback = dataFeedback;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "FeedbackPedido{" +
                "idFeedback=" + idFeedback +
                ", idPedido=" + idPedido +
                ", idMesa=" + idMesa +
                ", cliente='" + (nomeClienteFeedback != null ? nomeClienteFeedback : "Anônimo") + '\'' +
                ", notaComida=" + (notaComida != null ? notaComida : "N/A") +
                ", notaAtendimento=" + (notaAtendimento != null ? notaAtendimento : "N/A") +
                ", comentario='" + (comentarioTexto != null && !comentarioTexto.isEmpty() ? comentarioTexto.substring(0, Math.min(comentarioTexto.length(), 30))+"..." : "N/A") + '\'' +
                ", dataFeedback=" + (dataFeedback != null ? dataFeedback.format(formatter) : "N/A") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackPedido that = (FeedbackPedido) o;
        return idFeedback == that.idFeedback; // Chave primária
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFeedback); // Chave primária
    }
}