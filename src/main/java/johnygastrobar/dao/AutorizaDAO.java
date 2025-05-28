package johnygastrobar.dao;

import johnygastrobar.model.Autoriza;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AutorizaDAO {

    public Autoriza inserir(Autoriza autoriza, Connection conn) throws SQLException {
        String sql = "INSERT INTO Autoriza (id_pedido, id_gerente, data_autorizacao, observacao_autorizacao) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, autoriza.getIdPedido());
            stmt.setInt(2, autoriza.getIdGerente());
            stmt.setTimestamp(3, Timestamp.valueOf(autoriza.getDataAutorizacao()));
            stmt.setString(4, autoriza.getObservacaoAutorizacao());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir autorização, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    autoriza.setIdAutorizacao(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir autorização, não foi possível obter o ID gerado.");
                }
            }
        }
        System.out.println("Autorização ID " + autoriza.getIdAutorizacao() + " inserida para Pedido ID " + autoriza.getIdPedido());
        return autoriza;
    }

    public boolean atualizar(Autoriza autoriza, Connection conn) throws SQLException {
        String sql = "UPDATE Autoriza SET id_pedido = ?, id_gerente = ?, data_autorizacao = ?, observacao_autorizacao = ? WHERE id_autorizacao = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, autoriza.getIdPedido());
            stmt.setInt(2, autoriza.getIdGerente());
            stmt.setTimestamp(3, Timestamp.valueOf(autoriza.getDataAutorizacao()));
            stmt.setString(4, autoriza.getObservacaoAutorizacao());
            stmt.setInt(5, autoriza.getIdAutorizacao());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Autorização ID " + autoriza.getIdAutorizacao() + " atualizada.");
            }
            return affectedRows > 0;
        }
    }

    public boolean deletar(int idAutorizacao, Connection conn) throws SQLException {
        String sql = "DELETE FROM Autoriza WHERE id_autorizacao = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAutorizacao);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Autorização ID " + idAutorizacao + " deletada.");
            }
            return affectedRows > 0;
        }
    }

    public Autoriza buscarPorId(int idAutorizacao, Connection conn) throws SQLException {
        String sql = "SELECT id_autorizacao, id_pedido, id_gerente, data_autorizacao, observacao_autorizacao FROM Autoriza WHERE id_autorizacao = ?";
        Autoriza autoriza = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAutorizacao);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    autoriza = new Autoriza(
                            rs.getInt("id_autorizacao"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_gerente"),
                            rs.getTimestamp("data_autorizacao").toLocalDateTime(),
                            rs.getString("observacao_autorizacao")
                    );
                }
            }
        }
        return autoriza;
    }

    public Autoriza buscarPorId(int idAutorizacao) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idAutorizacao, conn);
        }
    }

    public List<Autoriza> listarTodos(Connection conn) throws SQLException {
        List<Autoriza> lista = new ArrayList<>();
        String sql = "SELECT id_autorizacao, id_pedido, id_gerente, data_autorizacao, observacao_autorizacao FROM Autoriza ORDER BY data_autorizacao DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Autoriza(
                        rs.getInt("id_autorizacao"),
                        rs.getInt("id_pedido"),
                        rs.getInt("id_gerente"),
                        rs.getTimestamp("data_autorizacao").toLocalDateTime(),
                        rs.getString("observacao_autorizacao")
                ));
            }
        }
        return lista;
    }

    public List<Autoriza> listarTodos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarTodos(conn);
        }
    }

    public List<Autoriza> listarPorPedido(int idPedido, Connection conn) throws SQLException {
        List<Autoriza> lista = new ArrayList<>();
        String sql = "SELECT id_autorizacao, id_pedido, id_gerente, data_autorizacao, observacao_autorizacao FROM Autoriza WHERE id_pedido = ? ORDER BY data_autorizacao DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Autoriza(
                            rs.getInt("id_autorizacao"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_gerente"),
                            rs.getTimestamp("data_autorizacao").toLocalDateTime(),
                            rs.getString("observacao_autorizacao")
                    ));
                }
            }
        }
        return lista;
    }

    public List<Autoriza> listarPorPedido(int idPedido) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarPorPedido(idPedido, conn);
        }
    }

    public List<Autoriza> listarPorGerente(int idGerente, Connection conn) throws SQLException {
        List<Autoriza> lista = new ArrayList<>();
        String sql = "SELECT id_autorizacao, id_pedido, id_gerente, data_autorizacao, observacao_autorizacao FROM Autoriza WHERE id_gerente = ? ORDER BY data_autorizacao DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idGerente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Autoriza(
                            rs.getInt("id_autorizacao"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_gerente"),
                            rs.getTimestamp("data_autorizacao").toLocalDateTime(),
                            rs.getString("observacao_autorizacao")
                    ));
                }
            }
        }
        return lista;
    }

    public List<Autoriza> listarPorGerente(int idGerente) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarPorGerente(idGerente, conn);
        }
    }
}