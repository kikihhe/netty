package org.xiaohe.util.concurrent.future;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author : 小何
 * @Description : 作为 Future 的第一个实现类，提供了 get 方法供子类调用
 * @date : 2024-01-23 19:22
 */
public abstract class AbstractFuture<V> implements Future<V> {
    @Override
    public V get() throws InterruptedException, ExecutionException {
        // 阻塞等待
        await();
        // 走出阻塞的情况有三种: 正常结束、异常结束、任务被取消。如果获取不到异常，则为正常结束，返回结果。
        Throwable cause = cause();
        if (cause == null) {
            return getNow();
        }
        if (cause instanceof CancellationException) {
            throw (CancellationException) cause;
        }
        throw new ExecutionException(cause);
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (await(timeout, unit)) {
            Throwable cause = cause();
            if (cause == null) {
                return getNow();
            }
            if (cause instanceof CancellationException) {
                throw (CancellationException) cause;
            }
            throw new ExecutionException(cause);
        }
        throw new TimeoutException();
    }
}
