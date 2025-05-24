package johnygastrobar.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reserva {
    private int idReserva;
    private String nomeResponsavel;
    private int numeroPessoas;
    private int idMesa;
    private LocalDate dataReserva;
    private LocalTime horaReserva;
    private String observacao;

    public Reserva(String nomeResponsavel, int numeroPessoas, int idMesa, LocalDate dataReserva, LocalTime horaReserva, String observacao) {
        this.nomeResponsavel = nomeResponsavel;
        this.numeroPessoas = numeroPessoas;
        this.idMesa = idMesa;
        this.dataReserva = dataReserva;
        this.horaReserva = horaReserva;
        this.observacao = observacao;
    }

    public Reserva(int idReserva, String nomeResponsavel, int numeroPessoas, int idMesa, LocalDate dataReserva, LocalTime horaReserva, String observacao) {
        this.idReserva = idReserva;
        this.nomeResponsavel = nomeResponsavel;
        this.numeroPessoas = numeroPessoas;
        this.idMesa = idMesa;
        this.dataReserva = dataReserva;
        this.horaReserva = horaReserva;
        this.observacao = observacao;
    }

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
        return "Reserva [ID=" + idReserva + ", Responsável=" + nomeResponsavel + ", Pessoas=" + numeroPessoas +
                ", Mesa ID=" + idMesa + ", Data=" + dataReserva + ", Hora=" + horaReserva +
                ", Obs='" + observacao + "']";
    }
}