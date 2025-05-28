package johnygastrobar.dao;

import johnygastrobar.model.Item;
import johnygastrobar.model.TopItemInfo; // IMPORT ADICIONADO
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // Para LocalDate
import java.time.LocalDate; // Para os novos métodos
import java.util.ArrayList;
import java.util.List;

@Repository
public class ItemDAO {

    public Item inserir(Item item, Connection conn) throws SQLException {
        String sql = "INSERT INTO Item (nome, tipo, preco) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getTipo());
            stmt.setBigDecimal(3, item.getPreco());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir item, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setIdItem(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir item, não foi possível obter o ID gerado.");
                }
            }
        }
        System.out.println("Item '" + item.getNome() + "' inserido com ID: " + item.getIdItem());
        return item;
    }

    public boolean atualizar(Item item, Connection conn) throws SQLException {
        String sql = "UPDATE Item SET nome = ?, tipo = ?, preco = ? WHERE id_item = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getTipo());
            stmt.setBigDecimal(3, item.getPreco());
            stmt.setInt(4, item.getIdItem());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Item ID " + item.getIdItem() + " atualizado.");
            }
            return affectedRows > 0;
        }
    }

    public boolean deletar(int idItem, Connection conn) throws SQLException {
        String sql = "DELETE FROM Item WHERE id_item = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idItem);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Item ID " + idItem + " deletado.");
            }
            return affectedRows > 0;
        }
    }

    public Item buscarPorId(int idItem, Connection conn) throws SQLException {
        String sql = "SELECT id_item, nome, tipo, preco FROM Item WHERE id_item = ?";
        Item item = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idItem);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    item = new Item(
                            rs.getInt("id_item"),
                            rs.getString("nome"),
                            rs.getString("tipo"),
                            rs.getBigDecimal("preco")
                    );
                }
            }
        }
        return item;
    }

    public Item buscarPorId(int idItem) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idItem, conn);
        }
    }

    public List<Item> listarTodos(Connection conn) throws SQLException {
        List<Item> lista = new ArrayList<>();
        String sql = "SELECT id_item, nome, tipo, preco FROM Item ORDER BY nome";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Item(
                        rs.getInt("id_item"),
                        rs.getString("nome"),
                        rs.getString("tipo"),
                        rs.getBigDecimal("preco")
                ));
            }
        }
        return lista;
    }

    public List<Item> listarTodos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarTodos(conn);
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD ---

    public List<TopItemInfo> getTopItensMaisVendidosPorQuantidade(LocalDate dataInicial, LocalDate dataFinal, int limite, Connection conn) throws SQLException {
        List<TopItemInfo> topItens = new ArrayList<>();
        String sql = "SELECT i.nome, SUM(pi.quantidade) as total_quantidade " +
                "FROM Pedido_Item pi " +
                "JOIN Item i ON pi.id_item = i.id_item " +
                "JOIN Pedido p ON pi.id_pedido = p.id_pedido " +
                "WHERE DATE(p.data_hora) BETWEEN ? AND ? " +
                "GROUP BY i.id_item, i.nome " +
                "ORDER BY total_quantidade DESC " +
                "LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataInicial));
            stmt.setDate(2, Date.valueOf(dataFinal));
            stmt.setInt(3, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topItens.add(new TopItemInfo(rs.getString("nome"), rs.getBigDecimal("total_quantidade")));
                }
            }
        }
        return topItens;
    }

    public List<TopItemInfo> getTopItensMaisRentaveis(LocalDate dataInicial, LocalDate dataFinal, int limite, Connection conn) throws SQLException {
        List<TopItemInfo> topItens = new ArrayList<>();
        String sql = "SELECT i.nome, SUM(pi.quantidade * pi.preco_unitario) as faturamento_total " +
                "FROM Pedido_Item pi " +
                "JOIN Item i ON pi.id_item = i.id_item " +
                "JOIN Pedido p ON pi.id_pedido = p.id_pedido " +
                "WHERE DATE(p.data_hora) BETWEEN ? AND ? " +
                "GROUP BY i.id_item, i.nome " +
                "ORDER BY faturamento_total DESC " +
                "LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataInicial));
            stmt.setDate(2, Date.valueOf(dataFinal));
            stmt.setInt(3, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topItens.add(new TopItemInfo(rs.getString("nome"), rs.getBigDecimal("faturamento_total")));
                }
            }
        }
        return topItens;
    }
}