package com.hotel.common.application;

public interface TransactionManager {
    <T> T runInTransaction(TransactionCallback<T> work) throws Exception;
}
