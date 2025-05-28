package johnygastrobar.model;

import java.time.LocalDate;
import java.util.Objects;

public class Dependente {
    private int idFuncionario; // Chave estrangeira e parte da chave primária composta
    private String nomeDependente; // Parte da chave primária composta
    private LocalDate dataNascimento;
    private String parentesco;

    // Construtor completo (usado pelo DAO ao ler do ResultSet)
    public Dependente(int idFuncionario, String nomeDependente, LocalDate dataNascimento, String parentesco) {
        this.idFuncionario = idFuncionario;
        this.nomeDependente = nomeDependente;
        this.dataNascimento = dataNascimento;
        this.parentesco = parentesco;
    }

    // Construtor para criar um novo dependente (antes de inserir no BD)
    // idFuncionario será setado pelo Funcionario ou pelo serviço antes da persistência.
    public Dependente(String nomeDependente, LocalDate dataNascimento, String parentesco) {
        this.nomeDependente = nomeDependente;
        this.dataNascimento = dataNascimento;
        this.parentesco = parentesco;
    }

    // Construtor vazio
    public Dependente() {
    }

    // Getters
    public int getIdFuncionario() {
        return idFuncionario;
    }

    public String getNomeDependente() {
        return nomeDependente;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getParentesco() {
        return parentesco;
    }

    // Setters
    public void setIdFuncionario(int idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public void setNomeDependente(String nomeDependente) {
        this.nomeDependente = nomeDependente;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    @Override
    public String toString() {
        return "Dependente{" +
                "idFuncionario=" + idFuncionario +
                ", nomeDependente='" + nomeDependente + '\'' +
                ", dataNascimento=" + (dataNascimento != null ? dataNascimento.toString() : "N/A") +
                ", parentesco='" + parentesco + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependente that = (Dependente) o;
        // A chave primária da tabela Dependente é composta por id_funcionario e nome_dependente
        return idFuncionario == that.idFuncionario &&
                Objects.equals(nomeDependente, that.nomeDependente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFuncionario, nomeDependente);
    }
}