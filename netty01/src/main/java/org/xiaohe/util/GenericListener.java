package org.xiaohe.util;

/**
     * 监听器, T 代表 Promise 的实现类。
     * @param <T>
     */
    public interface GenericListener<T> {
        public void operationComplete(T promise);
    }