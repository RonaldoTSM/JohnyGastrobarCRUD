package johnygastrobar.dao;

import johnygastrobar.model.Item;
import johnygastrobar.util.ConnectionFactory; // Continua sendo usada para listarTodos ou buscarPorId sem conexão externa

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // MÉTODO INSERIR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public int inserir(Item item, Connection conn) throws SQLException { // Lança SQLException
        String sql = "INSERT INTO Item (nome, tipo, preco) VALUES (?, ?, ?)";
        int idGerado = -1;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getTipo());
            stmt.setDouble(3, item.getPreco());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                idGerado = rs.getInt(1);
                System.out.println("Item inserido com ID: " + idGerado);
            } else {
                throw new SQLException("Falha ao obter o ID gerado para o Item.");
            }
        }
        return idGerado;
    }

    // MÉTODO LISTAR TODOS CONTINUA ABRINDO SUA PRÓPRIA CONEXÃO
    public List<Item> listarTodos() {
        List<Item> lista = new ArrayList<>();
        String sql = "SELECT id_item, nome, tipo, preco FROM Item ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Item(
                        rs.getInt("id_item"),
                        rs.getString("nome"),
                        rs.getString("tipo"),
                        rs.getDouble("preco")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar itens: " + e.getMessage());
        }
        return lista;
    }

    // MÉTODO ATUALIZAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void atualizar(Item item, Connection conn) throws SQLException { // Lança SQLException
        String sql = "UPDATE Item SET nome = ?, tipo = ?, preco = ? WHERE id_item = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getTipo());
            stmt.setDouble(3, item.getPreco());
            stmt.setInt(4, item.getIdItem());
            stmt.executeUpdate();
            System.out.println("Item com ID " + item.getIdItem() + " atualizado com sucesso.");
        }
    }

    // MÉTODO DELETAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void deletar(int idItem, Connection conn) throws SQLException { // Lança SQLException
        String sql = "DELETE FROM Item WHERE id_item = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idItem);
            stmt.executeUpdate();
            System.out.println("Item com ID " + idItem + " deletado com sucesso.");
        }
    }

    // MÉTODO BUSCAR POR ID AGORA RECEBE UMA CONEXÃO EXISTENTE (ou abre uma nova se null)
    public Item buscarPorId(int idItem, Connection externalConn) throws SQLException {
        Connection conn = null;
        try {
            if (externalConn != null) {
                conn = externalConn;
            } else {
                conn = ConnectionFactory.getConnection();
            }
            String sql = "SELECT id_item, nome, tipo, preco FROM Item WHERE id_item = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idItem);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new Item(
                            rs.getInt("id_item"),
                            rs.getString("nome"),
                            rs.getString("tipo"),
                            rs.getDouble("preco")
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
    public Item buscarPorId(int idItem) {
        try {
            return buscarPorId(idItem, null);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar item por ID (sem conexão externa): " + e.getMessage());
            return null;
        }
    }
}