package org.xiaohe.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-22 18:22
 */
public class DefaultPromise<V> implements Promise<V> {
    /**
     * 代表执行成功
     */
    private static final Object SUCCESS = new Object();
    private Object result;

    private List<GenericListener> listeners = new ArrayList<>();
    /**
     * get阻塞的线程数量
     */
    private int waiters;

    public Promise<V> setSuccess(V result) {
        if (setSuccess0(result)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }
    private boolean setSuccess0(V result) {
//        set(result == null ? (V) SUCCESS : result);
        this.result = result;
        // 唤醒所有等待的线程
        checkNotifyWaiters();
        // 执行所有回调
        notifyListeners();
        return true;
    }

    protected void set(V v) {
        result = v;
        // 唤醒所有等待的线程
        checkNotifyWaiters();
        // 执行所有回调
        notifyListeners();
    }
    /**
     * 同步等待结果
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

    @Override
    public boolean isDone() {
        return isDone0();
    }

    private boolean isDone0() {
        return result != null;
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
        await(timeout, unit);
        return getNow();
    }
    /**
     * 等待
     * @return
     */
    public Promise<V> await() throws InterruptedException {
        if(isDone()) {
            return this;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException(toString());
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
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        if (isDone()) {
            return true;
        }
        if (timeout <= 0) {
            return true;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        timeout = unit.toNanos(timeout);
        long waitTime = timeout;
        long startTime = System.currentTimeMillis();
        for (;;) {
            synchronized (this) {
                if (isDone()) {
                    return true;
                }
                ++waiters;
                try {
                    wait(waitTime);
                } finally {
                    --waiters;
                }
            }
            if (isDone()) {
                return true;
            } else {
                waitTime = timeout - (System.currentTimeMillis() - startTime);
                if (waitTime <= 0) {
                    return isDone();
                }
            }
        }
    }

    private synchronized void checkNotifyWaiters() {
        if (waiters > 0) {
            notifyAll();
        }
    }

    public V getNow() {
        return (V) this.result;
    }

    private void notifyListeners() {
        for (GenericListener listener : listeners) {
            listener.operationComplete(this);
        }
    }

    /**
     * 添加监听器
     * @param listener
     * @return
     */
    public Promise<V> addListener(GenericListener<? extends Promise<? super V>> listener) {
        synchronized (this) {
            listeners.add(listener);
        }
        if (isDone()) {
            notifyListeners();
        }
        return this;
    }

    public static void main(String[] args) throws Exception {
        DefaultPromise<Integer> promise = new DefaultPromise<>();
        promise.addListener(new GenericListener<Promise<? super Integer>>() {
            @Override
            public void operationComplete(Promise<? super Integer> promise) {
                try {
                    System.out.println("回调方法中获取任务的执行结果" + promise.get());
                } catch (Exception e) {

                }
            }
        });

        new Thread(() -> {
            System.out.println("执行任务");
            System.out.println("执行任务");
            System.out.println("执行任务");

            promise.setSuccess(10);

            System.out.println("执行任务");
            System.out.println("执行任务");
            System.out.println("执行任务");
        }).start();

        System.out.println("主线程获取的结果: " + promise.get()); // wait
        Thread.sleep(2000);
    }

}
