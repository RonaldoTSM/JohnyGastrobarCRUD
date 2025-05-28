package johnygastrobar.dao;

import johnygastrobar.model.Reserva;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReservaDAO {

    private static final int DURACAO_RESERVA_MINUTOS = 90;

    public Reserva inserir(Reserva reserva, Connection conn) throws SQLException {
        String sql = "INSERT INTO Reserva (nome_responsavel, numero_pessoas, id_mesa, data_reserva, hora_reserva, observacao) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reserva.getNomeResponsavel());
            stmt.setInt(2, reserva.getNumeroPessoas());
            stmt.setInt(3, reserva.getIdMesa());
            stmt.setDate(4, Date.valueOf(reserva.getDataReserva()));
            stmt.setTime(5, Time.valueOf(reserva.getHoraReserva()));
            stmt.setString(6, reserva.getObservacao());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir reserva, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reserva.setIdReserva(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir reserva, não foi possível obter o ID gerado.");
                }
            }
        }
        System.out.println("Reserva ID " + reserva.getIdReserva() + " inserida para " + reserva.getNomeResponsavel());
        return reserva;
    }

    public boolean atualizar(Reserva reserva, Connection conn) throws SQLException {
        String sql = "UPDATE Reserva SET nome_responsavel = ?, numero_pessoas = ?, id_mesa = ?, data_reserva = ?, hora_reserva = ?, observacao = ? WHERE id_reserva = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reserva.getNomeResponsavel());
            stmt.setInt(2, reserva.getNumeroPessoas());
            stmt.setInt(3, reserva.getIdMesa());
            stmt.setDate(4, Date.valueOf(reserva.getDataReserva()));
            stmt.setTime(5, Time.valueOf(reserva.getHoraReserva()));
            stmt.setString(6, reserva.getObservacao());
            stmt.setInt(7, reserva.getIdReserva());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Reserva ID " + reserva.getIdReserva() + " atualizada.");
            }
            return affectedRows > 0;
        }
    }

    public boolean deletar(int idReserva, Connection conn) throws SQLException {
        String sql = "DELETE FROM Reserva WHERE id_reserva = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReserva);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Reserva ID " + idReserva + " deletada.");
            }
            return affectedRows > 0;
        }
    }

    public Reserva buscarPorId(int idReserva, Connection conn) throws SQLException {
        String sql = "SELECT id_reserva, nome_responsavel, numero_pessoas, id_mesa, data_reserva, hora_reserva, observacao FROM Reserva WHERE id_reserva = ?";
        Reserva reserva = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReserva);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    reserva = new Reserva(
                            rs.getInt("id_reserva"),
                            rs.getString("nome_responsavel"),
                            rs.getInt("numero_pessoas"),
                            rs.getInt("id_mesa"),
                            rs.getDate("data_reserva").toLocalDate(),
                            rs.getTime("hora_reserva").toLocalTime(),
                            rs.getString("observacao")
                    );
                }
            }
        }
        return reserva;
    }

    public Reserva buscarPorId(int idReserva) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idReserva, conn);
        }
    }

    public List<Reserva> listarTodos(Connection conn) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT id_reserva, nome_responsavel, numero_pessoas, id_mesa, data_reserva, hora_reserva, observacao FROM Reserva ORDER BY data_reserva, hora_reserva";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Reserva(
                        rs.getInt("id_reserva"),
                        rs.getString("nome_responsavel"),
                        rs.getInt("numero_pessoas"),
                        rs.getInt("id_mesa"),
                        rs.getDate("data_reserva").toLocalDate(),
                        rs.getTime("hora_reserva").toLocalTime(),
                        rs.getString("observacao")
                ));
            }
        }
        return lista;
    }

    public List<Reserva> listarTodos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarTodos(conn);
        }
    }

    public boolean verificarConflito(int idMesa, LocalDate dataReserva, LocalTime horaReserva, int idReservaExcluir, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva " +
                "WHERE id_mesa = ? AND data_reserva = ? " +
                "AND id_reserva != ? " +
                "AND hora_reserva < ADDTIME(?, SEC_TO_TIME(? * 60)) " +
                "AND ADDTIME(hora_reserva, SEC_TO_TIME(? * 60)) > ? ";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMesa);
            stmt.setDate(2, Date.valueOf(dataReserva));
            stmt.setInt(3, idReservaExcluir);

            stmt.setTime(4, Time.valueOf(horaReserva));
            stmt.setInt(5, DURACAO_RESERVA_MINUTOS);

            stmt.setInt(6, DURACAO_RESERVA_MINUTOS);
            stmt.setTime(7, Time.valueOf(horaReserva));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean verificarConflito(int idMesa, LocalDate dataReserva, LocalTime horaReserva, int idReservaExcluir) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return verificarConflito(idMesa, dataReserva, horaReserva, idReservaExcluir, conn);
        }
    }

    // --- NOVOS MÉTODOS PARA DASHBOARD ---
    public int countReservasParaData(LocalDate data, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(id_reserva) FROM Reserva WHERE data_reserva = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(data));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int sumPessoasReservasParaData(LocalDate data, Connection conn) throws SQLException {
        String sql = "SELECT SUM(numero_pessoas) FROM Reserva WHERE data_reserva = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(data));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // SUM pode retornar NULL se não houver linhas, getInt(0) para NULL.
                }
            }
        }
        return 0; // Retorna 0 se não houver pessoas ou reservas
    }
}