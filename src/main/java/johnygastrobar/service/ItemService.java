package johnygastrobar.service;

import johnygastrobar.dao.ItemDAO;
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.Item;
import johnygastrobar.model.TopItemInfo; // IMPORT ADICIONADO
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate; // IMPORT ADICIONADO
import java.util.List;

@Service
public class ItemService {

    private final ItemDAO itemDAO;

    @Autowired
    public ItemService(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    public Item criarItem(Item item) throws ServiceException {
        if (item == null) {
            throw new IllegalArgumentException("Objeto item não pode ser nulo.");
        }
        if (item.getNome() == null || item.getNome().trim().isEmpty()) {
            throw new ServiceException("Nome do item é obrigatório.");
        }
        if (item.getPreco() == null || item.getPreco().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Preço do item não pode ser nulo ou negativo.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Item novoItem = itemDAO.inserir(item, conn);
            conn.commit();
            return novoItem;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRollback) {
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de criar item: " + exRollback.getMessage());
                }
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate entry") && e.getMessage().toLowerCase().contains("nome")) {
                throw new ServiceException("Erro ao criar item: Nome '" + item.getNome() + "' já cadastrado.", e);
            }
            throw new ServiceException("Erro ao criar item no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após criar item: " + e.getMessage());
                }
            }
        }
    }

    public Item buscarItemPorId(int id) throws ResourceNotFoundException, ServiceException {
        try {
            Item item = itemDAO.buscarPorId(id);
            if (item == null) {
                throw new ResourceNotFoundException("Item com ID " + id + " não encontrado.");
            }
            return item;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar item por ID: " + e.getMessage(), e);
        }
    }

    public List<Item> listarTodosItens() throws ServiceException {
        try {
            return itemDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todos os itens: " + e.getMessage(), e);
        }
    }

    public Item atualizarItem(Item item) throws ResourceNotFoundException, ServiceException {
        if (item == null || item.getIdItem() <= 0) {
            throw new IllegalArgumentException("Dados do item inválidos para atualização ou ID não fornecido.");
        }
        if (item.getNome() == null || item.getNome().trim().isEmpty()) {
            throw new ServiceException("Nome do item é obrigatório para atualização.");
        }
        if (item.getPreco() == null || item.getPreco().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Preço do item não pode ser nulo ou negativo.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Item itemExistente = itemDAO.buscarPorId(item.getIdItem(), conn);
            if (itemExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Item com ID " + item.getIdItem() + " não encontrado para atualização.");
            }

            boolean atualizado = itemDAO.atualizar(item, conn);
            if (!atualizado) {
                throw new ServiceException("Falha ao atualizar o item ID " + item.getIdItem() + ", nenhuma linha foi afetada no banco.");
            }

            conn.commit();
            return itemDAO.buscarPorId(item.getIdItem(), conn);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da atualização de item: " + ex.getMessage());}
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate entry") && e.getMessage().toLowerCase().contains("nome")) {
                throw new ServiceException("Erro ao atualizar item: Nome '" + item.getNome() + "' já cadastrado para outro item.", e);
            }
            throw new ServiceException("Erro ao atualizar item: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da atualização de item: " + ex.getMessage());}
            }
        }
    }

    public void deletarItem(int id) throws ResourceNotFoundException, ServiceException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID do item inválido para deleção.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Item itemExistente = itemDAO.buscarPorId(id, conn);
            if (itemExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Item com ID " + id + " não encontrado para deleção.");
            }

            boolean deletado = itemDAO.deletar(id, conn);
            if (!deletado) {
                throw new ServiceException("Falha ao deletar o item ID " + id + ", nenhuma linha foi afetada (inesperado).");
            }

            conn.commit();
            System.out.println("Item ID " + id + " deletado com sucesso (serviço).");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da deleção de item: " + ex.getMessage());}
            }
            if (e.getMessage() != null && (e.getMessage().toLowerCase().contains("foreign key constraint fails") || e.getErrorCode() == 1451)) {
                throw new ServiceException("Não é possível deletar o item ID " + id + " pois ele está associado a um ou mais pedidos.", e);
            }
            throw new ServiceException("Erro ao deletar item: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da deleção de item: " + ex.getMessage());}
            }
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD ---
    public List<TopItemInfo> getTopItensMaisVendidosPorQuantidade(LocalDate dataInicial, LocalDate dataFinal, int limite) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal) || limite <= 0) {
            throw new IllegalArgumentException("Parâmetros inválidos para buscar top itens vendidos.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return itemDAO.getTopItensMaisVendidosPorQuantidade(dataInicial, dataFinal, limite, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar top itens mais vendidos por quantidade: " + e.getMessage(), e);
        }
    }

    public List<TopItemInfo> getTopItensMaisRentaveis(LocalDate dataInicial, LocalDate dataFinal, int limite) throws ServiceException {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal) || limite <= 0) {
            throw new IllegalArgumentException("Parâmetros inválidos para buscar top itens mais rentáveis.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return itemDAO.getTopItensMaisRentaveis(dataInicial, dataFinal, limite, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar top itens mais rentáveis: " + e.getMessage(), e);
        }
    }
}