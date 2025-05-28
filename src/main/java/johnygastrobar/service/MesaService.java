package johnygastrobar.service;

import johnygastrobar.dao.MesaDAO;
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.Mesa;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class MesaService {

    private final MesaDAO mesaDAO;

    @Autowired
    public MesaService(MesaDAO mesaDAO) {
        this.mesaDAO = mesaDAO;
    }

    public Mesa criarMesa(Mesa mesa) throws ServiceException {
        if (mesa == null) {
            throw new IllegalArgumentException("Objeto mesa não pode ser nulo.");
        }
        // Validações de negócio (ex: capacidade deve ser positiva)
        if (mesa.getCapacidade() <= 0) {
            throw new ServiceException("Capacidade da mesa deve ser um número positivo.");
        }
        if (mesa.getLocalizacao() == null || mesa.getLocalizacao().trim().isEmpty()) {
            throw new ServiceException("Localização da mesa é obrigatória.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Mesa novaMesa = mesaDAO.inserir(mesa, conn);

            conn.commit();
            return novaMesa;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRollback) {
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de criar mesa: " + exRollback.getMessage());
                }
            }
            throw new ServiceException("Erro ao criar mesa no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após criar mesa: " + e.getMessage());
                }
            }
        }
    }

    public Mesa buscarMesaPorId(int id) throws ResourceNotFoundException, ServiceException {
        try {
            Mesa mesa = mesaDAO.buscarPorId(id); // Chama a versão do DAO que gerencia a conexão
            if (mesa == null) {
                throw new ResourceNotFoundException("Mesa com ID " + id + " não encontrada.");
            }
            return mesa;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar mesa por ID: " + e.getMessage(), e);
        }
    }

    public List<Mesa> listarTodasMesas() throws ServiceException {
        try {
            return mesaDAO.listarTodos(); // Chama a versão do DAO que gerencia a conexão
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todas as mesas: " + e.getMessage(), e);
        }
    }

    public Mesa atualizarMesa(Mesa mesa) throws ResourceNotFoundException, ServiceException {
        if (mesa == null || mesa.getIdMesa() <= 0) {
            throw new IllegalArgumentException("Dados da mesa inválidos para atualização ou ID não fornecido.");
        }
        if (mesa.getCapacidade() <= 0) {
            throw new ServiceException("Capacidade da mesa deve ser um número positivo.");
        }
        if (mesa.getLocalizacao() == null || mesa.getLocalizacao().trim().isEmpty()) {
            throw new ServiceException("Localização da mesa é obrigatória para atualização.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Mesa mesaExistente = mesaDAO.buscarPorId(mesa.getIdMesa(), conn);
            if (mesaExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Mesa com ID " + mesa.getIdMesa() + " não encontrada para atualização.");
            }

            boolean atualizado = mesaDAO.atualizar(mesa, conn);
            if (!atualizado) {
                throw new ServiceException("Falha ao atualizar a mesa ID " + mesa.getIdMesa() + ", nenhuma linha foi afetada no banco.");
            }

            conn.commit();
            return mesa;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da atualização de mesa: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao atualizar mesa: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da atualização de mesa: " + ex.getMessage());}
            }
        }
    }

    public void deletarMesa(int id) throws ResourceNotFoundException, ServiceException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID da mesa inválido para deleção.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Mesa mesaExistente = mesaDAO.buscarPorId(id, conn);
            if (mesaExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Mesa com ID " + id + " não encontrada para deleção.");
            }

            // A constraint ON DELETE CASCADE na tabela Reserva (se id_mesa for FK para Reserva)
            // ou ON DELETE RESTRICT na tabela Pedido (se id_mesa for FK para Pedido)
            // será acionada aqui. O DAO apenas tenta deletar da tabela Mesa.
            // Se houver restrição FK (como em Pedido), o BD lançará SQLException.
            boolean deletado = mesaDAO.deletar(id, conn);
            if (!deletado) {
                throw new ServiceException("Falha ao deletar a mesa ID " + id + ", nenhuma linha foi afetada (inesperado).");
            }

            conn.commit();
            System.out.println("Mesa ID " + id + " deletada com sucesso (serviço).");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da deleção de mesa: " + ex.getMessage());}
            }
            // Verificar erro de FK (ex: se a mesa está em uso em Pedidos e há ON DELETE RESTRICT)
            // Código de erro 1451 para MySQL: "Cannot delete or update a parent row: a foreign key constraint fails"
            if (e.getErrorCode() == 1451 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key constraint fails"))) {
                throw new ServiceException("Não é possível deletar a mesa ID " + id + " pois ela está referenciada em outros registros (ex: pedidos ou reservas ativas).", e);
            }
            throw new ServiceException("Erro ao deletar mesa: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da deleção de mesa: " + ex.getMessage());}
            }
        }
    }
}