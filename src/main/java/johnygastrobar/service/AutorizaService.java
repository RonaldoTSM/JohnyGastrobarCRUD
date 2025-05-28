package johnygastrobar.service;

import johnygastrobar.dao.AutorizaDAO;
import johnygastrobar.dao.PedidoDAO; // Para validar se o pedido existe
import johnygastrobar.dao.FuncionarioDAO; // Para validar se o gerente existe e é gerente
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.Autoriza;
import johnygastrobar.model.Pedido;
import johnygastrobar.model.Funcionario; // Para verificar se é Gerente
import johnygastrobar.model.Gerente;   // Para verificar se é Gerente
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AutorizaService {

    private final AutorizaDAO autorizaDAO;
    private final PedidoDAO pedidoDAO;
    private final FuncionarioDAO funcionarioDAO;

    @Autowired
    public AutorizaService(AutorizaDAO autorizaDAO, PedidoDAO pedidoDAO, FuncionarioDAO funcionarioDAO) {
        this.autorizaDAO = autorizaDAO;
        this.pedidoDAO = pedidoDAO;
        this.funcionarioDAO = funcionarioDAO;
    }

    public Autoriza criarAutorizacao(Autoriza autoriza) throws ServiceException, ResourceNotFoundException {
        if (autoriza == null) {
            throw new IllegalArgumentException("Objeto autoriza não pode ser nulo.");
        }
        if (autoriza.getIdPedido() <= 0) {
            throw new ServiceException("ID do pedido inválido para a autorização.");
        }
        if (autoriza.getIdGerente() <= 0) {
            throw new ServiceException("ID do gerente inválido para a autorização.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Pedido pedido = pedidoDAO.buscarPorId(autoriza.getIdPedido(), conn);
            if (pedido == null) {
                throw new ResourceNotFoundException("Pedido com ID " + autoriza.getIdPedido() + " não encontrado.");
            }

            Funcionario gerente = funcionarioDAO.buscarPorId(autoriza.getIdGerente(), conn);
            if (gerente == null) {
                throw new ResourceNotFoundException("Gerente com ID " + autoriza.getIdGerente() + " não encontrado.");
            }
            // Verifica se o funcionário é realmente um Gerente
            // O método getTipoFuncionario no FuncionarioDAO pode ser usado, ou instanceof se buscarPorId retornar a subclasse
            if (!(gerente instanceof Gerente)) {
                String tipoFuncionario = funcionarioDAO.getTipoFuncionario(autoriza.getIdGerente(), conn);
                if (!"Gerente".equalsIgnoreCase(tipoFuncionario)) {
                    throw new ServiceException("Funcionário com ID " + autoriza.getIdGerente() + " não é um gerente válido.");
                }
            }


            if (autoriza.getDataAutorizacao() == null) {
                autoriza.setDataAutorizacao(LocalDateTime.now());
            }

            Autoriza novaAutorizacao = autorizaDAO.inserir(autoriza, conn);

            conn.commit();
            return novaAutorizacao;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRollback) {
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de criar autorização: " + exRollback.getMessage());
                }
            }
            throw new ServiceException("Erro ao criar autorização no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após criar autorização: " + e.getMessage());
                }
            }
        }
    }

    public Autoriza buscarAutorizacaoPorId(int id) throws ResourceNotFoundException, ServiceException {
        try {
            Autoriza autoriza = autorizaDAO.buscarPorId(id);
            if (autoriza == null) {
                throw new ResourceNotFoundException("Autorização com ID " + id + " não encontrada.");
            }
            return autoriza;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar autorização por ID: " + e.getMessage(), e);
        }
    }

    public List<Autoriza> listarTodasAutorizacoes() throws ServiceException {
        try {
            return autorizaDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todas as autorizações: " + e.getMessage(), e);
        }
    }

    public List<Autoriza> listarAutorizacoesPorPedido(int idPedido) throws ServiceException {
        if (idPedido <= 0) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }
        try {
            return autorizaDAO.listarPorPedido(idPedido);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar autorizações por pedido: " + e.getMessage(), e);
        }
    }

    public List<Autoriza> listarAutorizacoesPorGerente(int idGerente) throws ServiceException {
        if (idGerente <= 0) {
            throw new IllegalArgumentException("ID do gerente inválido.");
        }
        try {
            return autorizaDAO.listarPorGerente(idGerente);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar autorizações por gerente: " + e.getMessage(), e);
        }
    }

    public Autoriza atualizarAutorizacao(Autoriza autoriza) throws ResourceNotFoundException, ServiceException {
        if (autoriza == null || autoriza.getIdAutorizacao() <= 0) {
            throw new IllegalArgumentException("Dados de autorização inválidos ou ID não fornecido.");
        }
        // Adicionar outras validações se necessário (ex: idPedido, idGerente)

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Autoriza existente = autorizaDAO.buscarPorId(autoriza.getIdAutorizacao(), conn);
            if (existente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Autorização com ID " + autoriza.getIdAutorizacao() + " não encontrada para atualização.");
            }

            // Validar pedido e gerente novamente se eles puderem ser alterados na autorização
            if (existente.getIdPedido() != autoriza.getIdPedido()) {
                Pedido pedido = pedidoDAO.buscarPorId(autoriza.getIdPedido(), conn);
                if (pedido == null) {
                    throw new ResourceNotFoundException("Novo Pedido com ID " + autoriza.getIdPedido() + " não encontrado.");
                }
            }
            if (existente.getIdGerente() != autoriza.getIdGerente()) {
                Funcionario gerente = funcionarioDAO.buscarPorId(autoriza.getIdGerente(), conn);
                if (gerente == null || !(gerente instanceof Gerente)) { // Ou usar getTipoFuncionario
                    throw new ResourceNotFoundException("Novo Gerente com ID " + autoriza.getIdGerente() + " não é válido.");
                }
            }


            boolean atualizado = autorizaDAO.atualizar(autoriza, conn);
            if (!atualizado) {
                throw new ServiceException("Falha ao atualizar a autorização ID " + autoriza.getIdAutorizacao());
            }

            conn.commit();
            return autorizaDAO.buscarPorId(autoriza.getIdAutorizacao(), conn);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da atualização de autorização: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao atualizar autorização: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da atualização de autorização: " + ex.getMessage());}
            }
        }
    }

    public void deletarAutorizacao(int id) throws ResourceNotFoundException, ServiceException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID da autorização inválido para deleção.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Autoriza existente = autorizaDAO.buscarPorId(id, conn);
            if (existente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Autorização com ID " + id + " não encontrada para deleção.");
            }

            boolean deletado = autorizaDAO.deletar(id, conn);
            if (!deletado) {
                throw new ServiceException("Falha ao deletar a autorização ID " + id);
            }

            conn.commit();
            System.out.println("Autorização ID " + id + " deletada com sucesso (serviço).");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da deleção de autorização: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao deletar autorização: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da deleção de autorização: " + ex.getMessage());}
            }
        }
    }
}