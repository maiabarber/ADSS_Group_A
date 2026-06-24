package dataaccess;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// This class is responsible only for opening a connection to the local SQLite database.
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:data/adss_group_a.db";

    private DatabaseConnection() {
        // Utility class, no objects needed.
    }

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(Path.of("data"));
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver was not found in the classpath", e);
        } catch (Exception e) {
            throw new SQLException("Could not prepare database connection", e);
        }

        return DriverManager.getConnection(DB_URL);
    }
}