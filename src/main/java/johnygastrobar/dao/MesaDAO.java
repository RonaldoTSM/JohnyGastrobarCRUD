package johnygastrobar.dao;

import johnygastrobar.model.Mesa;
import johnygastrobar.util.ConnectionFactory; // Continua sendo usada para listarTodos ou buscarPorId sem conexão externa

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {

    // MÉTODO INSERIR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public int inserir(Mesa mesa, Connection conn) throws SQLException { // Lança SQLException
        String sql = "INSERT INTO Mesa (capacidade, localizacao) VALUES (?, ?)";
        int idGerado = -1;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, mesa.getCapacidade());
            stmt.setString(2, mesa.getLocalizacao());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                idGerado = rs.getInt(1);
                System.out.println("Mesa inserida com ID: " + idGerado);
            } else {
                throw new SQLException("Falha ao obter o ID gerado para a Mesa.");
            }
        }
        return idGerado;
    }

    // MÉTODO LISTAR TODOS CONTINUA ABRINDO SUA PRÓPRIA CONEXÃO
    public List<Mesa> listarTodos() {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT id_mesa, capacidade, localizacao FROM Mesa ORDER BY id_mesa";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Mesa(
                        rs.getInt("id_mesa"),
                        rs.getInt("capacidade"),
                        rs.getString("localizacao")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar mesas: " + e.getMessage());
        }
        return lista;
    }

    // MÉTODO ATUALIZAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void atualizar(Mesa mesa, Connection conn) throws SQLException { // Lança SQLException
        String sql = "UPDATE Mesa SET capacidade = ?, localizacao = ? WHERE id_mesa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mesa.getCapacidade());
            stmt.setString(2, mesa.getLocalizacao());
            stmt.setInt(3, mesa.getIdMesa());
            stmt.executeUpdate();
            System.out.println("Mesa ID " + mesa.getIdMesa() + " atualizada com sucesso.");
        }
    }

    // MÉTODO DELETAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void deletar(int idMesa, Connection conn) throws SQLException { // Lança SQLException
        String sql = "DELETE FROM Mesa WHERE id_mesa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMesa);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Mesa ID " + idMesa + " deletada com sucesso.");
            } else {
                System.out.println("Nenhuma mesa encontrada com ID " + idMesa + " para deletar.");
            }
        }
    }

    // MÉTODO BUSCAR POR ID AGORA RECEBE UMA CONEXÃO EXISTENTE (ou abre uma nova se null)
    public Mesa buscarPorId(int idMesa, Connection externalConn) throws SQLException {
        Connection conn = null;
        try {
            if (externalConn != null) {
                conn = externalConn;
            } else {
                conn = ConnectionFactory.getConnection();
            }
            String sql = "SELECT id_mesa, capacidade, localizacao FROM Mesa WHERE id_mesa = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idMesa);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new Mesa(
                            rs.getInt("id_mesa"),
                            rs.getInt("capacidade"),
                            rs.getString("localizacao")
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
    public Mesa buscarPorId(int idMesa) {
        try {
            return buscarPorId(idMesa, null);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar mesa por ID (sem conexão externa): " + e.getMessage());
            return null;
        }
    }
}