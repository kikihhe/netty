package Netty;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-13 17:05
 */
public class DefaultPromise<V> implements Promise<V> {
    /**
     * callable的执行结果，类型是T
     */
    private volatile Object result;

    /**
     * 用户提交的任务
     */
    private Callable<V> callable;

    /**
     * 阻塞在这里等待结果的线程总数
     */
    private volatile int waiters;

    /**
     * promise的返回类型为void时，把该属性赋值给result.
     * 如果有用户定义的类型，就是用用户定义的返回值。
     */
    private static final Object SUCCESS = new Object();


    public Promise<V> setSuccess(V result) {
        if (setSuccess0(result)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    private boolean setSuccess0(V result) {
         set(result == null ? (V)SUCCESS : result);
         return true;
    }

    protected void set(V v) {
        result = v;
        // 唤醒所有等待的线程
        checkNotifyWaiters();
    }

    private synchronized void checkNotifyWaiters() {
        if (waiters > 0) {
            notifyAll();
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if (result == null) {
            await();
        }
        return getNow();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    private Promise<V> await() throws InterruptedException {
        if (isDone()) {
            return this;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        synchronized (this) {
            while (!isDone()) {
                ++waiters;
                try {
                    wait();
                } finally {
                    --waiters;
                }
            }
        }
        return this;
    }

    private V getNow() {
        Object result = this.result;
        return (V) result;
    }

    /**
     * 同步等待执行结束
     * @return
     * @throws InterruptedException
     */
    public Promise<V> sync() throws InterruptedException {
        await();
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * 任务是否已经完成
     * @return
     */
    @Override
    public boolean isDone() {
        return isDown0(result);
    }
    private static boolean isDown0(Object result) {
        return result != null;
    }
}
