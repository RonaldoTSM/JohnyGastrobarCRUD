package johnygastrobar.dao;

import johnygastrobar.model.Pagamento;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date; // IMPORT NECESSÁRIO PARA Date.valueOf(LocalDate)
import java.time.LocalDate; // IMPORT NECESSÁRIO PARA os parâmetros do novo método
import java.util.ArrayList;
import java.util.List;

@Repository
public class PagamentoDAO {

    public Pagamento inserir(Pagamento pagamento, Connection conn) throws SQLException {
        String sql = "INSERT INTO Pagamento_realiza (id_pedido, valor_total, metodo_pagamento, data_pagamento) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pagamento.getIdPedido());
            stmt.setBigDecimal(2, pagamento.getValorTotal());
            stmt.setString(3, pagamento.getMetodoPagamento());
            stmt.setTimestamp(4, Timestamp.valueOf(pagamento.getDataPagamento()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir pagamento, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pagamento.setIdPagamento(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir pagamento, não foi possível obter o ID gerado.");
                }
            }
        }
        System.out.println("Pagamento ID " + pagamento.getIdPagamento() + " inserido para o Pedido ID " + pagamento.getIdPedido());
        return pagamento;
    }

    public boolean atualizar(Pagamento pagamento, Connection conn) throws SQLException {
        String sql = "UPDATE Pagamento_realiza SET id_pedido = ?, valor_total = ?, metodo_pagamento = ?, data_pagamento = ? WHERE id_pagamento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pagamento.getIdPedido());
            stmt.setBigDecimal(2, pagamento.getValorTotal());
            stmt.setString(3, pagamento.getMetodoPagamento());
            stmt.setTimestamp(4, Timestamp.valueOf(pagamento.getDataPagamento()));
            stmt.setInt(5, pagamento.getIdPagamento());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Pagamento ID " + pagamento.getIdPagamento() + " atualizado.");
            }
            return affectedRows > 0;
        }
    }

    public boolean deletar(int idPagamento, Connection conn) throws SQLException {
        String sql = "DELETE FROM Pagamento_realiza WHERE id_pagamento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPagamento);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Pagamento ID " + idPagamento + " deletado.");
            }
            return affectedRows > 0;
        }
    }

    public Pagamento buscarPorId(int idPagamento, Connection conn) throws SQLException {
        String sql = "SELECT id_pagamento, id_pedido, valor_total, metodo_pagamento, data_pagamento FROM Pagamento_realiza WHERE id_pagamento = ?";
        Pagamento pagamento = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPagamento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pagamento = new Pagamento(
                            rs.getInt("id_pagamento"),
                            rs.getInt("id_pedido"),
                            rs.getBigDecimal("valor_total"),
                            rs.getString("metodo_pagamento"),
                            rs.getTimestamp("data_pagamento").toLocalDateTime()
                    );
                }
            }
        }
        return pagamento;
    }

    public Pagamento buscarPorId(int idPagamento) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idPagamento, conn);
        }
    }

    public Pagamento buscarPorIdPedido(int idPedido, Connection conn) throws SQLException {
        String sql = "SELECT id_pagamento, id_pedido, valor_total, metodo_pagamento, data_pagamento FROM Pagamento_realiza WHERE id_pedido = ?";
        Pagamento pagamento = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pagamento = new Pagamento(
                            rs.getInt("id_pagamento"),
                            rs.getInt("id_pedido"),
                            rs.getBigDecimal("valor_total"),
                            rs.getString("metodo_pagamento"),
                            rs.getTimestamp("data_pagamento").toLocalDateTime()
                    );
                }
            }
        }
        return pagamento;
    }

    public Pagamento buscarPorIdPedido(int idPedido) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorIdPedido(idPedido, conn);
        }
    }

    public List<Pagamento> listarTodos(Connection conn) throws SQLException {
        List<Pagamento> lista = new ArrayList<>();
        String sql = "SELECT id_pagamento, id_pedido, valor_total, metodo_pagamento, data_pagamento FROM Pagamento_realiza ORDER BY data_pagamento DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Pagamento(
                        rs.getInt("id_pagamento"),
                        rs.getInt("id_pedido"),
                        rs.getBigDecimal("valor_total"),
                        rs.getString("metodo_pagamento"),
                        rs.getTimestamp("data_pagamento").toLocalDateTime()
                ));
            }
        }
        return lista;
    }

    public List<Pagamento> listarTodos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarTodos(conn);
        }
    }

    // MÉTODO ADICIONADO PARA O DASHBOARD
    public BigDecimal sumValorTotalPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT SUM(valor_total) FROM Pagamento_realiza WHERE DATE(data_pagamento) BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataInicial));
            stmt.setDate(2, Date.valueOf(dataFinal));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    return (total == null) ? BigDecimal.ZERO : total;
                }
            }
        }
        return BigDecimal.ZERO;
    }
}