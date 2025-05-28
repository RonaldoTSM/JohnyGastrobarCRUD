package johnygastrobar.dao;

import johnygastrobar.model.FeedbackPedido;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // Para as médias
import java.math.RoundingMode; // Para as médias
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date; // Para LocalDate
import java.time.LocalDate; // Para os novos métodos
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FeedbackPedidoDAO {

    public FeedbackPedido inserir(FeedbackPedido feedback, Connection conn) throws SQLException {
        String sql = "INSERT INTO Feedback_Pedido (id_pedido, id_mesa, nome_cliente_feedback, nota_comida, nota_atendimento, comentario_texto, data_feedback) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, feedback.getIdPedido());
            stmt.setInt(2, feedback.getIdMesa());
            stmt.setString(3, feedback.getNomeClienteFeedback());
            stmt.setObject(4, feedback.getNotaComida(), Types.INTEGER);
            stmt.setObject(5, feedback.getNotaAtendimento(), Types.INTEGER);
            stmt.setString(6, feedback.getComentarioTexto());
            stmt.setTimestamp(7, Timestamp.valueOf(feedback.getDataFeedback()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir feedback, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setIdFeedback(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir feedback, não foi possível obter o ID gerado.");
                }
            }
        }
        System.out.println("Feedback ID " + feedback.getIdFeedback() + " inserido para Pedido ID " + feedback.getIdPedido());
        return feedback;
    }

    public boolean atualizar(FeedbackPedido feedback, Connection conn) throws SQLException {
        String sql = "UPDATE Feedback_Pedido SET id_pedido = ?, id_mesa = ?, nome_cliente_feedback = ?, nota_comida = ?, nota_atendimento = ?, comentario_texto = ?, data_feedback = ? WHERE id_feedback = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, feedback.getIdPedido());
            stmt.setInt(2, feedback.getIdMesa());
            stmt.setString(3, feedback.getNomeClienteFeedback());
            stmt.setObject(4, feedback.getNotaComida(), Types.INTEGER);
            stmt.setObject(5, feedback.getNotaAtendimento(), Types.INTEGER);
            stmt.setString(6, feedback.getComentarioTexto());
            stmt.setTimestamp(7, Timestamp.valueOf(feedback.getDataFeedback()));
            stmt.setInt(8, feedback.getIdFeedback());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Feedback ID " + feedback.getIdFeedback() + " atualizado.");
            }
            return affectedRows > 0;
        }
    }

    public boolean deletar(int idFeedback, Connection conn) throws SQLException {
        String sql = "DELETE FROM Feedback_Pedido WHERE id_feedback = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFeedback);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Feedback ID " + idFeedback + " deletado.");
            }
            return affectedRows > 0;
        }
    }

    public FeedbackPedido buscarPorId(int idFeedback, Connection conn) throws SQLException {
        String sql = "SELECT id_feedback, id_pedido, id_mesa, nome_cliente_feedback, nota_comida, nota_atendimento, comentario_texto, data_feedback FROM Feedback_Pedido WHERE id_feedback = ?";
        FeedbackPedido feedback = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFeedback);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    feedback = new FeedbackPedido(
                            rs.getInt("id_feedback"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_mesa"),
                            rs.getString("nome_cliente_feedback"),
                            (Integer) rs.getObject("nota_comida"),
                            (Integer) rs.getObject("nota_atendimento"),
                            rs.getString("comentario_texto"),
                            rs.getTimestamp("data_feedback").toLocalDateTime()
                    );
                }
            }
        }
        return feedback;
    }

    public FeedbackPedido buscarPorId(int idFeedback) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idFeedback, conn);
        }
    }

    public List<FeedbackPedido> listarTodos(Connection conn) throws SQLException {
        List<FeedbackPedido> lista = new ArrayList<>();
        String sql = "SELECT id_feedback, id_pedido, id_mesa, nome_cliente_feedback, nota_comida, nota_atendimento, comentario_texto, data_feedback FROM Feedback_Pedido ORDER BY data_feedback DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new FeedbackPedido(
                        rs.getInt("id_feedback"),
                        rs.getInt("id_pedido"),
                        rs.getInt("id_mesa"),
                        rs.getString("nome_cliente_feedback"),
                        (Integer) rs.getObject("nota_comida"),
                        (Integer) rs.getObject("nota_atendimento"),
                        rs.getString("comentario_texto"),
                        rs.getTimestamp("data_feedback").toLocalDateTime()
                ));
            }
        }
        return lista;
    }

    public List<FeedbackPedido> listarTodos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarTodos(conn);
        }
    }

    public List<FeedbackPedido> listarPorPedido(int idPedido, Connection conn) throws SQLException {
        List<FeedbackPedido> lista = new ArrayList<>();
        String sql = "SELECT id_feedback, id_pedido, id_mesa, nome_cliente_feedback, nota_comida, nota_atendimento, comentario_texto, data_feedback FROM Feedback_Pedido WHERE id_pedido = ? ORDER BY data_feedback DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FeedbackPedido(
                            rs.getInt("id_feedback"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_mesa"),
                            rs.getString("nome_cliente_feedback"),
                            (Integer) rs.getObject("nota_comida"),
                            (Integer) rs.getObject("nota_atendimento"),
                            rs.getString("comentario_texto"),
                            rs.getTimestamp("data_feedback").toLocalDateTime()
                    ));
                }
            }
        }
        return lista;
    }

    public List<FeedbackPedido> listarPorPedido(int idPedido) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarPorPedido(idPedido, conn);
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD ---
    public BigDecimal avgNotaComidaPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT AVG(CAST(nota_comida AS DECIMAL(10,2))) FROM Feedback_Pedido WHERE DATE(data_feedback) BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataInicial));
            stmt.setDate(2, Date.valueOf(dataFinal));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal avg = rs.getBigDecimal(1);
                    return (avg == null) ? null : avg.setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return null; // Retorna null se não houver notas no período
    }

    public BigDecimal avgNotaAtendimentoPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT AVG(CAST(nota_atendimento AS DECIMAL(10,2))) FROM Feedback_Pedido WHERE DATE(data_feedback) BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataInicial));
            stmt.setDate(2, Date.valueOf(dataFinal));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal avg = rs.getBigDecimal(1);
                    return (avg == null) ? null : avg.setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return null; // Retorna null se não houver notas no período
    }

    public int countFeedbacksPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(id_feedback) FROM Feedback_Pedido WHERE DATE(data_feedback) BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataInicial));
            stmt.setDate(2, Date.valueOf(dataFinal));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}