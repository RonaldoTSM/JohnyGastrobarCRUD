package johnygastrobar.service;

import johnygastrobar.dao.PagamentoDAO;
import johnygastrobar.dao.PedidoDAO;
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.Pagamento;
import johnygastrobar.model.Pedido;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate; // Para o novo método
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoDAO pagamentoDAO;
    private final PedidoDAO pedidoDAO;
    private final PedidoService pedidoService;

    @Autowired
    public PagamentoService(PagamentoDAO pagamentoDAO, PedidoDAO pedidoDAO, PedidoService pedidoService) {
        this.pagamentoDAO = pagamentoDAO;
        this.pedidoDAO = pedidoDAO;
        this.pedidoService = pedidoService;
    }

    public Pagamento registrarPagamento(Pagamento pagamento) throws ServiceException, ResourceNotFoundException {
        if (pagamento == null) {
            throw new IllegalArgumentException("Objeto pagamento não pode ser nulo.");
        }
        if (pagamento.getIdPedido() <= 0) {
            throw new ServiceException("ID do pedido inválido para o pagamento.");
        }
        if (pagamento.getValorTotal() == null || pagamento.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Valor total do pagamento deve ser positivo.");
        }
        if (pagamento.getMetodoPagamento() == null || pagamento.getMetodoPagamento().trim().isEmpty()) {
            throw new ServiceException("Método de pagamento é obrigatório.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Pedido pedido = pedidoDAO.buscarPorId(pagamento.getIdPedido(), conn);
            if (pedido == null) {
                throw new ResourceNotFoundException("Pedido com ID " + pagamento.getIdPedido() + " não encontrado para registrar pagamento.");
            }
            // REQUISITO: Não deve ser possível fazer isso para pagamentos já realizados.
            if (pedido.isPago()) {
                throw new ServiceException("Pedido com ID " + pagamento.getIdPedido() + " já foi pago anteriormente.");
            }

            if (pagamento.getDataPagamento() == null) {
                pagamento.setDataPagamento(LocalDateTime.now());
            }

            Pagamento novoPagamento = pagamentoDAO.inserir(pagamento, conn);

            pedidoService.marcarPedidoComoPago(novoPagamento.getIdPedido(), conn);

            conn.commit();
            return novoPagamento;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRollback) {
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de registrar pagamento: " + exRollback.getMessage());
                }
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("pagamento_realiza_id_pedido_key")) {
                throw new ServiceException("Já existe um pagamento registrado para o pedido ID " + pagamento.getIdPedido() + ".", e);
            }
            throw new ServiceException("Erro ao registrar pagamento no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após registrar pagamento: " + e.getMessage());
                }
            }
        }
    }

    public Pagamento buscarPagamentoPorId(int id) throws ResourceNotFoundException, ServiceException {
        try {
            Pagamento pagamento = pagamentoDAO.buscarPorId(id);
            if (pagamento == null) {
                throw new ResourceNotFoundException("Pagamento com ID " + id + " não encontrado.");
            }
            return pagamento;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar pagamento por ID: " + e.getMessage(), e);
        }
    }

    public Pagamento buscarPagamentoPorIdPedido(int idPedido) throws ResourceNotFoundException, ServiceException {
        try {
            Pagamento pagamento = pagamentoDAO.buscarPorIdPedido(idPedido);
            if (pagamento == null) {
                throw new ResourceNotFoundException("Nenhum pagamento encontrado para o Pedido ID " + idPedido + ".");
            }
            return pagamento;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar pagamento por ID do pedido: " + e.getMessage(), e);
        }
    }

    public List<Pagamento> listarTodosPagamentos() throws ServiceException {
        try {
            return pagamentoDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todos os pagamentos: " + e.getMessage(), e);
        }
    }

    // NOVO MÉTODO PARA DASHBOARD
    public BigDecimal getFaturamentoTotalPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de faturamento.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            // Operação de leitura, não necessita de gerenciamento de transação explícito (commit/rollback)
            // A conexão será fechada pelo try-with-resources.
            return pagamentoDAO.sumValorTotalPorPeriodo(dataInicial, dataFinal, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao calcular faturamento total por período: " + e.getMessage(), e);
        }
    }
}