package johnygastrobar.dao;

import johnygastrobar.model.Pedido;
import johnygastrobar.model.Pedido.PedidoItem;
import johnygastrobar.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    // MÉTODO INSERIR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public int inserir(Pedido pedido, Connection conn) throws SQLException { // Lança SQLException para ser gerenciada externamente
        String sqlPedido = "INSERT INTO Pedido (id_garcom, id_gerente, id_mesa, data_hora, entregue, desconto, pago) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlPedidoItem = "INSERT INTO Pedido_Item (id_pedido, id_item, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        int idGerado = -1;

        try (PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
            if (pedido.getIdGarcom() == null) {
                stmtPedido.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmtPedido.setInt(1, pedido.getIdGarcom());
            }
            if (pedido.getIdGerente() == null) {
                stmtPedido.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmtPedido.setInt(2, pedido.getIdGerente());
            }
            stmtPedido.setInt(3, pedido.getIdMesa());
            stmtPedido.setTimestamp(4, Timestamp.valueOf(pedido.getDataHora()));
            stmtPedido.setBoolean(5, pedido.isEntregue());
            if (pedido.getDesconto() == null) {
                stmtPedido.setNull(6, java.sql.Types.DECIMAL);
            } else {
                stmtPedido.setDouble(6, pedido.getDesconto());
            }
            stmtPedido.setBoolean(7, pedido.isPago()); // Define o status 'pago'
            stmtPedido.executeUpdate();

            ResultSet rs = stmtPedido.getGeneratedKeys();
            if (rs.next()) {
                idGerado = rs.getInt(1);
                System.out.println("Pedido inserido com ID: " + idGerado);
            } else {
                throw new SQLException("Falha ao obter o ID gerado para o Pedido.");
            }
        }

        try (PreparedStatement stmtPedidoItem = conn.prepareStatement(sqlPedidoItem)) {
            for (PedidoItem item : pedido.getItensDoPedido()) {
                stmtPedidoItem.setInt(1, idGerado);
                stmtPedidoItem.setInt(2, item.getIdItem());
                stmtPedidoItem.setInt(3, item.getQuantidade());
                stmtPedidoItem.setDouble(4, item.getPrecoUnitario());
                stmtPedidoItem.addBatch();
            }
            stmtPedidoItem.executeBatch();
        }
        return idGerado;
    }

    // MÉTODO LISTAR TODOS CONTINUA ABRINDO SUA PRÓPRIA CONEXÃO
    public List<Pedido> listarTodos() {
        List<Pedido> lista = new ArrayList<>();
        String sqlPedidos = "SELECT id_pedido, id_garcom, id_gerente, id_mesa, data_hora, entregue, desconto, pago FROM Pedido ORDER BY data_hora DESC";
        String sqlItensPedido = "SELECT pi.id_item, i.nome, i.tipo, pi.quantidade, pi.preco_unitario " +
                "FROM Pedido_Item pi JOIN Item i ON pi.id_item = i.id_item " +
                "WHERE pi.id_pedido = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmtPedidos = conn.createStatement();
             ResultSet rsPedidos = stmtPedidos.executeQuery(sqlPedidos)) {

            while (rsPedidos.next()) {
                int idPedido = rsPedidos.getInt("id_pedido");
                Integer idGarcom = rsPedidos.getInt("id_garcom");
                if (rsPedidos.wasNull()) idGarcom = null;
                Integer idGerente = rsPedidos.getInt("id_gerente");
                if (rsPedidos.wasNull()) idGerente = null;

                Pedido pedido = new Pedido(
                        idPedido,
                        idGarcom,
                        idGerente,
                        rsPedidos.getInt("id_mesa"),
                        rsPedidos.getTimestamp("data_hora").toLocalDateTime(),
                        rsPedidos.getBoolean("entregue"),
                        rsPedidos.getDouble("desconto"),
                        rsPedidos.getBoolean("pago")
                );
                if (rsPedidos.wasNull()) pedido.setDesconto(null);

                try (PreparedStatement stmtItens = conn.prepareStatement(sqlItensPedido)) {
                    stmtItens.setInt(1, idPedido);
                    ResultSet rsItens = stmtItens.executeQuery();
                    while (rsItens.next()) {
                        pedido.adicionarItem(
                                rsItens.getInt("id_item"),
                                rsItens.getString("nome"),
                                rsItens.getString("tipo"),
                                rsItens.getInt("quantidade"),
                                rsItens.getDouble("preco_unitario")
                        );
                    }
                }
                lista.add(pedido);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pedidos e seus itens: " + e.getMessage());
        }
        return lista;
    }

    // MÉTODO ATUALIZAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void atualizar(Pedido pedido, Connection conn) throws SQLException { // Lança SQLException
        String sqlUpdatePedido = "UPDATE Pedido SET id_garcom = ?, id_gerente = ?, id_mesa = ?, data_hora = ?, entregue = ?, desconto = ?, pago = ? WHERE id_pedido = ?";
        String sqlDeleteItens = "DELETE FROM Pedido_Item WHERE id_pedido = ?";
        String sqlInsertItem = "INSERT INTO Pedido_Item (id_pedido, id_item, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmtUpdatePedido = conn.prepareStatement(sqlUpdatePedido)) {
            if (pedido.getIdGarcom() == null) {
                stmtUpdatePedido.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmtUpdatePedido.setInt(1, pedido.getIdGarcom());
            }
            if (pedido.getIdGerente() == null) {
                stmtUpdatePedido.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmtUpdatePedido.setInt(2, pedido.getIdGerente());
            }
            stmtUpdatePedido.setInt(3, pedido.getIdMesa());
            stmtUpdatePedido.setTimestamp(4, Timestamp.valueOf(pedido.getDataHora()));
            stmtUpdatePedido.setBoolean(5, pedido.isEntregue());
            if (pedido.getDesconto() == null) {
                stmtUpdatePedido.setNull(6, java.sql.Types.DECIMAL);
            } else {
                stmtUpdatePedido.setDouble(6, pedido.getDesconto());
            }
            stmtUpdatePedido.setBoolean(7, pedido.isPago());
            stmtUpdatePedido.setInt(8, pedido.getIdPedido());
            stmtUpdatePedido.executeUpdate();
        }

        try (PreparedStatement stmtDeleteItens = conn.prepareStatement(sqlDeleteItens)) {
            stmtDeleteItens.setInt(1, pedido.getIdPedido());
            stmtDeleteItens.executeUpdate();
        }

        try (PreparedStatement stmtInsertItem = conn.prepareStatement(sqlInsertItem)) {
            for (PedidoItem item : pedido.getItensDoPedido()) {
                stmtInsertItem.setInt(1, pedido.getIdPedido());
                stmtInsertItem.setInt(2, item.getIdItem());
                stmtInsertItem.setInt(3, item.getQuantidade());
                stmtInsertItem.setDouble(4, item.getPrecoUnitario());
                stmtInsertItem.addBatch();
            }
            stmtInsertItem.executeBatch();
        }
        System.out.println("Pedido e itens atualizados para ID: " + pedido.getIdPedido());
    }

    // MÉTODO DELETAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void deletar(int idPedido, Connection conn) throws SQLException { // Lança SQLException
        String sqlDeletePedido = "DELETE FROM Pedido WHERE id_pedido = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDeletePedido)) {
            stmt.setInt(1, idPedido);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Pedido com ID " + idPedido + " deletado com sucesso.");
            } else {
                System.out.println("Nenhum pedido encontrado com ID " + idPedido + " para deletar.");
            }
        }
    }

    // MÉTODO BUSCAR POR ID AGORA RECEBE UMA CONEXÃO EXISTENTE (ou abre uma nova se null)
    public Pedido buscarPorId(int idPedido, Connection externalConn) throws SQLException {
        Connection conn = null;
        try {
            if (externalConn != null) {
                conn = externalConn;
            } else {
                conn = ConnectionFactory.getConnection(); // Abre nova conexão se não houver externa
            }
            String sqlPedido = "SELECT id_pedido, id_garcom, id_gerente, id_mesa, data_hora, entregue, desconto, pago FROM Pedido WHERE id_pedido = ?";
            String sqlItensPedido = "SELECT pi.id_item, i.nome, i.tipo, pi.quantidade, pi.preco_unitario " +
                    "FROM Pedido_Item pi JOIN Item i ON pi.id_item = i.id_item " +
                    "WHERE pi.id_pedido = ?";
            Pedido pedido = null;
            try (PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido)) {

                stmtPedido.setInt(1, idPedido);
                ResultSet rsPedidos = stmtPedido.executeQuery();

                if (rsPedidos.next()) {
                    Integer idGarcom = rsPedidos.getInt("id_garcom");
                    if (rsPedidos.wasNull()) idGarcom = null;
                    Integer idGerente = rsPedidos.getInt("id_gerente");
                    if (rsPedidos.wasNull()) idGerente = null;

                    pedido = new Pedido(
                            rsPedidos.getInt("id_pedido"),
                            idGarcom,
                            idGerente,
                            rsPedidos.getInt("id_mesa"),
                            rsPedidos.getTimestamp("data_hora").toLocalDateTime(),
                            rsPedidos.getBoolean("entregue"),
                            rsPedidos.getDouble("desconto"),
                            rsPedidos.getBoolean("pago")
                    );
                    if (rsPedidos.wasNull()) pedido.setDesconto(null);

                    try (PreparedStatement stmtItens = conn.prepareStatement(sqlItensPedido)) {
                        stmtItens.setInt(1, idPedido);
                        ResultSet rsItens = stmtItens.executeQuery();
                        while (rsItens.next()) {
                            pedido.adicionarItem(
                                    rsItens.getInt("id_item"),
                                    rsItens.getString("nome"),
                                    rsItens.getString("tipo"),
                                    rsItens.getInt("quantidade"),
                                    rsItens.getDouble("preco_unitario")
                            );
                        }
                    }
                }
            }
            return pedido;
        } finally {
            if (externalConn == null && conn != null) { // Fecha a conexão se ela foi aberta por este método
                conn.close();
            }
        }
    }

    // Sobrecarga para compatibilidade, manterá a função original que abre e fecha a conexão
    public Pedido buscarPorId(int idPedido) {
        try {
            return buscarPorId(idPedido, null);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar pedido por ID (sem conexão externa): " + e.getMessage());
            return null;
        }
    }


    // MÉTODO MARCAR COMO PAGO AGORA RECEBE UMA CONEXÃO EXISTENTE
    public boolean marcarComoPago(int idPedido, Connection conn) throws SQLException { // Lança SQLException
        String sql = "UPDATE Pedido SET pago = TRUE WHERE id_pedido = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Pedido ID " + idPedido + " marcado como pago.");
                return true;
            } else {
                System.out.println("Pedido ID " + idPedido + " não encontrado para marcar como pago.");
                return false;
            }
        }
    }

    // MÉTODO LISTAR PEDIDOS NÃO PAGOS AGORA RECEBE UMA CONEXÃO EXISTENTE (ou abre uma nova se null)
    public List<Pedido> listarPedidosNaoPagos(Connection externalConn) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        Connection conn = null;
        try {
            if (externalConn != null) {
                conn = externalConn;
            } else {
                conn = ConnectionFactory.getConnection();
            }
            String sqlPedidos = "SELECT id_pedido, id_garcom, id_gerente, id_mesa, data_hora, entregue, desconto, pago FROM Pedido WHERE pago = FALSE ORDER BY data_hora DESC";
            String sqlItensPedido = "SELECT pi.id_item, i.nome, i.tipo, pi.quantidade, pi.preco_unitario " +
                    "FROM Pedido_Item pi JOIN Item i ON pi.id_item = i.id_item " +
                    "WHERE pi.id_pedido = ?";

            try (Statement stmtPedidos = conn.createStatement();
                 ResultSet rsPedidos = stmtPedidos.executeQuery(sqlPedidos)) {

                while (rsPedidos.next()) {
                    int idPedido = rsPedidos.getInt("id_pedido");
                    Integer idGarcom = rsPedidos.getInt("id_garcom");
                    if (rsPedidos.wasNull()) idGarcom = null;
                    Integer idGerente = rsPedidos.getInt("id_gerente");
                    if (rsPedidos.wasNull()) idGerente = null;

                    Pedido pedido = new Pedido(
                            idPedido,
                            idGarcom,
                            idGerente,
                            rsPedidos.getInt("id_mesa"),
                            rsPedidos.getTimestamp("data_hora").toLocalDateTime(),
                            rsPedidos.getBoolean("entregue"),
                            rsPedidos.getDouble("desconto"),
                            rsPedidos.getBoolean("pago")
                    );
                    if (rsPedidos.wasNull()) pedido.setDesconto(null);

                    try (PreparedStatement stmtItens = conn.prepareStatement(sqlItensPedido)) {
                        stmtItens.setInt(1, idPedido);
                        ResultSet rsItens = stmtItens.executeQuery();
                        while (rsItens.next()) {
                            pedido.adicionarItem(
                                    rsItens.getInt("id_item"),
                                    rsItens.getString("nome"),
                                    rsItens.getString("tipo"),
                                    rsItens.getInt("quantidade"),
                                    rsItens.getDouble("preco_unitario")
                            );
                        }
                    }
                    lista.add(pedido);
                }
            }
        } finally {
            if (externalConn == null && conn != null) { // Fecha a conexão se ela foi aberta por este método
                conn.close();
            }
        }
        return lista;
    }

    // Sobrecarga para compatibilidade
    public List<Pedido> listarPedidosNaoPagos() {
        try {
            return listarPedidosNaoPagos(null);
        } catch (SQLException e) {
            System.err.println("Erro ao listar pedidos não pagos (sem conexão externa): " + e.getMessage());
            return new ArrayList<>();
        }
    }
}