package johnygastrobar.service;

import johnygastrobar.dao.FeedbackPedidoDAO;
import johnygastrobar.dao.PedidoDAO;
import johnygastrobar.dao.MesaDAO; // Necessário se validar a mesa diretamente no feedback
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.FeedbackPedido;
import johnygastrobar.model.Pedido;
import johnygastrobar.model.Mesa;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackPedidoService {

    private final FeedbackPedidoDAO feedbackPedidoDAO;
    private final PedidoDAO pedidoDAO;
    private final MesaDAO mesaDAO;

    @Autowired
    public FeedbackPedidoService(FeedbackPedidoDAO feedbackPedidoDAO, PedidoDAO pedidoDAO, MesaDAO mesaDAO) {
        this.feedbackPedidoDAO = feedbackPedidoDAO;
        this.pedidoDAO = pedidoDAO;
        this.mesaDAO = mesaDAO;
    }

    public FeedbackPedido criarFeedback(FeedbackPedido feedback) throws ServiceException, ResourceNotFoundException {
        if (feedback == null) {
            throw new IllegalArgumentException("Objeto feedback não pode ser nulo.");
        }
        if (feedback.getIdPedido() <= 0) {
            throw new ServiceException("ID do pedido inválido para o feedback.");
        }
        // Validações das notas (1-5 ou nulo)
        if (feedback.getNotaComida() != null && (feedback.getNotaComida() < 1 || feedback.getNotaComida() > 5)) {
            throw new ServiceException("Nota da comida deve ser entre 1 e 5, ou nula.");
        }
        if (feedback.getNotaAtendimento() != null && (feedback.getNotaAtendimento() < 1 || feedback.getNotaAtendimento() > 5)) {
            throw new ServiceException("Nota do atendimento deve ser entre 1 e 5, ou nula.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Pedido pedido = pedidoDAO.buscarPorId(feedback.getIdPedido(), conn);
            if (pedido == null) {
                throw new ResourceNotFoundException("Pedido com ID " + feedback.getIdPedido() + " não encontrado para associar ao feedback.");
            }

            // REQUISITO: Só deve ser possível fazê-los para pedidos realizados no dia atual.
            if (pedido.getDataHora() == null || !pedido.getDataHora().toLocalDate().equals(LocalDate.now())) {
                throw new ServiceException("Feedback só pode ser registrado para pedidos realizados no dia de hoje.");
            }

            // Se idMesa no feedback não foi preenchido, usar o do pedido.
            // Ou validar se o idMesa do feedback corresponde ao do pedido.
            if (feedback.getIdMesa() <= 0 && pedido.getIdMesa() > 0) {
                feedback.setIdMesa(pedido.getIdMesa());
            } else if (feedback.getIdMesa() > 0 && feedback.getIdMesa() != pedido.getIdMesa()) {
                throw new ServiceException("A mesa informada no feedback (ID: " + feedback.getIdMesa() +
                        ") não corresponde à mesa do pedido (ID: " + pedido.getIdMesa() + ").");
            } else if (feedback.getIdMesa() <= 0 && pedido.getIdMesa() <= 0) { // Caso raro
                throw new ServiceException("ID da mesa não pôde ser determinado para o feedback.");
            }

            // Validar se a mesa ainda existe (opcional, FK deve cuidar disso)
            Mesa mesa = mesaDAO.buscarPorId(feedback.getIdMesa(), conn);
            if (mesa == null) {
                throw new ResourceNotFoundException("Mesa com ID " + feedback.getIdMesa() + " especificada no feedback não foi encontrada.");
            }

            if (feedback.getDataFeedback() == null) {
                feedback.setDataFeedback(LocalDateTime.now());
            }

            FeedbackPedido novoFeedback = feedbackPedidoDAO.inserir(feedback, conn);

            conn.commit();
            return novoFeedback;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRollback) {
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de criar feedback: " + exRollback.getMessage());
                }
            }
            throw new ServiceException("Erro ao criar feedback no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após criar feedback: " + e.getMessage());
                }
            }
        }
    }

    public FeedbackPedido buscarFeedbackPorId(int id) throws ResourceNotFoundException, ServiceException {
        try {
            FeedbackPedido feedback = feedbackPedidoDAO.buscarPorId(id);
            if (feedback == null) {
                throw new ResourceNotFoundException("Feedback com ID " + id + " não encontrado.");
            }
            return feedback;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar feedback por ID: " + e.getMessage(), e);
        }
    }

    public List<FeedbackPedido> listarTodosFeedbacks() throws ServiceException {
        try {
            return feedbackPedidoDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todos os feedbacks: " + e.getMessage(), e);
        }
    }

    public List<FeedbackPedido> listarFeedbacksPorPedido(int idPedido) throws ServiceException {
        if (idPedido <= 0) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }
        try {
            return feedbackPedidoDAO.listarPorPedido(idPedido);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar feedbacks por pedido: " + e.getMessage(), e);
        }
    }

    public FeedbackPedido atualizarFeedback(FeedbackPedido feedback) throws ResourceNotFoundException, ServiceException {
        if (feedback == null || feedback.getIdFeedback() <= 0) {
            throw new IllegalArgumentException("Dados de feedback inválidos ou ID não fornecido.");
        }
        // Adicionar validações para notas (1-5 ou nulo) como no criarFeedback
        if (feedback.getNotaComida() != null && (feedback.getNotaComida() < 1 || feedback.getNotaComida() > 5)) {
            throw new ServiceException("Nota da comida deve ser entre 1 e 5, ou nula.");
        }
        if (feedback.getNotaAtendimento() != null && (feedback.getNotaAtendimento() < 1 || feedback.getNotaAtendimento() > 5)) {
            throw new ServiceException("Nota do atendimento deve ser entre 1 e 5, ou nula.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            FeedbackPedido existente = feedbackPedidoDAO.buscarPorId(feedback.getIdFeedback(), conn);
            if (existente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Feedback com ID " + feedback.getIdFeedback() + " não encontrado para atualização.");
            }

            // A lógica de restringir a atualização de feedback a pedidos do dia pode ser adicionada aqui também se necessário.
            // No entanto, geralmente, um feedback uma vez dado pode ser editado por um tempo, independente da data do pedido.
            // Vamos assumir que a restrição do dia é apenas para CRIAÇÃO.

            boolean atualizado = feedbackPedidoDAO.atualizar(feedback, conn);
            if (!atualizado) {
                throw new ServiceException("Falha ao atualizar o feedback ID " + feedback.getIdFeedback());
            }

            conn.commit();
            return feedbackPedidoDAO.buscarPorId(feedback.getIdFeedback(), conn);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da atualização de feedback: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao atualizar feedback: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da atualização de feedback: " + ex.getMessage());}
            }
        }
    }

    public void deletarFeedback(int id) throws ResourceNotFoundException, ServiceException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID do feedback inválido para deleção.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            FeedbackPedido existente = feedbackPedidoDAO.buscarPorId(id, conn);
            if (existente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Feedback com ID " + id + " não encontrado para deleção.");
            }

            boolean deletado = feedbackPedidoDAO.deletar(id, conn);
            if (!deletado) {
                throw new ServiceException("Falha ao deletar o feedback ID " + id);
            }

            conn.commit();
            System.out.println("Feedback ID " + id + " deletado com sucesso (serviço).");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da deleção de feedback: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao deletar feedback: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da deleção de feedback: " + ex.getMessage());}
            }
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD ---
    public BigDecimal getNotaMediaComidaPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de nota média.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return feedbackPedidoDAO.avgNotaComidaPorPeriodo(dataInicial, dataFinal, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao calcular nota média da comida por período: " + e.getMessage(), e);
        }
    }

    public BigDecimal getNotaMediaAtendimentoPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de nota média.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return feedbackPedidoDAO.avgNotaAtendimentoPorPeriodo(dataInicial, dataFinal, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao calcular nota média do atendimento por período: " + e.getMessage(), e);
        }
    }

    public int getTotalFeedbacksPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Datas inválidas para consulta de total de feedbacks.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return feedbackPedidoDAO.countFeedbacksPorPeriodo(dataInicial, dataFinal, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao contar feedbacks por período: " + e.getMessage(), e);
        }
    }
}