package johnygastrobar.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Reserva {
    private int idReserva;
    private String nomeResponsavel;
    private int numeroPessoas;
    private int idMesa; // Chave estrangeira para a Mesa
    private LocalDate dataReserva;
    private LocalTime horaReserva;
    private String observacao;

    // Construtor completo (usado pelo DAO ao ler do ResultSet)
    public Reserva(int idReserva, String nomeResponsavel, int numeroPessoas, int idMesa,
                   LocalDate dataReserva, LocalTime horaReserva, String observacao) {
        this.idReserva = idReserva;
        this.nomeResponsavel = nomeResponsavel;
        this.numeroPessoas = numeroPessoas;
        this.idMesa = idMesa;
        this.dataReserva = dataReserva;
        this.horaReserva = horaReserva;
        this.observacao = observacao;
    }

    // Construtor para criar uma nova reserva (antes de inserir no BD, sem ID)
    public Reserva(String nomeResponsavel, int numeroPessoas, int idMesa,
                   LocalDate dataReserva, LocalTime horaReserva, String observacao) {
        this.nomeResponsavel = nomeResponsavel;
        this.numeroPessoas = numeroPessoas;
        this.idMesa = idMesa;
        this.dataReserva = dataReserva;
        this.horaReserva = horaReserva;
        this.observacao = observacao;
    }

    // Construtor vazio
    public Reserva() {
    }

    // Getters
    public int getIdReserva() {
        return idReserva;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public int getNumeroPessoas() {
        return numeroPessoas;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public LocalDate getDataReserva() {
        return dataReserva;
    }

    public LocalTime getHoraReserva() {
        return horaReserva;
    }

    public String getObservacao() {
        return observacao;
    }

    // Setters
    public void setIdReserva(int idReserva) { // ID geralmente setado pelo DAO
        this.idReserva = idReserva;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public void setNumeroPessoas(int numeroPessoas) {
        this.numeroPessoas = numeroPessoas;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public void setDataReserva(LocalDate dataReserva) {
        this.dataReserva = dataReserva;
    }

    public void setHoraReserva(LocalTime horaReserva) {
        this.horaReserva = horaReserva;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

        return "Reserva{" +
                "idReserva=" + idReserva +
                ", nomeResponsavel='" + nomeResponsavel + '\'' +
                ", numeroPessoas=" + numeroPessoas +
                ", idMesa=" + idMesa +
                ", dataReserva=" + (dataReserva != null ? dataReserva.format(dateFormatter) : "N/A") +
                ", horaReserva=" + (horaReserva != null ? horaReserva.format(timeFormatter) : "N/A") +
                ", observacao='" + (observacao != null ? observacao : "") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return idReserva == reserva.idReserva; // Chave primária
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReserva); // Chave primária
    }
}