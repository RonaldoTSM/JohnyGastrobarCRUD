package johnygastrobar.model;

import java.math.BigDecimal; // IMPORT ADICIONADO
import java.time.LocalDate;
import java.util.Objects; // Necessário para hashCode se usar Objects.hash com mais campos

public class Gerente extends Funcionario {
    private String nivelAcesso;
    private BigDecimal limiteDesconto; // ALTERADO PARA BigDecimal

    public Gerente(String nome, String cpf, BigDecimal salario, LocalDate dataContratacao,
                   String rua, String numero, String bairro, String cidade, String estado, String cep,
                   Integer idSupervisor, String nivelAcesso, BigDecimal limiteDesconto) {
        super(nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Salario é BigDecimal
        this.nivelAcesso = nivelAcesso;
        this.limiteDesconto = limiteDesconto;
    }

    public Gerente(int id, String nome, String cpf, BigDecimal salario, LocalDate dataContratacao,
                   String rua, String numero, String bairro, String cidade, String estado, String cep,
                   Integer idSupervisor, String nivelAcesso, BigDecimal limiteDesconto) {
        super(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Salario é BigDecimal
        this.nivelAcesso = nivelAcesso;
        this.limiteDesconto = limiteDesconto;
    }

    public Gerente() {
        super();
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    public BigDecimal getLimiteDesconto() {
        return limiteDesconto;
    }

    public void setLimiteDesconto(BigDecimal limiteDesconto) {
        this.limiteDesconto = limiteDesconto;
    }

    @Override
    public String toString() {
        return "Gerente{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", nivelAcesso='" + nivelAcesso + '\'' +
                ", limiteDesconto=" + (limiteDesconto != null ? limiteDesconto.toPlainString() : "N/A") +
                '}';
    }

    // O equals e hashCode da superclasse Funcionario (baseado em ID) geralmente são suficientes.
    // Se precisar de lógica específica para Gerente, pode sobrescrevê-los.
    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (o == null || getClass() != o.getClass()) return false;
    //     if (!super.equals(o)) return false; // Chama o equals da superclasse
    //     Gerente gerente = (Gerente) o;
    //     return Objects.equals(nivelAcesso, gerente.nivelAcesso) &&
    //            Objects.equals(limiteDesconto, gerente.limiteDesconto);
    // }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(super.hashCode(), nivelAcesso, limiteDesconto);
    // }
}