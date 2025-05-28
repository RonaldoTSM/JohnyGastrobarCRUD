package johnygastrobar.dao;

import johnygastrobar.model.Pedido;
import johnygastrobar.model.Pedido.PedidoItem;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date; // Para conversão de LocalDate
import java.time.LocalDate; // Para os novos métodos
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Repository
public class PedidoDAO {

    private void inserirItensDoPedido(Pedido pedido, Connection conn) throws SQLException {
        if (pedido.getItensDoPedido() == null || pedido.getItensDoPedido().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO Pedido_Item (id_pedido, id_item, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (PedidoItem item : pedido.getItensDoPedido()) {
                stmt.setInt(1, pedido.getIdPedido());
                stmt.setInt(2, item.getIdItem());
                stmt.setInt(3, item.getQuantidade());
                stmt.setBigDecimal(4, item.getPrecoUnitario());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void deletarItensDoPedido(int idPedido, Connection conn) throws SQLException {
        String sql = "DELETE FROM Pedido_Item WHERE id_pedido = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            stmt.executeUpdate();
        }
    }

    private List<PedidoItem> getItensPorPedidoId(int idPedido, Connection conn) throws SQLException {
        List<PedidoItem> itens = new ArrayList<>();
        String sql = "SELECT pi.id_item, i.nome AS nome_item, i.tipo AS tipo_item, pi.quantidade, pi.preco_unitario " +
                "FROM Pedido_Item pi " +
                "JOIN Item i ON pi.id_item = i.id_item " +
                "WHERE pi.id_pedido = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(new PedidoItem(
                            rs.getInt("id_item"),
                            rs.getString("nome_item"),
                            rs.getString("tipo_item"),
                            rs.getInt("quantidade"),
                            rs.getBigDecimal("preco_unitario")
                    ));
                }
            }
        }
        return itens;
    }

    public Pedido inserir(Pedido pedido, Connection conn) throws SQLException {
        String sql = "INSERT INTO Pedido (id_garcom, id_gerente, id_mesa, data_hora, entregue, pago, desconto) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, pedido.getIdGarcom(), Types.INTEGER);
            stmt.setObject(2, pedido.getIdGerente(), Types.INTEGER);
            stmt.setInt(3, pedido.getIdMesa());
            stmt.setTimestamp(4, Timestamp.valueOf(pedido.getDataHora()));
            stmt.setBoolean(5, pedido.isEntregue());
            stmt.setBoolean(6, pedido.isPago());
            stmt.setBigDecimal(7, pedido.getDesconto());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir pedido, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pedido.setIdPedido(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir pedido, não foi possível obter o ID gerado.");
                }
            }
            inserirItensDoPedido(pedido, conn);
        }
        System.out.println("Pedido ID " + pedido.getIdPedido() + " inserido.");
        return pedido;
    }

    public boolean atualizar(Pedido pedido, Connection conn) throws SQLException {
        String sql = "UPDATE Pedido SET id_garcom = ?, id_gerente = ?, id_mesa = ?, data_hora = ?, entregue = ?, pago = ?, desconto = ? WHERE id_pedido = ?";
        int affectedRows;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, pedido.getIdGarcom(), Types.INTEGER);
            stmt.setObject(2, pedido.getIdGerente(), Types.INTEGER);
            stmt.setInt(3, pedido.getIdMesa());
            stmt.setTimestamp(4, Timestamp.valueOf(pedido.getDataHora()));
            stmt.setBoolean(5, pedido.isEntregue());
            stmt.setBoolean(6, pedido.isPago());
            stmt.setBigDecimal(7, pedido.getDesconto());
            stmt.setInt(8, pedido.getIdPedido());
            affectedRows = stmt.executeUpdate();
        }

        if (affectedRows > 0) {
            deletarItensDoPedido(pedido.getIdPedido(), conn);
            inserirItensDoPedido(pedido, conn);
            System.out.println("Pedido ID " + pedido.getIdPedido() + " atualizado.");
        }
        return affectedRows > 0;
    }

    public boolean deletar(int idPedido, Connection conn) throws SQLException {
        String sql = "DELETE FROM Pedido WHERE id_pedido = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Pedido ID " + idPedido + " deletado.");
            }
            return affectedRows > 0;
        }
    }

    public Pedido buscarPorId(int idPedido, Connection conn) throws SQLException {
        String sql = "SELECT id_pedido, id_garcom, id_gerente, id_mesa, data_hora, entregue, pago, desconto FROM Pedido WHERE id_pedido = ?";
        Pedido pedido = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pedido = new Pedido(
                            rs.getInt("id_pedido"),
                            (Integer) rs.getObject("id_garcom"),
                            (Integer) rs.getObject("id_gerente"),
                            rs.getInt("id_mesa"),
                            rs.getTimestamp("data_hora").toLocalDateTime(),
                            rs.getBoolean("entregue"),
                            rs.getBoolean("pago"),
                            rs.getBigDecimal("desconto")
                    );
                    pedido.setItensDoPedido(getItensPorPedidoId(idPedido, conn));
                }
            }
        }
        return pedido;
    }

    public Pedido buscarPorId(int idPedido) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idPedido, conn);
        }
    }

    public List<Pedido> listarTodos(Connection conn) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sqlPedidos = "SELECT id_pedido, id_garcom, id_gerente, id_mesa, data_hora, entregue, pago, desconto FROM Pedido ORDER BY data_hora DESC";

        Map<Integer, List<PedidoItem>> todosOsItensDePedidos = new HashMap<>();
        String sqlItens = "SELECT pi.id_pedido, pi.id_item, i.nome AS nome_item, i.tipo AS tipo_item, pi.quantidade, pi.preco_unitario " +
                "FROM Pedido_Item pi JOIN Item i ON pi.id_item = i.id_item";

        try (Statement stmtItens = conn.createStatement();
             ResultSet rsItens = stmtItens.executeQuery(sqlItens)) {
            while (rsItens.next()) {
                int idPedidoMap = rsItens.getInt("id_pedido");
                PedidoItem item = new PedidoItem(
                        rsItens.getInt("id_item"),
                        rsItens.getString("nome_item"),
                        rsItens.getString("tipo_item"),
                        rsItens.getInt("quantidade"),
                        rsItens.getBigDecimal("preco_unitario")
                );
                todosOsItensDePedidos.computeIfAbsent(idPedidoMap, k -> new ArrayList<>()).add(item);
            }
        }

        try (PreparedStatement stmtPedidos = conn.prepareStatement(sqlPedidos);
             ResultSet rsPedidos = stmtPedidos.executeQuery()) {
            while (rsPedidos.next()) {
                int idPedido = rsPedidos.getInt("id_pedido");
                Pedido pedido = new Pedido(
                        idPedido,
                        (Integer) rsPedidos.getObject("id_garcom"),
                        (Integer) rsPedidos.getObject("id_gerente"),
                        rsPedidos.getInt("id_mesa"),
                        rsPedidos.getTimestamp("data_hora").toLocalDateTime(),
                        rsPedidos.getBoolean("entregue"),
                        rsPedidos.getBoolean("pago"),
                        rsPedidos.getBigDecimal("desconto")
                );
                pedido.setItensDoPedido(todosOsItensDePedidos.getOrDefault(idPedido, new ArrayList<Pedido.PedidoItem>()));
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    public List<Pedido> listarTodos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarTodos(conn);
        }
    }

    public boolean marcarComoPago(int idPedido, Connection conn) throws SQLException {
        String sql = "UPDATE Pedido SET pago = TRUE WHERE id_pedido = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Pedido ID " + idPedido + " marcado como pago.");
            }
            return affectedRows > 0;
        }
    }

    public List<Pedido> listarPedidosNaoPagos(Connection conn) throws SQLException {
        return listarPedidosByStatus(false, false, conn); // Exemplo: entregue=false, pago=false
    }

    public List<Pedido> listarPedidosNaoPagos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarPedidosNaoPagos(conn);
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD E REQUISITOS DE PÁGINA ---

    public int countPedidosCriadosPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(id_pedido) FROM Pedido WHERE DATE(data_hora) BETWEEN ? AND ?";
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

    public int countPedidosPagosPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(id_pedido) FROM Pedido WHERE pago = TRUE AND DATE(data_hora) BETWEEN ? AND ?";
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

    public BigDecimal sumQuantidadeItensVendidosPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT SUM(pi.quantidade) FROM Pedido_Item pi " +
                "JOIN Pedido p ON pi.id_pedido = p.id_pedido " +
                "WHERE DATE(p.data_hora) BETWEEN ? AND ?";
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

    public int countPedidosByStatusEPeriodo(boolean entregue, boolean pago, LocalDate dataInicial, LocalDate dataFinal, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(id_pedido) FROM Pedido WHERE entregue = ? AND pago = ? AND DATE(data_hora) BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, entregue);
            stmt.setBoolean(2, pago);
            stmt.setDate(3, Date.valueOf(dataInicial));
            stmt.setDate(4, Date.valueOf(dataFinal));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // Método para listagem por status específico para a "Aba de Pedidos"
    // Pode ou não incluir filtro de período, dependendo da necessidade da tela.
    // Para simplificar, esta versão não filtra por período, mas poderia ser adicionado.
    public List<Pedido> listarPedidosByStatus(boolean entregue, boolean pago, Connection conn) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        // Reutiliza a lógica de listarTodos e filtra, ou cria query SQL específica
        // Criar query específica é mais eficiente se a lista for grande.
        String sqlPedidos = "SELECT id_pedido, id_garcom, id_gerente, id_mesa, data_hora, entregue, pago, desconto " +
                "FROM Pedido WHERE entregue = ? AND pago = ? ORDER BY data_hora DESC";

        // A lógica de buscar todos os itens para todos esses pedidos de uma vez (como em listarTodos)
        // se torna mais complexa aqui se quisermos otimizar.
        // Para simplificar o exemplo agora, vamos buscar os itens para cada pedido individualmente (N+1).
        // Uma otimização seria buscar todos os IDs dos pedidos filtrados e depois buscar todos os seus itens.

        try (PreparedStatement stmtPedidos = conn.prepareStatement(sqlPedidos)) {
            stmtPedidos.setBoolean(1, entregue);
            stmtPedidos.setBoolean(2, pago);
            try(ResultSet rsPedidos = stmtPedidos.executeQuery()) {
                while (rsPedidos.next()) {
                    int idPedido = rsPedidos.getInt("id_pedido");
                    Pedido pedido = new Pedido(
                            idPedido,
                            (Integer) rsPedidos.getObject("id_garcom"),
                            (Integer) rsPedidos.getObject("id_gerente"),
                            rsPedidos.getInt("id_mesa"),
                            rsPedidos.getTimestamp("data_hora").toLocalDateTime(),
                            rsPedidos.getBoolean("entregue"),
                            rsPedidos.getBoolean("pago"),
                            rsPedidos.getBigDecimal("desconto")
                    );
                    // Atenção: N+1 query aqui para os itens. Otimizar se necessário.
                    pedido.setItensDoPedido(getItensPorPedidoId(idPedido, conn));
                    pedidos.add(pedido);
                }
            }
        }
        return pedidos;
    }
}