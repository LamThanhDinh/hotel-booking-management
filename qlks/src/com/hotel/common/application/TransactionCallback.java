package com.hotel.common.application;

@FunctionalInterface
public interface TransactionCallback<T> {
    T execute() throws Exception;
}
