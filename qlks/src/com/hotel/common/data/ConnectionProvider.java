package com.hotel.common.data;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
    Connection getConnection() throws SQLException;

    Connection openNewConnection() throws SQLException;

    void bindToCurrentThread(Connection connection);

    void clearThreadBinding();
}
