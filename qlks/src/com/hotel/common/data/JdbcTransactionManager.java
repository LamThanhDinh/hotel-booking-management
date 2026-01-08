package com.hotel.common.data;

import com.hotel.common.application.TransactionCallback;
import com.hotel.common.application.TransactionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransactionManager implements TransactionManager {
    private final MySqlConnectionProvider connectionProvider;

    public JdbcTransactionManager(MySqlConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public <T> T runInTransaction(TransactionCallback<T> work) throws Exception {
        Connection connection = connectionProvider.openNewConnection();
        connectionProvider.bindToCurrentThread(connection);
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            T result = work.execute();
            connection.commit();
            return result;
        } catch (Exception ex) {
            rollbackQuietly(connection);
            throw ex;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException ignored) {
            }
            connectionProvider.clearThreadBinding();
            closeQuietly(connection);
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
