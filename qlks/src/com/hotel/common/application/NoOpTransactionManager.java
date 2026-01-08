package com.hotel.common.application;

public class NoOpTransactionManager implements TransactionManager {
    @Override
    public <T> T runInTransaction(TransactionCallback<T> work) throws Exception {
        return work.execute();
    }
}
