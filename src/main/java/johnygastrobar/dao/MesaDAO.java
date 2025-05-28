package johnygastrobar.dao;

import johnygastrobar.model.Mesa;
import johnygastrobar.util.ConnectionFactory;

import org.springframework.stereotype.Repository; // IMPORT ADICIONADO

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository // ANOTAÇÃO ADICIONADA
public class MesaDAO {

    public Mesa inserir(Mesa mesa, Connection conn) throws SQLException {
        String sql = "INSERT INTO Mesa (capacidade, localizacao) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, mesa.getCapacidade());
            stmt.setString(2, mesa.getLocalizacao());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir mesa, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    mesa.setIdMesa(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir mesa, não foi possível obter o ID gerado.");
                }
            }
        }
        System.out.println("Mesa ID " + mesa.getIdMesa() + " inserida. Localização: " + mesa.getLocalizacao());
        return mesa;
    }

    public boolean atualizar(Mesa mesa, Connection conn) throws SQLException {
        String sql = "UPDATE Mesa SET capacidade = ?, localizacao = ? WHERE id_mesa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mesa.getCapacidade());
            stmt.setString(2, mesa.getLocalizacao());
            stmt.setInt(3, mesa.getIdMesa());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Mesa ID " + mesa.getIdMesa() + " atualizada.");
            }
            return affectedRows > 0;
        }
    }

    public boolean deletar(int idMesa, Connection conn) throws SQLException {
        String sql = "DELETE FROM Mesa WHERE id_mesa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMesa);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Mesa ID " + idMesa + " deletada.");
            }
            // Considerar lançar exceção se a mesa não for encontrada para deleção
            // if (affectedRows == 0) {
            //     throw new SQLException("Mesa com ID " + idMesa + " não encontrada para deleção.");
            // }
            return affectedRows > 0;
        }
    }

    public Mesa buscarPorId(int idMesa, Connection conn) throws SQLException {
        String sql = "SELECT id_mesa, capacidade, localizacao FROM Mesa WHERE id_mesa = ?";
        Mesa mesa = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMesa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mesa = new Mesa(
                            rs.getInt("id_mesa"),
                            rs.getInt("capacidade"),
                            rs.getString("localizacao")
                    );
                }
            }
        }
        return mesa;
    }

    public Mesa buscarPorId(int idMesa) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(idMesa, conn);
        }
    }

    public List<Mesa> listarTodos(Connection conn) throws SQLException {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT id_mesa, capacidade, localizacao FROM Mesa ORDER BY id_mesa";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Mesa(
                        rs.getInt("id_mesa"),
                        rs.getInt("capacidade"),
                        rs.getString("localizacao")
                ));
            }
        }
        return lista;
    }

    public List<Mesa> listarTodos() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return listarTodos(conn);
        }
    }
}