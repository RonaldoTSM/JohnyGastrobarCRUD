package johnygastrobar.service;

import johnygastrobar.dao.PedidoDAO;
import johnygastrobar.dao.ItemDAO;
import johnygastrobar.dao.MesaDAO;
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.Item;
import johnygastrobar.model.Mesa;
import johnygastrobar.model.Pedido;
import johnygastrobar.model.Pedido.PedidoItem;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate; // Para os novos métodos
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap; // Para DTO de contagem de status
import java.util.List;
import java.util.Map; // Para DTO de contagem de status


@Service
public class PedidoService {

    private final PedidoDAO pedidoDAO;
    private final ItemDAO itemDAO;
    private final MesaDAO mesaDAO;

    @Autowired
    public PedidoService(PedidoDAO pedidoDAO, ItemDAO itemDAO, MesaDAO mesaDAO) {
        this.pedidoDAO = pedidoDAO;
        this.itemDAO = itemDAO;
        this.mesaDAO = mesaDAO;
    }

    public Pedido criarPedido(Pedido pedido) throws ServiceException, ResourceNotFoundException {
        if (pedido == null) {
            throw new IllegalArgumentException("Objeto pedido não pode ser nulo.");
        }
        if (pedido.getIdMesa() <= 0) {
            throw new ServiceException("ID da mesa inválido para o pedido.");
        }
        if (pedido.getItensDoPedido() == null || pedido.getItensDoPedido().isEmpty()) {
            throw new ServiceException("Um pedido deve conter pelo menos um item.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Mesa mesa = mesaDAO.buscarPorId(pedido.getIdMesa(), conn);
            if (mesa == null) {
                throw new ResourceNotFoundException("Mesa com ID " + pedido.getIdMesa() + " não encontrada.");
            }

            List<PedidoItem> itensValidados = new ArrayList<>();
            for (PedidoItem pi : pedido.getItensDoPedido()) {
                if (pi.getIdItem() <= 0 || pi.getQuantidade() <= 0) {
                    throw new ServiceException("Item inválido no pedido: ID e quantidade devem ser positivos.");
                }
                Item itemDeCatalogo = itemDAO.buscarPorId(pi.getIdItem(), conn);
                if (itemDeCatalogo == null) {
                    throw new ResourceNotFoundException("Item com ID " + pi.getIdItem() + " não encontrado no catálogo.");
                }
                itensValidados.add(new PedidoItem(
                        itemDeCatalogo.getIdItem(),
                        itemDeCatalogo.getNome(),
                        itemDeCatalogo.getTipo(),
                        pi.getQuantidade(),
                        itemDeCatalogo.getPreco()
                ));
            }
            pedido.setItensDoPedido(itensValidados);

            if (pedido.getDataHora() == null) {
                pedido.setDataHora(LocalDateTime.now());
            }

            Pedido novoPedido = pedidoDAO.inserir(pedido, conn);

            conn.commit();
            return novoPedido;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRollback) {
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de criar pedido: " + exRollback.getMessage());
                }
            }
            throw new ServiceException("Erro ao criar pedido no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após criar pedido: " + e.getMessage());
                }
            }
        }
    }

    public Pedido buscarPedidoPorId(int id) throws ResourceNotFoundException, ServiceException {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(id);
            if (pedido == null) {
                throw new ResourceNotFoundException("Pedido com ID " + id + " não encontrado.");
            }
            return pedido;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar pedido por ID: " + e.getMessage(), e);
        }
    }

    public List<Pedido> listarTodosPedidos() throws ServiceException {
        try {
            return pedidoDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todos os pedidos: " + e.getMessage(), e);
        }
    }

    public List<Pedido> listarPedidosNaoPagos() throws ServiceException {
        try {
            // Este método no DAO foi ajustado para chamar listarPedidosByStatus(false, false, conn)
            return pedidoDAO.listarPedidosNaoPagos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar pedidos não pagos: " + e.getMessage(), e);
        }
    }

    // NOVO: Método para listar pedidos por status específico para a "Aba de Pedidos"
    public List<Pedido> listarPedidosPorStatus(String status) throws ServiceException {
        boolean entregue;
        boolean pago;

        // Definir a lógica para cada status. CUIDADO com sobreposições ou status não cobertos.
        switch (status.toUpperCase()) {
            case "PENDENTE": // Ex: Não entregue E não pago
                entregue = false;
                pago = false;
                break;
            case "ENTREGUE": // Ex: Entregue E não pago
                entregue = true;
                pago = false;
                break;
            case "PAGO": // Ex: Pago (entregue pode ser true ou false dependendo da regra)
                entregue = true; // Assumindo que um pedido pago geralmente está entregue ou será.
                pago = true;     // Ou apenas pago = true;
                break;
            default:
                throw new IllegalArgumentException("Status de pedido desconhecido: " + status);
        }

        try (Connection conn = ConnectionFactory.getConnection()){
            return pedidoDAO.listarPedidosByStatus(entregue, pago, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar pedidos por status '" + status + "': " + e.getMessage(), e);
        }
    }


    public Pedido atualizarPedido(Pedido pedido) throws ResourceNotFoundException, ServiceException {
        if (pedido == null || pedido.getIdPedido() <= 0) {
            throw new IllegalArgumentException("Dados do pedido inválidos para atualização ou ID não fornecido.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Pedido pedidoExistente = pedidoDAO.buscarPorId(pedido.getIdPedido(), conn);
            if (pedidoExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Pedido com ID " + pedido.getIdPedido() + " não encontrado para atualização.");
            }

            // REQUISITO: Não deve ser possível editar um pedido pago.
            if (pedidoExistente.isPago()) {
                conn.rollback(); // Não há nada para fazer se já está pago e tentamos editar.
                throw new ServiceException("Não é possível atualizar um pedido que já foi pago (ID: " + pedido.getIdPedido() + ").");
            }

            if (pedido.getItensDoPedido() != null && !pedido.getItensDoPedido().isEmpty()) {
                List<PedidoItem> itensValidadosAtualizacao = new ArrayList<>();
                for (PedidoItem pi : pedido.getItensDoPedido()) {
                    if (pi.getIdItem() <= 0 || pi.getQuantidade() <= 0) {
                        throw new ServiceException("Item inválido no pedido: ID e quantidade devem ser positivos.");
                    }
                    Item itemDeCatalogo = itemDAO.buscarPorId(pi.getIdItem(), conn);
                    if (itemDeCatalogo == null) {
                        throw new ResourceNotFoundException("Item com ID " + pi.getIdItem() + " não encontrado no catálogo para atualização do pedido.");
                    }

                    BigDecimal precoParaUtilizar;
                    if (pi.getPrecoUnitario() != null && pi.getPrecoUnitario().compareTo(BigDecimal.ZERO) > 0) {
                        precoParaUtilizar = pi.getPrecoUnitario();
                    } else {
                        precoParaUtilizar = itemDeCatalogo.getPreco();
                    }

                    itensValidadosAtualizacao.add(new PedidoItem(
                            itemDeCatalogo.getIdItem(),
                            itemDeCatalogo.getNome(),
                            itemDeCatalogo.getTipo(),
                            pi.getQuantidade(),
                            precoParaUtilizar
                    ));
                }
                pedido.setItensDoPedido(itensValidadosAtualizacao);
            } else {
                pedido.setItensDoPedido(new ArrayList<>());
            }

            boolean atualizado = pedidoDAO.atualizar(pedido, conn);
            if (!atualizado) {
                throw new ServiceException("Falha ao atualizar o pedido ID " + pedido.getIdPedido() + ".");
            }

            conn.commit();
            return pedidoDAO.buscarPorId(pedido.getIdPedido(), conn);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da atualização de pedido: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao atualizar pedido: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da atualização de pedido: " + ex.getMessage());}
            }
        }
    }

    public Pedido marcarPedidoComoPago(int idPedido, Connection conn) throws ResourceNotFoundException, ServiceException {
        if (idPedido <= 0) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }
        if (conn == null) {
            throw new IllegalArgumentException("Objeto de conexão não pode ser nulo para esta operação transacional.");
        }

        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido, conn);
            if (pedido == null) {
                throw new ResourceNotFoundException("Pedido com ID " + idPedido + " não encontrado para marcar como pago.");
            }

            if (pedido.isPago()) {
                System.out.println("INFO: Pedido ID " + idPedido + " já está marcado como pago. Nenhuma alteração realizada.");
                return pedido;
            }

            boolean sucesso = pedidoDAO.marcarComoPago(idPedido, conn);
            if (!sucesso) {
                throw new ServiceException("Não foi possível marcar o pedido ID " + idPedido + " como pago (DAO retornou falha).");
            }

            return pedidoDAO.buscarPorId(idPedido, conn);

        } catch (SQLException e) {
            throw new ServiceException("Erro ao marcar pedido como pago: " + e.getMessage(), e);
        }
    }

    public void deletarPedido(int id) throws ResourceNotFoundException, ServiceException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID do pedido inválido para deleção.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Pedido pedidoExistente = pedidoDAO.buscarPorId(id, conn);
            if (pedidoExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Pedido com ID " + id + " não encontrado para deleção.");
            }

            boolean deletado = pedidoDAO.deletar(id, conn);
            if (!deletado) {
                throw new ServiceException("Falha ao deletar o pedido ID " + id + ".");
            }

            conn.commit();
            System.out.println("Pedido ID " + id + " deletado com sucesso (serviço).");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    System.err.println("CRÍTICO: Erro no rollback da deleção de pedido: " + ex.getMessage());
                }
            }
            if (e.getErrorCode() == 1451 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key constraint fails"))) {
                throw new ServiceException("Não é possível deletar o pedido ID " + id + " pois ele está referenciado em outros registros (ex: pagamentos, autorizações, feedbacks).", e);
            }
            throw new ServiceException("Erro ao deletar pedido: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {
                    System.err.println("Erro ao fechar conexão da deleção de pedido: " + ex.getMessage());
                }
            }
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD ---

    public int countPedidosCriadosPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de contagem de pedidos.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return pedidoDAO.countPedidosCriadosPorPeriodo(dataInicial, dataFinal, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao contar pedidos criados por período: " + e.getMessage(), e);
        }
    }

    public int countPedidosPagosPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de contagem de pedidos pagos.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return pedidoDAO.countPedidosPagosPorPeriodo(dataInicial, dataFinal, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao contar pedidos pagos por período: " + e.getMessage(), e);
        }
    }

    public BigDecimal sumQuantidadeItensVendidosPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de soma de itens vendidos.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return pedidoDAO.sumQuantidadeItensVendidosPorPeriodo(dataInicial, dataFinal, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao somar quantidade de itens vendidos por período: " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getContagemPedidosPorStatus(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de status de pedidos.");
        }
        Map<String, Integer> contagens = new HashMap<>();
        try (Connection conn = ConnectionFactory.getConnection()) {
            // Definição dos status:
            // Pendente: não entregue E não pago
            contagens.put("PENDENTE", pedidoDAO.countPedidosByStatusEPeriodo(false, false, dataInicial, dataFinal, conn));
            // Entregue (e não pago): entregue E não pago
            contagens.put("ENTREGUE_NAO_PAGO", pedidoDAO.countPedidosByStatusEPeriodo(true, false, dataInicial, dataFinal, conn));
            // Pago: pago (independente de entregue, mas geralmente entregue)
            contagens.put("PAGO", pedidoDAO.countPedidosByStatusEPeriodo(true, true, dataInicial, dataFinal, conn) +
                    pedidoDAO.countPedidosByStatusEPeriodo(false, true, dataInicial, dataFinal, conn)); // Soma pago&entregue + pago&nãoEntregue
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar contagem de pedidos por status: " + e.getMessage(), e);
        }
        return contagens;
    }
}