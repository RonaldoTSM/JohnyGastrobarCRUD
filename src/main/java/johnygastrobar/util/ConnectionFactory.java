package johnygastrobar.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/johny_gastrobar", // Sua URL de conexão
                "root",       // Seu usuário do banco
                "admin123"    // Sua senha do banco
        );
    }
}