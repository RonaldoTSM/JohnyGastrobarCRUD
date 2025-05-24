package johnygastrobar.dao;

import johnygastrobar.model.Reserva;
import johnygastrobar.util.ConnectionFactory; // Continua sendo usada para listarTodos ou buscarPorId sem conexão externa

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;   // Para java.sql.Date
import java.sql.Time;   // Para java.sql.Time
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    // Duração padrão de uma reserva em minutos. Ajuste conforme a necessidade do negócio.
    private static final int DURACAO_RESERVA_MINUTOS = 90; // 1 hora e 30 minutos

    // MÉTODO INSERIR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public int inserir(Reserva reserva, Connection conn) throws SQLException { // Lança SQLException
        String sql = "INSERT INTO Reserva (nome_responsavel, numero_pessoas, id_mesa, data_reserva, hora_reserva, observacao) VALUES (?, ?, ?, ?, ?, ?)";
        int idGerado = -1;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reserva.getNomeResponsavel());
            stmt.setInt(2, reserva.getNumeroPessoas());
            stmt.setInt(3, reserva.getIdMesa());
            stmt.setDate(4, Date.valueOf(reserva.getDataReserva()));     // Converte LocalDate para java.sql.Date
            stmt.setTime(5, Time.valueOf(reserva.getHoraReserva()));     // Converte LocalTime para java.sql.Time
            stmt.setString(6, reserva.getObservacao());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                idGerado = rs.getInt(1);
                System.out.println("Reserva inserida com ID: " + idGerado);
            } else {
                throw new SQLException("Falha ao obter o ID gerado para a Reserva.");
            }
        }
        return idGerado;
    }

    // MÉTODO LISTAR TODOS CONTINUA ABRINDO SUA PRÓPRIA CONEXÃO
    public List<Reserva> listarTodos() {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT id_reserva, nome_responsavel, numero_pessoas, id_mesa, data_reserva, hora_reserva, observacao FROM Reserva ORDER BY data_reserva, hora_reserva";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Reserva(
                        rs.getInt("id_reserva"),
                        rs.getString("nome_responsavel"),
                        rs.getInt("numero_pessoas"),
                        rs.getInt("id_mesa"),
                        rs.getDate("data_reserva").toLocalDate(), // Converte java.sql.Date para LocalDate
                        rs.getTime("hora_reserva").toLocalTime(), // Converte java.sql.Time para LocalTime
                        rs.getString("observacao")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar reservas: " + e.getMessage());
        }
        return lista;
    }

    // MÉTODO ATUALIZAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void atualizar(Reserva reserva, Connection conn) throws SQLException { // Lança SQLException
        String sql = "UPDATE Reserva SET nome_responsavel = ?, numero_pessoas = ?, id_mesa = ?, data_reserva = ?, hora_reserva = ?, observacao = ? WHERE id_reserva = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reserva.getNomeResponsavel());
            stmt.setInt(2, reserva.getNumeroPessoas());
            stmt.setInt(3, reserva.getIdMesa());
            stmt.setDate(4, Date.valueOf(reserva.getDataReserva()));
            stmt.setTime(5, Time.valueOf(reserva.getHoraReserva()));
            stmt.setString(6, reserva.getObservacao());
            stmt.setInt(7, reserva.getIdReserva());
            stmt.executeUpdate();
            System.out.println("Reserva com ID " + reserva.getIdReserva() + " atualizada com sucesso.");
        }
    }

    // MÉTODO DELETAR AGORA RECEBE UMA CONEXÃO EXISTENTE
    public void deletar(int idReserva, Connection conn) throws SQLException { // Lança SQLException
        String sql = "DELETE FROM Reserva WHERE id_reserva = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReserva);
            stmt.executeUpdate();
            System.out.println("Reserva com ID " + idReserva + " deletada com sucesso.");
        }
    }

    // MÉTODO VERIFICAR CONFLITO AGORA RECEBE UMA CONEXÃO EXISTENTE (ou abre uma nova se null)
    public boolean verificarConflito(int idMesa, LocalDate dataReserva, LocalTime horaReserva, int idReservaExcluir, Connection externalConn) throws SQLException {
        Connection conn = null;
        try {
            if (externalConn != null) {
                conn = externalConn;
            } else {
                conn = ConnectionFactory.getConnection();
            }

            Date sqlDataReserva = Date.valueOf(dataReserva);
            Time sqlHoraReserva = Time.valueOf(horaReserva);

            String sql = "SELECT COUNT(*) FROM Reserva " +
                    "WHERE id_mesa = ? AND data_reserva = ? " +
                    "AND id_reserva != ? " +
                    "AND ( " +
                    "    hora_reserva < ADDTIME(?, SEC_TO_TIME(? * 60)) AND " +
                    "    ADDTIME(hora_reserva, SEC_TO_TIME(? * 60)) > ? " +
                    ");";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idMesa);
                stmt.setDate(2, sqlDataReserva);
                stmt.setInt(3, idReservaExcluir);
                stmt.setTime(4, sqlHoraReserva);
                stmt.setInt(5, DURACAO_RESERVA_MINUTOS);
                stmt.setInt(6, DURACAO_RESERVA_MINUTOS);
                stmt.setTime(7, sqlHoraReserva);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } finally {
            if (externalConn == null && conn != null) { // Fecha a conexão se ela foi aberta por este método
                conn.close();
            }
        }
        return false;
    }

    // Sobrecarga para compatibilidade, manterá a função original que abre e fecha a conexão
    public boolean verificarConflito(int idMesa, LocalDate dataReserva, LocalTime horaReserva, int idReservaExcluir) {
        try {
            return verificarConflito(idMesa, dataReserva, horaReserva, idReservaExcluir, null);
        } catch (SQLException e) {
            System.err.println("Erro ao verificar conflito de reserva (sem conexão externa): " + e.getMessage());
            return false;
        }
    }
}