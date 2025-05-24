package johnygastrobar.dao;

import johnygastrobar.model.Funcionario;
import johnygastrobar.util.ConnectionFactory; // Continua sendo usada para métodos que abrem sua própria conexão (ex: listarTodos)

import java.sql.Connection;
import java.sql.Date; // Para data_contratacao
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    // MÉTODO INSERIR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public int inserir(Funcionario f, Connection conn) throws SQLException { // Lança SQLException para ser gerenciada externamente
        String sql = "INSERT INTO Funcionario (nome, cpf, salario, data_contratacao) VALUES (?, ?, ?, CURDATE())";
        int idGerado = -1;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, f.getNome());
            stmt.setString(2, f.getCpf());
            stmt.setDouble(3, f.getSalario());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                idGerado = rs.getInt(1);
                System.out.println("Funcionário inserido com ID: " + idGerado);
            } else {
                throw new SQLException("Falha ao obter o ID gerado para o Funcionário.");
            }
        }
        return idGerado;
    }

    // MÉTODO LISTAR TODOS CONTINUA ABRINDO SUA PRÓPRIA CONEXÃO
    public List<Funcionario> listarTodos() {
        List<Funcionario> lista = new ArrayList<>();
        String sql = "SELECT id_funcionario, nome, cpf, salario FROM Funcionario ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Funcionario(
                        rs.getInt("id_funcionario"),
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getDouble("salario")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar funcionários: " + e.getMessage());
        }
        return lista;
    }

    // MÉTODO ATUALIZAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void atualizarSalario(int id, double novoSalario, Connection conn) throws SQLException { // Lança SQLException
        String sql = "UPDATE Funcionario SET salario = ? WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novoSalario);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println("Salário do funcionário ID " + id + " atualizado.");
        }
    }

    // MÉTODO DELETAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void deletar(int id, Connection conn) throws SQLException { // Lança SQLException
        String sql = "DELETE FROM Funcionario WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Funcionário ID " + id + " deletado.");
        }
    }

    // MÉTODO BUSCAR POR ID AGORA RECEBE UMA CONEXÃO EXISTENTE (ou abre uma nova se null)
    public Funcionario buscarPorId(int idFuncionario, Connection externalConn) throws SQLException {
        Connection conn = null;
        try {
            if (externalConn != null) {
                conn = externalConn; // Usa a conexão externa
            } else {
                conn = ConnectionFactory.getConnection(); // Abre nova conexão se não houver externa
            }

            String sql = "SELECT id_funcionario, nome, cpf, salario, data_contratacao FROM Funcionario WHERE id_funcionario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idFuncionario);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // Crie um construtor em Funcionario que aceite dataContratacao se for usar
                    return new Funcionario(
                            rs.getInt("id_funcionario"),
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getDouble("salario")
                            // rs.getDate("data_contratacao").toLocalDate() // Se construtor incluir data
                    );
                }
            }
        } finally {
            if (externalConn == null && conn != null) { // Fecha a conexão se ela foi aberta por este método
                conn.close();
            }
        }
        return null;
    }

    // Sobrecarga para compatibilidade, manterá a função original que abre e fecha a conexão
    public Funcionario buscarPorId(int idFuncionario) {
        try {
            return buscarPorId(idFuncionario, null);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar funcionário por ID (sem conexão externa): " + e.getMessage());
            return null;
        }
    }
}