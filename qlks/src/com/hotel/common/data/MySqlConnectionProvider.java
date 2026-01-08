package com.hotel.common.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class MySqlConnectionProvider implements ConnectionProvider {
    private final String url;
    private final String username;
    private final String password;
    private final ThreadLocal<Connection> txConnection = new ThreadLocal<>();

    public MySqlConnectionProvider(String url, String username, String password) {
        this.url = Objects.requireNonNull(url, "url");
        this.username = Objects.requireNonNull(username, "username");
        this.password = Objects.requireNonNull(password, "password");
    }

    public static MySqlConnectionProvider fromProperties(Properties props) {
        String url = props.getProperty("db.url", "").trim();
        String user = props.getProperty("db.username", "").trim();
        String pass = props.getProperty("db.password", "").trim();
        if (url.isEmpty()) {
            throw new IllegalArgumentException("db.url is required when db.enabled=true");
        }
        return new MySqlConnectionProvider(url, user, pass);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection existing = txConnection.get();
        if (existing != null) {
            return existing;
        }
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection openNewConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public void bindToCurrentThread(Connection connection) {
        txConnection.set(connection);
    }

    @Override
    public void clearThreadBinding() {
        txConnection.remove();
    }
}
