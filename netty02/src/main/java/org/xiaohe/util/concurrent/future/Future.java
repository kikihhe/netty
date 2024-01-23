package org.xiaohe.util.concurrent.future;

import org.xiaohe.util.concurrent.listener.GenericFutureListener;

import java.util.concurrent.TimeUnit;

/**
 * @author : 小何
 * @Description : 自定义Future
 * @date : 2024-01-23 19:13
 */
public interface Future<V> extends java.util.concurrent.Future<V> {
    boolean isSuccess();

    boolean isCancellable();

    Throwable cause();

    Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    Future<V> addListeners(GenericFutureListener<? extends Future<? super V>> ... listeners);

    Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);
    Future<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listener);


    Future<V> sync() throws InterruptedException;

    Future<V> syncUninterruptibly();

    Future<V> await() throws InterruptedException;

    Future<V> awaitUninterruptibly() throws InterruptedException ;

    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    boolean await(long timoutMills) throws InterruptedException;

    boolean awaitUninterruptibly(long timeoutMills) throws InterruptedException ;
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) throws InterruptedException;

    V getNow();

    @Override
    boolean cancel(boolean mayInterruptIfRunning);
}
