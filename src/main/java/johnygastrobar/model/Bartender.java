package johnygastrobar.model;

import java.math.BigDecimal; // IMPORT ADICIONADO
import java.time.LocalDate;

public class Bartender extends Funcionario {
    private String especialidadeBar;

    public Bartender(String nome, String cpf, BigDecimal salario, LocalDate dataContratacao, // BigDecimal aqui
                     String rua, String numero, String bairro, String cidade, String estado, String cep,
                     Integer idSupervisor, String especialidadeBar) {
        super(nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Passa BigDecimal
        this.especialidadeBar = especialidadeBar;
    }

    public Bartender(int id, String nome, String cpf, BigDecimal salario, LocalDate dataContratacao, // BigDecimal aqui
                     String rua, String numero, String bairro, String cidade, String estado, String cep,
                     Integer idSupervisor, String especialidadeBar) {
        super(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Passa BigDecimal
        this.especialidadeBar = especialidadeBar;
    }

    public Bartender() {
        super();
    }

    public String getEspecialidadeBar() {
        return especialidadeBar;
    }

    public void setEspecialidadeBar(String especialidadeBar) {
        this.especialidadeBar = especialidadeBar;
    }

    @Override
    public String toString() {
        return "Bartender{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", especialidadeBar='" + especialidadeBar + '\'' +
                '}';
    }
}