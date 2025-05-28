package johnygastrobar.model;

import java.math.BigDecimal; // IMPORT ADICIONADO
import java.time.LocalDate;

public class Cozinheiro extends Funcionario {
    private String especialidadeCulinaria;

    public Cozinheiro(String nome, String cpf, BigDecimal salario, LocalDate dataContratacao, // BigDecimal aqui
                      String rua, String numero, String bairro, String cidade, String estado, String cep,
                      Integer idSupervisor, String especialidadeCulinaria) {
        super(nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Passa BigDecimal
        this.especialidadeCulinaria = especialidadeCulinaria;
    }

    public Cozinheiro(int id, String nome, String cpf, BigDecimal salario, LocalDate dataContratacao, // BigDecimal aqui
                      String rua, String numero, String bairro, String cidade, String estado, String cep,
                      Integer idSupervisor, String especialidadeCulinaria) {
        super(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor); // Passa BigDecimal
        this.especialidadeCulinaria = especialidadeCulinaria;
    }

    public Cozinheiro() {
        super();
    }

    public String getEspecialidadeCulinaria() {
        return especialidadeCulinaria;
    }

    public void setEspecialidadeCulinaria(String especialidadeCulinaria) {
        this.especialidadeCulinaria = especialidadeCulinaria;
    }

    @Override
    public String toString() {
        return "Cozinheiro{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", especialidadeCulinaria='" + especialidadeCulinaria + '\'' +
                '}';
    }
}