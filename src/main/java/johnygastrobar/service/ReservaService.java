package johnygastrobar.service;

import johnygastrobar.dao.ReservaDAO;
import johnygastrobar.dao.MesaDAO;
import johnygastrobar.exception.ServiceException;
import johnygastrobar.exception.ResourceNotFoundException;
import johnygastrobar.model.Mesa;
import johnygastrobar.model.Reserva;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaDAO reservaDAO;
    private final MesaDAO mesaDAO;

    @Autowired
    public ReservaService(ReservaDAO reservaDAO, MesaDAO mesaDAO) {
        this.reservaDAO = reservaDAO;
        this.mesaDAO = mesaDAO;
    }

    public Reserva criarReserva(Reserva reserva) throws ServiceException, ResourceNotFoundException {
        if (reserva == null) {
            throw new IllegalArgumentException("Objeto reserva não pode ser nulo.");
        }
        if (reserva.getNomeResponsavel() == null || reserva.getNomeResponsavel().trim().isEmpty()) {
            throw new ServiceException("Nome do responsável pela reserva é obrigatório.");
        }
        if (reserva.getNumeroPessoas() <= 0) {
            throw new ServiceException("Número de pessoas para a reserva deve ser positivo.");
        }
        if (reserva.getDataReserva() == null || reserva.getDataReserva().isBefore(LocalDate.now())) {
            throw new ServiceException("Data da reserva inválida ou no passado.");
        }
        if (reserva.getHoraReserva() == null) {
            throw new ServiceException("Hora da reserva é obrigatória.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Mesa mesa = mesaDAO.buscarPorId(reserva.getIdMesa(), conn);
            if (mesa == null) {
                throw new ResourceNotFoundException("Mesa com ID " + reserva.getIdMesa() + " não encontrada.");
            }
            if (mesa.getCapacidade() < reserva.getNumeroPessoas()) {
                throw new ServiceException("Mesa selecionada não comporta " + reserva.getNumeroPessoas() + " pessoas (capacidade: " + mesa.getCapacidade() + ").");
            }

            if (reservaDAO.verificarConflito(reserva.getIdMesa(), reserva.getDataReserva(), reserva.getHoraReserva(), 0, conn)) {
                throw new ServiceException("Conflito de horário: Já existe uma reserva para esta mesa neste dia/horário.");
            }

            Reserva novaReserva = reservaDAO.inserir(reserva, conn);

            conn.commit();
            return novaReserva;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRollback) {
                    System.err.println("CRÍTICO: Erro ao tentar reverter transação de criar reserva: " + exRollback.getMessage());
                }
            }
            throw new ServiceException("Erro ao criar reserva no banco de dados: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão após criar reserva: " + e.getMessage());
                }
            }
        }
    }

    public Reserva buscarReservaPorId(int id) throws ResourceNotFoundException, ServiceException {
        try {
            Reserva reserva = reservaDAO.buscarPorId(id);
            if (reserva == null) {
                throw new ResourceNotFoundException("Reserva com ID " + id + " não encontrada.");
            }
            return reserva;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar reserva por ID: " + e.getMessage(), e);
        }
    }

    public List<Reserva> listarTodasReservas() throws ServiceException {
        try {
            return reservaDAO.listarTodos();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar todas as reservas: " + e.getMessage(), e);
        }
    }

    public List<Reserva> listarReservasPorData(LocalDate data) throws ServiceException {
        if (data == null) {
            throw new IllegalArgumentException("A data para consulta não pode ser nula.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            List<Reserva> todas = reservaDAO.listarTodos(conn);
            List<Reserva> filtradas = new ArrayList<>();
            for (Reserva r : todas) {
                if (r.getDataReserva().equals(data)) {
                    filtradas.add(r);
                }
            }
            return filtradas;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar reservas por data: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { System.err.println("Erro ao fechar conexão ao listar reservas por data: " + e.getMessage());}
            }
        }
    }

    public Reserva atualizarReserva(Reserva reserva) throws ResourceNotFoundException, ServiceException {
        if (reserva == null || reserva.getIdReserva() <= 0) {
            throw new IllegalArgumentException("Dados da reserva inválidos para atualização ou ID não fornecido.");
        }
        if (reserva.getNomeResponsavel() == null || reserva.getNomeResponsavel().trim().isEmpty()) {
            throw new ServiceException("Nome do responsável pela reserva é obrigatório.");
        }
        if (reserva.getNumeroPessoas() <= 0) {
            throw new ServiceException("Número de pessoas para a reserva deve ser positivo.");
        }
        if (reserva.getDataReserva() == null) {
            throw new ServiceException("Data da reserva é obrigatória.");
        }
        if (reserva.getDataReserva().isBefore(LocalDate.now())) {
            throw new ServiceException("Data da reserva não pode ser no passado para atualização.");
        }
        if (reserva.getHoraReserva() == null) {
            throw new ServiceException("Hora da reserva é obrigatória.");
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Reserva reservaExistente = reservaDAO.buscarPorId(reserva.getIdReserva(), conn);
            if (reservaExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Reserva com ID " + reserva.getIdReserva() + " não encontrada para atualização.");
            }

            if (reservaExistente.getIdMesa() != reserva.getIdMesa() || reserva.getNumeroPessoas() != reservaExistente.getNumeroPessoas()) {
                Mesa mesaParaReserva = mesaDAO.buscarPorId(reserva.getIdMesa(), conn);
                if (mesaParaReserva == null) {
                    throw new ResourceNotFoundException("Mesa com ID " + reserva.getIdMesa() + " não encontrada para a reserva.");
                }
                if (mesaParaReserva.getCapacidade() < reserva.getNumeroPessoas()) {
                    throw new ServiceException("Mesa selecionada (ID: " + mesaParaReserva.getIdMesa() + ") não comporta " + reserva.getNumeroPessoas() + " pessoas (capacidade: " + mesaParaReserva.getCapacidade() + ").");
                }
            }

            if (reservaDAO.verificarConflito(reserva.getIdMesa(), reserva.getDataReserva(), reserva.getHoraReserva(), reserva.getIdReserva(), conn)) {
                throw new ServiceException("Conflito de horário: Já existe outra reserva para esta mesa neste dia/horário.");
            }

            boolean atualizado = reservaDAO.atualizar(reserva, conn);
            if (!atualizado) {
                throw new ServiceException("Falha ao atualizar a reserva ID " + reserva.getIdReserva() + ".");
            }

            conn.commit();
            return reservaDAO.buscarPorId(reserva.getIdReserva(), conn);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da atualização de reserva: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao atualizar reserva: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da atualização de reserva: " + ex.getMessage());}
            }
        }
    }

    public void deletarReserva(int id) throws ResourceNotFoundException, ServiceException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID da reserva inválido para deleção.");
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            Reserva reservaExistente = reservaDAO.buscarPorId(id, conn);
            if (reservaExistente == null) {
                conn.rollback();
                throw new ResourceNotFoundException("Reserva com ID " + id + " não encontrada para deleção.");
            }

            boolean deletado = reservaDAO.deletar(id, conn);
            if (!deletado) {
                throw new ServiceException("Falha ao deletar a reserva ID " + id + ".");
            }

            conn.commit();
            System.out.println("Reserva ID " + id + " deletada com sucesso (serviço).");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("CRÍTICO: Erro no rollback da deleção de reserva: " + ex.getMessage());}
            }
            throw new ServiceException("Erro ao deletar reserva: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Erro ao fechar conexão da deleção de reserva: " + ex.getMessage());}
            }
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD ---
    public int getTotalReservasParaData(LocalDate data) throws ServiceException {
        if (data == null) {
            throw new IllegalArgumentException("Data não pode ser nula para consulta de total de reservas.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return reservaDAO.countReservasParaData(data, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar total de reservas para a data " + data + ": " + e.getMessage(), e);
        }
    }

    public int getTotalPessoasEsperadasParaData(LocalDate data) throws ServiceException {
        if (data == null) {
            throw new IllegalArgumentException("Data não pode ser nula para consulta de total de pessoas esperadas.");
        }
        try (Connection conn = ConnectionFactory.getConnection()) {
            return reservaDAO.sumPessoasReservasParaData(data, conn);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar total de pessoas esperadas para a data " + data + ": " + e.getMessage(), e);
        }
    }
}