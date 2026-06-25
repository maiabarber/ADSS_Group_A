package dataaccess;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// This class is responsible only for opening a connection to the local SQLite database.
public class DatabaseConnection {
    private static final String DEFAULT_DB_PATH = "data/adss_group_a.db";
    private static final String DB_PATH_PROPERTY = "adss.db.path";

    private DatabaseConnection() {
        // Utility class, no objects needed.
    }

    public static Connection getConnection() throws SQLException {
        try {
            String dbPath = getDatabasePath();
            Path parentPath = Path.of(dbPath).getParent();

            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver was not found in the classpath", e);
        } catch (Exception e) {
            throw new SQLException("Could not prepare database connection", e);
        }
    }

    public static String getDatabasePath() {
        return System.getProperty(DB_PATH_PROPERTY, DEFAULT_DB_PATH);
    }
}