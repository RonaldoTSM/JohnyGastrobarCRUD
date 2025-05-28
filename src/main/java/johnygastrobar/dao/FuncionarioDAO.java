package johnygastrobar.dao;

import johnygastrobar.model.*;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // IMPORT ADICIONADO
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Repository
public class FuncionarioDAO {

    // --- MÉTODOS AUXILIARES PARA TELEFONES (sem alteração de tipo aqui) ---
    private void inserirTelefones(int idFuncionario, List<String> telefones, Connection conn) throws SQLException {
        if (telefones == null || telefones.isEmpty()) return;
        String sql = "INSERT INTO Funcionario_Telefones (id_funcionario, numero_telefone) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String telefone : telefones) {
                if (telefone != null && !telefone.trim().isEmpty()) {
                    stmt.setInt(1, idFuncionario);
                    stmt.setString(2, telefone.trim());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    private void deletarTelefones(int idFuncionario, Connection conn) throws SQLException {
        String sql = "DELETE FROM Funcionario_Telefones WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);
            stmt.executeUpdate();
        }
    }

    private List<String> getTelefonesPorIdFuncionario(int idFuncionario, Connection conn) throws SQLException {
        List<String> telefones = new ArrayList<>();
        String sql = "SELECT numero_telefone FROM Funcionario_Telefones WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    telefones.add(rs.getString("numero_telefone"));
                }
            }
        }
        return telefones;
    }

    // --- MÉTODOS AUXILIARES PARA DEPENDENTES (sem alteração de tipo aqui) ---
    private void inserirDependentes(int idFuncionario, List<Dependente> dependentes, Connection conn) throws SQLException {
        if (dependentes == null || dependentes.isEmpty()) return;
        String sql = "INSERT INTO Dependente (id_funcionario, nome_dependente, data_nascimento, parentesco) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Dependente dep : dependentes) {
                stmt.setInt(1, idFuncionario);
                stmt.setString(2, dep.getNomeDependente());
                stmt.setDate(3, dep.getDataNascimento() != null ? Date.valueOf(dep.getDataNascimento()) : null);
                stmt.setString(4, dep.getParentesco());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void deletarDependentes(int idFuncionario, Connection conn) throws SQLException {
        String sql = "DELETE FROM Dependente WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);
            stmt.executeUpdate();
        }
    }

    private List<Dependente> getDependentesPorIdFuncionario(int idFuncionario, Connection conn) throws SQLException {
        List<Dependente> dependentes = new ArrayList<>();
        String sql = "SELECT nome_dependente, data_nascimento, parentesco FROM Dependente WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date dataNascSql = rs.getDate("data_nascimento");
                    dependentes.add(new Dependente(
                            idFuncionario,
                            rs.getString("nome_dependente"),
                            dataNascSql != null ? dataNascSql.toLocalDate() : null,
                            rs.getString("parentesco")
                    ));
                }
            }
        }
        return dependentes;
    }

    // --- IDENTIFICAR TIPO DE FUNCIONÁRIO (sem alteração) ---
    public String getTipoFuncionario(int idFuncionario, Connection conn) throws SQLException {
        if (checkIfExistsInSpecialization(idFuncionario, "Garcom", conn)) return "Garcom";
        if (checkIfExistsInSpecialization(idFuncionario, "Cozinheiro", conn)) return "Cozinheiro";
        if (checkIfExistsInSpecialization(idFuncionario, "Bartender", conn)) return "Bartender";
        if (checkIfExistsInSpecialization(idFuncionario, "Gerente", conn)) return "Gerente";
        if (checkIfExistsInSpecialization(idFuncionario, "Funcionario", conn)) return "FuncionarioBase";
        return null;
    }

    private boolean checkIfExistsInSpecialization(int idFuncionario, String tableName, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM " + tableName + " WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // --- OPERAÇÕES CRUD ATUALIZADAS PARA BigDecimal ---

    public Funcionario inserir(Funcionario funcionario, Connection conn) throws SQLException {
        String sqlFuncionario = "INSERT INTO Funcionario (nome, cpf, salario, data_contratacao, rua, numero, bairro, cidade, estado, cep, id_supervisor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int idGerado;

        try (PreparedStatement stmt = conn.prepareStatement(sqlFuncionario, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getCpf());
            stmt.setBigDecimal(3, funcionario.getSalario()); // ALTERADO para setBigDecimal
            stmt.setDate(4, funcionario.getDataContratacao() != null ? Date.valueOf(funcionario.getDataContratacao()) : null);
            stmt.setString(5, funcionario.getRua());
            stmt.setString(6, funcionario.getNumero());
            stmt.setString(7, funcionario.getBairro());
            stmt.setString(8, funcionario.getCidade());
            stmt.setString(9, funcionario.getEstado());
            stmt.setString(10, funcionario.getCep());
            stmt.setObject(11, funcionario.getIdSupervisor(), Types.INTEGER);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir funcionário (base), nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    idGerado = rs.getInt(1);
                    funcionario.setId(idGerado);
                } else {
                    throw new SQLException("Falha ao obter o ID gerado para o Funcionário.");
                }
            }
        }

        if (funcionario instanceof Garcom) {
            Garcom garcom = (Garcom) funcionario;
            String sql = "INSERT INTO Garcom (id_funcionario, setor_atendimento) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idGerado);
                stmt.setString(2, garcom.getSetorAtendimento());
                stmt.executeUpdate();
            }
        } else if (funcionario instanceof Cozinheiro) {
            Cozinheiro cozinheiro = (Cozinheiro) funcionario;
            String sql = "INSERT INTO Cozinheiro (id_funcionario, espec_cul) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idGerado);
                stmt.setString(2, cozinheiro.getEspecialidadeCulinaria());
                stmt.executeUpdate();
            }
        } else if (funcionario instanceof Bartender) {
            Bartender bartender = (Bartender) funcionario;
            String sql = "INSERT INTO Bartender (id_funcionario, espec_bar) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idGerado);
                stmt.setString(2, bartender.getEspecialidadeBar());
                stmt.executeUpdate();
            }
        } else if (funcionario instanceof Gerente) {
            Gerente gerente = (Gerente) funcionario;
            String sql = "INSERT INTO Gerente (id_funcionario, nivel_acesso, limite_desconto) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idGerado);
                stmt.setString(2, gerente.getNivelAcesso());
                stmt.setBigDecimal(3, gerente.getLimiteDesconto()); // ALTERADO para setBigDecimal
                stmt.executeUpdate();
            }
        }

        inserirTelefones(idGerado, funcionario.getTelefones(), conn);
        inserirDependentes(idGerado, funcionario.getDependentes(), conn);

        System.out.println("Funcionário ID " + idGerado + " do tipo " + funcionario.getClass().getSimpleName() + " inserido.");
        return funcionario;
    }

    public Funcionario buscarPorId(int idFuncionario, Connection conn) throws SQLException {
        Funcionario funcionario = null;
        String sqlBase = "SELECT f.id_funcionario, f.nome, f.cpf, f.salario, f.data_contratacao, " +
                "f.rua, f.numero, f.bairro, f.cidade, f.estado, f.cep, " +
                "f.id_supervisor, s.nome AS nome_supervisor " +
                "FROM Funcionario f LEFT JOIN Funcionario s ON f.id_supervisor = s.id_funcionario " +
                "WHERE f.id_funcionario = ?";

        try (PreparedStatement stmtBase = conn.prepareStatement(sqlBase)) {
            stmtBase.setInt(1, idFuncionario);
            try (ResultSet rsBase = stmtBase.executeQuery()) {
                if (rsBase.next()) {
                    int id = rsBase.getInt("f.id_funcionario");
                    String nome = rsBase.getString("f.nome");
                    String cpf = rsBase.getString("f.cpf");
                    BigDecimal salario = rsBase.getBigDecimal("f.salario"); // ALTERADO para getBigDecimal
                    Date sqlDataContratacao = rsBase.getDate("f.data_contratacao");
                    LocalDate dataContratacao = (sqlDataContratacao != null) ? sqlDataContratacao.toLocalDate() : null;
                    String rua = rsBase.getString("f.rua");
                    String numero = rsBase.getString("f.numero");
                    String bairro = rsBase.getString("f.bairro");
                    String cidade = rsBase.getString("f.cidade");
                    String estado = rsBase.getString("f.estado");
                    String cep = rsBase.getString("f.cep");
                    Integer idSupervisor = (Integer) rsBase.getObject("f.id_supervisor");
                    String nomeSupervisor = rsBase.getString("nome_supervisor");

                    String tipo = getTipoFuncionario(id, conn);

                    if ("Garcom".equals(tipo)) {
                        String sqlEsp = "SELECT setor_atendimento FROM Garcom WHERE id_funcionario = ?";
                        try (PreparedStatement stmtEsp = conn.prepareStatement(sqlEsp)) {
                            stmtEsp.setInt(1, id);
                            try (ResultSet rsEsp = stmtEsp.executeQuery()) {
                                if (rsEsp.next()) {
                                    funcionario = new Garcom(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, rsEsp.getString("setor_atendimento"));
                                }
                            }
                        }
                    } else if ("Cozinheiro".equals(tipo)) {
                        String sqlEsp = "SELECT espec_cul FROM Cozinheiro WHERE id_funcionario = ?";
                        try (PreparedStatement stmtEsp = conn.prepareStatement(sqlEsp)) {
                            stmtEsp.setInt(1, id);
                            try (ResultSet rsEsp = stmtEsp.executeQuery()) {
                                if (rsEsp.next()) {
                                    funcionario = new Cozinheiro(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, rsEsp.getString("espec_cul"));
                                }
                            }
                        }
                    } else if ("Bartender".equals(tipo)) {
                        String sqlEsp = "SELECT espec_bar FROM Bartender WHERE id_funcionario = ?";
                        try (PreparedStatement stmtEsp = conn.prepareStatement(sqlEsp)) {
                            stmtEsp.setInt(1, id);
                            try (ResultSet rsEsp = stmtEsp.executeQuery()) {
                                if (rsEsp.next()) {
                                    funcionario = new Bartender(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, rsEsp.getString("espec_bar"));
                                }
                            }
                        }
                    } else if ("Gerente".equals(tipo)) {
                        String sqlEsp = "SELECT nivel_acesso, limite_desconto FROM Gerente WHERE id_funcionario = ?";
                        try (PreparedStatement stmtEsp = conn.prepareStatement(sqlEsp)) {
                            stmtEsp.setInt(1, id);
                            try (ResultSet rsEsp = stmtEsp.executeQuery()) {
                                if (rsEsp.next()) {
                                    funcionario = new Gerente(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, rsEsp.getString("nivel_acesso"), rsEsp.getBigDecimal("limite_desconto")); // ALTERADO para getBigDecimal
                                }
                            }
                        }
                    } else {
                        funcionario = new Funcionario(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor);
                    }

                    if (funcionario != null) {
                        funcionario.setNomeSupervisor(nomeSupervisor);
                        funcionario.setTelefones(getTelefonesPorIdFuncionario(id, conn));
                        funcionario.setDependentes(getDependentesPorIdFuncionario(id, conn));
                    }
                }
            }
        }
        return funcionario;
    }

    public Funcionario buscarPorId(int idFuncionario) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idFuncionario, conn);
        }
    }

    // Classe auxiliar interna para dados do Gerente em listarTodos
    private static class GerenteData {
        String nivelAcesso;
        BigDecimal limiteDesconto; // ALTERADO para BigDecimal
        GerenteData(String nivel, BigDecimal limite) { this.nivelAcesso = nivel; this.limiteDesconto = limite; }
    }

    public List<Funcionario> listarTodos() throws SQLException {
        List<Funcionario> funcionarios = new ArrayList<>();
        Map<Integer, String> setoresGarcom = new HashMap<>();
        Map<Integer, String> especCulinarias = new HashMap<>();
        Map<Integer, String> especBares = new HashMap<>();
        Map<Integer, GerenteData> dadosGerente = new HashMap<>();

        try (Connection conn = ConnectionFactory.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT id_funcionario, setor_atendimento FROM Garcom")) {
                    while (rs.next()) setoresGarcom.put(rs.getInt("id_funcionario"), rs.getString("setor_atendimento"));
                }
                try (ResultSet rs = stmt.executeQuery("SELECT id_funcionario, espec_cul FROM Cozinheiro")) {
                    while (rs.next()) especCulinarias.put(rs.getInt("id_funcionario"), rs.getString("espec_cul"));
                }
                try (ResultSet rs = stmt.executeQuery("SELECT id_funcionario, espec_bar FROM Bartender")) {
                    while (rs.next()) especBares.put(rs.getInt("id_funcionario"), rs.getString("espec_bar"));
                }
                try (ResultSet rs = stmt.executeQuery("SELECT id_funcionario, nivel_acesso, limite_desconto FROM Gerente")) {
                    while (rs.next()) dadosGerente.put(rs.getInt("id_funcionario"), new GerenteData(rs.getString("nivel_acesso"), rs.getBigDecimal("limite_desconto"))); // ALTERADO para getBigDecimal
                }
            }

            String sqlBase = "SELECT f.id_funcionario, f.nome, f.cpf, f.salario, f.data_contratacao, " +
                    "f.rua, f.numero, f.bairro, f.cidade, f.estado, f.cep, " +
                    "f.id_supervisor, s.nome AS nome_supervisor " +
                    "FROM Funcionario f LEFT JOIN Funcionario s ON f.id_supervisor = s.id_funcionario ORDER BY f.nome";
            try (PreparedStatement stmtBase = conn.prepareStatement(sqlBase);
                 ResultSet rsBase = stmtBase.executeQuery()) {

                while (rsBase.next()) {
                    int id = rsBase.getInt("f.id_funcionario");
                    String nome = rsBase.getString("f.nome");
                    String cpf = rsBase.getString("f.cpf");
                    BigDecimal salario = rsBase.getBigDecimal("f.salario"); // ALTERADO para getBigDecimal
                    Date sqlDataContratacao = rsBase.getDate("f.data_contratacao");
                    LocalDate dataContratacao = (sqlDataContratacao != null) ? sqlDataContratacao.toLocalDate() : null;
                    String rua = rsBase.getString("f.rua");
                    String numero = rsBase.getString("f.numero");
                    String bairro = rsBase.getString("f.bairro");
                    String cidade = rsBase.getString("f.cidade");
                    String estado = rsBase.getString("f.estado");
                    String cep = rsBase.getString("f.cep");
                    Integer idSupervisor = (Integer) rsBase.getObject("f.id_supervisor");
                    String nomeSupervisor = rsBase.getString("nome_supervisor");

                    Funcionario func;
                    if (setoresGarcom.containsKey(id)) {
                        func = new Garcom(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, setoresGarcom.get(id));
                    } else if (especCulinarias.containsKey(id)) {
                        func = new Cozinheiro(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, especCulinarias.get(id));
                    } else if (especBares.containsKey(id)) {
                        func = new Bartender(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, especBares.get(id));
                    } else if (dadosGerente.containsKey(id)) {
                        GerenteData gd = dadosGerente.get(id);
                        func = new Gerente(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor, gd.nivelAcesso, gd.limiteDesconto);
                    } else {
                        func = new Funcionario(id, nome, cpf, salario, dataContratacao, rua, numero, bairro, cidade, estado, cep, idSupervisor);
                    }

                    func.setNomeSupervisor(nomeSupervisor);
                    func.setTelefones(getTelefonesPorIdFuncionario(id, conn));
                    func.setDependentes(getDependentesPorIdFuncionario(id, conn));
                    funcionarios.add(func);
                }
            }
        }
        return funcionarios;
    }

    public Funcionario atualizarCompleto(Funcionario funcionario, Connection conn) throws SQLException {
        String sqlUpdateBase = "UPDATE Funcionario SET nome = ?, cpf = ?, salario = ?, data_contratacao = ?, " +
                "rua = ?, numero = ?, bairro = ?, cidade = ?, estado = ?, cep = ?, id_supervisor = ? " +
                "WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateBase)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getCpf());
            stmt.setBigDecimal(3, funcionario.getSalario()); // ALTERADO para setBigDecimal
            stmt.setDate(4, funcionario.getDataContratacao() != null ? Date.valueOf(funcionario.getDataContratacao()) : null);
            stmt.setString(5, funcionario.getRua());
            stmt.setString(6, funcionario.getNumero());
            stmt.setString(7, funcionario.getBairro());
            stmt.setString(8, funcionario.getCidade());
            stmt.setString(9, funcionario.getEstado());
            stmt.setString(10, funcionario.getCep());
            stmt.setObject(11, funcionario.getIdSupervisor(), Types.INTEGER);
            stmt.setInt(12, funcionario.getId());
            stmt.executeUpdate();
        }

        String tipoAntigoNoBanco = getTipoFuncionario(funcionario.getId(), conn);
        String tipoNovoDoObjeto = null;
        if (funcionario instanceof Garcom) tipoNovoDoObjeto = "Garcom";
        else if (funcionario instanceof Cozinheiro) tipoNovoDoObjeto = "Cozinheiro";
        else if (funcionario instanceof Bartender) tipoNovoDoObjeto = "Bartender";
        else if (funcionario instanceof Gerente) tipoNovoDoObjeto = "Gerente";
        else tipoNovoDoObjeto = "FuncionarioBase";

        if (tipoAntigoNoBanco != null && !tipoAntigoNoBanco.equals("FuncionarioBase") && !tipoAntigoNoBanco.equalsIgnoreCase(tipoNovoDoObjeto)) {
            String sqlDeleteAntiga = "DELETE FROM " + tipoAntigoNoBanco + " WHERE id_funcionario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteAntiga)) {
                stmt.setInt(1, funcionario.getId());
                stmt.executeUpdate();
            }
        }

        boolean precisaInserirNovaEspecializacao = !tipoNovoDoObjeto.equals("FuncionarioBase") &&
                (tipoAntigoNoBanco == null || !tipoAntigoNoBanco.equalsIgnoreCase(tipoNovoDoObjeto) || "FuncionarioBase".equals(tipoAntigoNoBanco));

        if (funcionario instanceof Garcom) {
            Garcom garcom = (Garcom) funcionario;
            if (precisaInserirNovaEspecializacao) {
                String sql = "INSERT INTO Garcom (id_funcionario, setor_atendimento) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setInt(1, garcom.getId()); stmt.setString(2, garcom.getSetorAtendimento()); stmt.executeUpdate(); }
            } else if ("Garcom".equalsIgnoreCase(tipoAntigoNoBanco)) {
                String sql = "UPDATE Garcom SET setor_atendimento = ? WHERE id_funcionario = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setString(1, garcom.getSetorAtendimento()); stmt.setInt(2, garcom.getId()); stmt.executeUpdate(); }
            }
        } else if (funcionario instanceof Cozinheiro) {
            Cozinheiro cozinheiro = (Cozinheiro) funcionario;
            if (precisaInserirNovaEspecializacao) {
                String sql = "INSERT INTO Cozinheiro (id_funcionario, espec_cul) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setInt(1, cozinheiro.getId()); stmt.setString(2, cozinheiro.getEspecialidadeCulinaria()); stmt.executeUpdate(); }
            } else if ("Cozinheiro".equalsIgnoreCase(tipoAntigoNoBanco)) {
                String sql = "UPDATE Cozinheiro SET espec_cul = ? WHERE id_funcionario = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setString(1, cozinheiro.getEspecialidadeCulinaria()); stmt.setInt(2, cozinheiro.getId()); stmt.executeUpdate(); }
            }
        } else if (funcionario instanceof Bartender) {
            Bartender bartender = (Bartender) funcionario;
            if (precisaInserirNovaEspecializacao) {
                String sql = "INSERT INTO Bartender (id_funcionario, espec_bar) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setInt(1, bartender.getId()); stmt.setString(2, bartender.getEspecialidadeBar()); stmt.executeUpdate(); }
            } else if ("Bartender".equalsIgnoreCase(tipoAntigoNoBanco)) {
                String sql = "UPDATE Bartender SET espec_bar = ? WHERE id_funcionario = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setString(1, bartender.getEspecialidadeBar()); stmt.setInt(2, bartender.getId()); stmt.executeUpdate(); }
            }
        } else if (funcionario instanceof Gerente) {
            Gerente gerente = (Gerente) funcionario;
            if (precisaInserirNovaEspecializacao) {
                String sql = "INSERT INTO Gerente (id_funcionario, nivel_acesso, limite_desconto) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setInt(1, gerente.getId()); stmt.setString(2, gerente.getNivelAcesso()); stmt.setBigDecimal(3, gerente.getLimiteDesconto()); stmt.executeUpdate(); } // ALTERADO para setBigDecimal
            } else if ("Gerente".equalsIgnoreCase(tipoAntigoNoBanco)) {
                String sql = "UPDATE Gerente SET nivel_acesso = ?, limite_desconto = ? WHERE id_funcionario = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) { stmt.setString(1, gerente.getNivelAcesso()); stmt.setBigDecimal(2, gerente.getLimiteDesconto()); stmt.setInt(3, gerente.getId()); stmt.executeUpdate(); } // ALTERADO para setBigDecimal
            }
        }

        deletarTelefones(funcionario.getId(), conn);
        inserirTelefones(funcionario.getId(), funcionario.getTelefones(), conn);
        deletarDependentes(funcionario.getId(), conn);
        inserirDependentes(funcionario.getId(), funcionario.getDependentes(), conn);

        System.out.println("Funcionário ID " + funcionario.getId() + " atualizado completamente.");
        return funcionario;
    }

    public Funcionario atualizarCompleto(Funcionario funcionario) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                Funcionario atualizado = atualizarCompleto(funcionario, conn);
                conn.commit();
                return atualizado;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public void atualizarSalario(int id, BigDecimal novoSalario, Connection conn) throws SQLException { // Parâmetro BigDecimal
        String sql = "UPDATE Funcionario SET salario = ? WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, novoSalario); // ALTERADO para setBigDecimal
            stmt.setInt(2, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Funcionário com ID " + id + " não encontrado para atualizar salário.");
            }
            System.out.println("Salário do funcionário ID " + id + " atualizado.");
        }
    }

    public void atualizarEndereco(int id, String rua, String numero, String bairro, String cidade, String estado, String cep, Connection conn) throws SQLException {
        String sql = "UPDATE Funcionario SET rua = ?, numero = ?, bairro = ?, cidade = ?, estado = ?, cep = ? WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rua);
            stmt.setString(2, numero);
            stmt.setString(3, bairro);
            stmt.setString(4, cidade);
            stmt.setString(5, estado);
            stmt.setString(6, cep);
            stmt.setInt(7, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Funcionário com ID " + id + " não encontrado para atualizar endereço.");
            }
            System.out.println("Endereço do funcionário ID " + id + " atualizado.");
        }
    }

    public void deletar(int idFuncionario, Connection conn) throws SQLException {
        String sql = "DELETE FROM Funcionario WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Funcionário com ID " + idFuncionario + " não encontrado para deleção.");
            }
            System.out.println("Funcionário ID " + idFuncionario + " deletado.");
        }
    }

    public void deletar(int idFuncionario) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                deletar(idFuncionario, conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
    }
}