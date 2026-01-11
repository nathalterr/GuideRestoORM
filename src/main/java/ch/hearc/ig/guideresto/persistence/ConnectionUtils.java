package ch.hearc.ig.guideresto.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConnectionUtils {

    private static HikariDataSource dataSource;

    static {
        try {
            // Charger les infos depuis resources/database.properties
            ResourceBundle dbProps = ResourceBundle.getBundle("database");
            String url = dbProps.getString("database.url");
            String username = dbProps.getString("database.username");
            String password = dbProps.getString("database.password");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(5); // max 5 connexions simultanées

            dataSource = new HikariDataSource(config);

        } catch (MissingResourceException e) {
            throw new RuntimeException("Impossible de trouver le fichier database.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // récupère une connexion du pool
    }

    public static void closePool() {
        if (dataSource != null) dataSource.close();
    }

}
