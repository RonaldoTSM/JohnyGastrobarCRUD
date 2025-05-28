package johnygastrobar.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal; // IMPORT ADICIONADO
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Garcom.class, name = "Garcom"),
        @JsonSubTypes.Type(value = Cozinheiro.class, name = "Cozinheiro"),
        @JsonSubTypes.Type(value = Bartender.class, name = "Bartender"),
        @JsonSubTypes.Type(value = Gerente.class, name = "Gerente")
})
public class Funcionario {
    private int id;
    private String nome;
    private String cpf;
    private BigDecimal salario; // ALTERADO PARA BigDecimal
    private LocalDate dataContratacao;

    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    private List<String> telefones;

    private Integer idSupervisor;
    private String nomeSupervisor;

    private List<Dependente> dependentes;

    public Funcionario(int id, String nome, String cpf, BigDecimal salario, LocalDate dataContratacao,
                       String rua, String numero, String bairro, String cidade, String estado, String cep,
                       Integer idSupervisor) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.salario = salario;
        this.dataContratacao = dataContratacao;
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.idSupervisor = idSupervisor;
        this.telefones = new ArrayList<>();
        this.dependentes = new ArrayList<>();
    }

    public Funcionario(String nome, String cpf, BigDecimal salario, LocalDate dataContratacao,
                       String rua, String numero, String bairro, String cidade, String estado, String cep,
                       Integer idSupervisor) {
        this.nome = nome;
        this.cpf = cpf;
        this.salario = salario;
        this.dataContratacao = dataContratacao;
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.idSupervisor = idSupervisor;
        this.telefones = new ArrayList<>();
        this.dependentes = new ArrayList<>();
    }

    public Funcionario() {
        this.telefones = new ArrayList<>();
        this.dependentes = new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public BigDecimal getSalario() { return salario; }
    public LocalDate getDataContratacao() { return dataContratacao; }
    public String getRua() { return rua; }
    public String getNumero() { return numero; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }
    public List<String> getTelefones() { return telefones; }
    public Integer getIdSupervisor() { return idSupervisor; }
    public String getNomeSupervisor() { return nomeSupervisor; }
    public List<Dependente> getDependentes() { return dependentes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public void setDataContratacao(LocalDate dataContratacao) { this.dataContratacao = dataContratacao; }
    public void setRua(String rua) { this.rua = rua; }
    public void setNumero(String numero) { this.numero = numero; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setCep(String cep) { this.cep = cep; }
    public void setTelefones(List<String> telefones) { this.telefones = (telefones != null) ? telefones : new ArrayList<>(); }
    public void setIdSupervisor(Integer idSupervisor) { this.idSupervisor = idSupervisor; }
    public void setNomeSupervisor(String nomeSupervisor) { this.nomeSupervisor = nomeSupervisor; }
    public void setDependentes(List<Dependente> dependentes) { this.dependentes = (dependentes != null) ? dependentes : new ArrayList<>(); }

    public void addTelefone(String telefone) {
        if (this.telefones == null) this.telefones = new ArrayList<>();
        if (telefone != null && !telefone.trim().isEmpty() && !this.telefones.contains(telefone.trim())) {
            this.telefones.add(telefone.trim());
        }
    }
    public void removeTelefone(String telefone) {
        if (this.telefones != null) this.telefones.remove(telefone);
    }

    public void addDependente(Dependente dependente) {
        if (this.dependentes == null) this.dependentes = new ArrayList<>();
        if (dependente != null && !this.dependentes.contains(dependente)) {
            this.dependentes.add(dependente);
        }
    }
    public void removeDependente(Dependente dependente) {
        if (this.dependentes != null) this.dependentes.remove(dependente);
    }

    @Override
    public String toString() {
        return "Funcionario [id=" + id + ", nome=" + nome + ", cpf=" + cpf +
                ", salario=" + (salario != null ? salario.toPlainString() : "N/A") +
                ", dataContratacao=" + (dataContratacao != null ? dataContratacao.toString() : "N/A") + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Funcionario)) return false;
        Funcionario that = (Funcionario) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}