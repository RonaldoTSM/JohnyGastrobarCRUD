package johnygastrobar.model;

import java.math.BigDecimal; // IMPORT ADICIONADO (para o parâmetro do construtor)
import java.time.LocalDate;

public class Garcom extends Funcionario {
    private String setorAtendimento;

    public Garcom(String nome, String cpf, BigDecimal salario, LocalDate dataContratacao,
                  String rua, String numero, String bairro, String cidade, String estado, String cep,
                  Integer idSupervisor, String setorAtendimento) {
        super(nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Passa BigDecimal
        this.setorAtendimento = setorAtendimento;
    }

    public Garcom(int id, String nome, String cpf, BigDecimal salario, LocalDate dataContratacao,
                  String rua, String numero, String bairro, String cidade, String estado, String cep,
                  Integer idSupervisor, String setorAtendimento) {
        super(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Passa BigDecimal
        this.setorAtendimento = setorAtendimento;
    }

    public Garcom() {
        super();
    }

    public String getSetorAtendimento() {
        return setorAtendimento;
    }

    public void setSetorAtendimento(String setorAtendimento) {
        this.setorAtendimento = setorAtendimento;
    }

    @Override
    public String toString() {
        return "Garcom{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", setorAtendimento='" + setorAtendimento + '\'' +
                '}';
    }
}