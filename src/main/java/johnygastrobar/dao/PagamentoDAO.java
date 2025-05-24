package johnygastrobar.dao;

import johnygastrobar.model.Pagamento;
import johnygastrobar.util.ConnectionFactory; // Continua sendo usada para listarTodos ou buscarPorId sem conexão externa

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp; // Para LocalDateTime
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    // Instância do PedidoDAO para poder marcar o pedido como pago
    // Será inicializada, mas os métodos dela receberão a conexão externa quando usados em transações.
    private PedidoDAO pedidoDao = new PedidoDAO();

    // MÉTODO INSERIR AGORA RECEBE UMA CONEXÃO EXTERNA PARA A TRANSAÇÃO
    public int inserir(Pagamento pagamento, Connection conn) throws SQLException { // Lança SQLException
        String sqlPagamento = "INSERT INTO Pagamento_realiza (id_pedido, valor_total, metodo_pagamento, data_pagamento) VALUES (?, ?, ?, ?)";
        int idGerado = -1;

        try (PreparedStatement stmtPagamento = conn.prepareStatement(sqlPagamento, Statement.RETURN_GENERATED_KEYS)) {
            stmtPagamento.setInt(1, pagamento.getIdPedido());
            stmtPagamento.setDouble(2, pagamento.getValorTotal());
            stmtPagamento.setString(3, pagamento.getMetodoPagamento());
            stmtPagamento.setTimestamp(4, Timestamp.valueOf(pagamento.getDataPagamento()));
            stmtPagamento.executeUpdate();

            ResultSet rs = stmtPagamento.getGeneratedKeys();
            if (rs.next()) {
                idGerado = rs.getInt(1);
                System.out.println("Pagamento inserido com ID: " + idGerado);
            } else {
                throw new SQLException("Falha ao obter o ID gerado para o Pagamento.");
            }
        }

        // MARCAR O PEDIDO COMO PAGO NA MESMA TRANSAÇÃO
        boolean pedidoMarcado = pedidoDao.marcarComoPago(pagamento.getIdPedido(), conn);
        if (!pedidoMarcado) {
            throw new SQLException("Falha ao marcar o pedido como pago.");
        }

        System.out.println("Pagamento e marcação de pedido concluídos na mesma transação para Pedido ID: " + pagamento.getIdPedido());
        return idGerado;
    }

    // MÉTODO LISTAR TODOS CONTINUA ABRINDO SUA PRÓPRIA CONEXÃO
    public List<Pagamento> listarTodos() {
        List<Pagamento> lista = new ArrayList<>();
        String sql = "SELECT id_pagamento, id_pedido, valor_total, metodo_pagamento, data_pagamento FROM Pagamento_realiza ORDER BY data_pagamento DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Pagamento(
                        rs.getInt("id_pagamento"),
                        rs.getInt("id_pedido"),
                        rs.getDouble("valor_total"),
                        rs.getString("metodo_pagamento"),
                        rs.getTimestamp("data_pagamento").toLocalDateTime() // Converte Timestamp
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pagamentos: " + e.getMessage());
        }
        return lista;
    }

    // MÉTODO ATUALIZAR AGORA RECEBE UMA CONEXÃO EXTERNA
    public void atualizar(Pagamento pagamento, Connection conn) throws SQLException { // Lança SQLException
        String sql = "UPDATE Pagamento_realiza SET id_pedido = ?, valor_total = ?, metodo_pagamento = ?, data_pagamento = ? WHERE id_pagamento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pagamento.getIdPedido());
            stmt.setDouble(2, pagamento.getValorTotal());
            stmt.setString(3, pagamento.getMetodoPagamento());
            stmt.setTimestamp(4, Timestamp.valueOf(pagamento.getDataPagamento()));
            stmt.setInt(5, pagamento.getIdPagamento());
            stmt.executeUpdate();
            System.out.println("Pagamento ID " + pagamento.getIdPagamento() + " atualizado com sucesso.");
        }
    }

    // MÉTODO DELETAR AGORA RECEBE UMA CONEXÃO EXTERNA
    public void deletar(int idPagamento, Connection conn) throws SQLException { // Lança SQLException
        String sql = "DELETE FROM Pagamento_realiza WHERE id_pagamento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPagamento);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Pagamento ID " + idPagamento + " deletado com sucesso.");
            } else {
                System.out.println("Nenhum pagamento encontrado com ID " + idPagamento + " para deletar.");
            }
        }
    }
}