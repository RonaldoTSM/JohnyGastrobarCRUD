package johnygastrobar.service;

import johnygastrobar.dao.FuncionarioDAO;
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.Funcionario; // Incluindo subclasses se forem referenciadas diretamente
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class FuncionarioService {

    private final FuncionarioDAO funcionarioDAO;

    @Autowired
    public FuncionarioService(FuncionarioDAO funcionarioDAO) {
        this.funcionarioDAO = funcionarioDAO;
    }

    public Funcionario criarFuncionario(Funcionario funcionario) throws ServiceException {
        if (funcionario == null) {
            throw new IllegalArgumentException("Objeto funcionário não pode ser nulo.");
        }
        // Validações de negócio básicas podem vir aqui
        // Ex: if (funcionario.getCpf() == null || funcionario.getCpf().trim().isEmpty()) {
        //     throw new ValidationException("CPF do funcionário é obrigatório.");
        // }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Funcionario novoFuncionario = funcionarioDAO.inserir(funcionario, conn);

            conn.commit();
            return novoFuncionario;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException exRollback) {
                    // Logar exRollback
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de criar funcionário: " + exRollback.getMessage());
                }
            }
            // Verificar se o erro é de CPF duplicado (exemplo, depende da mensagem do seu BD/Driver)
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate entry") && e.getMessage().toLowerCase().contains("cpf")) {
                throw new ServiceException("Erro ao criar funcionário: CPF já cadastrado.", e);
            }
            throw new ServiceException("Erro ao criar funcionário no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Logar
                    System.err.println("Erro ao fechar conexão após criar funcionário: " + e.getMessage());
                }
            }
        }
    }

    public Funcionario buscarFuncionarioPorId(int id) throws ResourceNotFoundException, ServiceException {
        // Para operações de leitura simples, o DAO pode gerenciar sua própria conexão se quisermos,
        // ou podemos gerenciar aqui para consistência, embora não haja transação de escrita.
        // A versão atual do funcionarioDAO.buscarPorId(id) gerencia sua própria conexão.
        try {
            Funcionario funcionario = funcionarioDAO.buscarPorId(id); // Chama a versão que gerencia a conexão
            if (funcionario == null) {
                throw new ResourceNotFoundException("Funcionário com ID " + id + " não encontrado.");
            }
            return funcionario;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar funcionário por ID: " + e.getMessage(), e);
        }
    }

    public List<Funcionario> listarTodosFuncionarios() throws ServiceException {
        try {
            // Chama a versão do DAO que gerencia sua própria conexão
            return funcionarioDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todos os funcionários: " + e.getMessage(), e);
        }
    }

    public Funcionario atualizarFuncionario(Funcionario funcionario) throws ResourceNotFoundException, ServiceException {
        if (funcionario == null || funcionario.getId() <= 0) {
            throw new IllegalArgumentException("Dados do funcionário inválidos para atualização ou ID não fornecido.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // O método do DAO já deve lidar com "não encontrado" lançando exceção ou retornando um indicativo.
            // A versão atualizada do atualizarCompleto no DAO deve retornar o funcionário ou lançar exceção.
            Funcionario funcionarioAtualizado = funcionarioDAO.atualizarCompleto(funcionario, conn);
            // Se o DAO não lançar exceção para "não encontrado" mas retornar null, por exemplo:
            // if (funcionarioAtualizado == null) {
            //     conn.rollback(); // Importante reverter se a entidade não foi encontrada para atualizar
            //     throw new ResourceNotFoundException("Funcionário com ID " + funcionario.getId() + " não encontrado para atualização.");
            // }

            conn.commit();
            return funcionarioAtualizado; // Retorna o funcionário com os dados atualizados

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da atualização de funcionário: " + ex.getMessage());}
            }
            // Exemplo de como tratar erro de "não encontrado" se o DAO lançar SQLException específica
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("não encontrado")) {
                throw new ResourceNotFoundException("Funcionário com ID " + funcionario.getId() + " não encontrado para atualização.", e);
            }
            throw new ServiceException("Erro ao atualizar funcionário: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da atualização de funcionário: " + ex.getMessage());}
            }
        }
    }

    public void deletarFuncionario(int id) throws ResourceNotFoundException, ServiceException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido para deleção.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // O método deletar do DAO agora lança SQLException se não encontrar
            funcionarioDAO.deletar(id, conn);

            conn.commit();
            System.out.println("Funcionário ID " + id + " deletado com sucesso (serviço).");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da deleção de funcionário: " + ex.getMessage());}
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("não encontrado para deleção")) {
                throw new ResourceNotFoundException("Funcionário com ID " + id + " não encontrado para deleção.", e);
            }
            throw new ServiceException("Erro ao deletar funcionário: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da deleção de funcionário: " + ex.getMessage());}
            }
        }
    }
}