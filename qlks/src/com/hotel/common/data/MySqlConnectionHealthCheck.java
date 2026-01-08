package com.hotel.common.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySqlConnectionHealthCheck {
    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("app.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Cannot load app.properties: " + e.getMessage());
            System.exit(1);
            return;
        }

        String url = props.getProperty("db.url", "").trim();
        String user = props.getProperty("db.username", "").trim();
        String pass = props.getProperty("db.password", "");

        if (url.isEmpty()) {
            System.err.println("db.url is empty. Please set it in app.properties");
            System.exit(1);
            return;
        }

        System.out.println("Connecting to: " + url + " as user " + user);
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected. autoCommit=" + conn.getAutoCommit());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            System.exit(2);
        }
    }
}
