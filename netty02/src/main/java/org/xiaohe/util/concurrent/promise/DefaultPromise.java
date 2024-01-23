package org.xiaohe.util.concurrent.promise;

import org.xiaohe.BlockingOperationException;
import org.xiaohe.util.concurrent.future.AbstractFuture;
import org.xiaohe.util.concurrent.future.Future;
import org.xiaohe.util.concurrent.listener.DefaultFutureListeners;
import org.xiaohe.util.concurrent.listener.GenericFutureListener;
import org.xiaohe.util.executor.EventExecutor;
import org.xiaohe.util.internal.StringUtil;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-23 20:12
 */
public class DefaultPromise<V> extends AbstractFuture<V> implements Promise<V> {
    /**
     * 每一个 promise 都要有执行器来执行
     */
    private final EventExecutor executor;
    /**
     * 最终结果
     */
    private volatile Object result;

    private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");
    /**
     * 当异步任务没有返回值时，将这个 SUCCESS 赋值给 result
     */
    private static final Object SUCCESS = new Object();

    /**
     * 当异步任务不可取消时，原子更新器使用该值更新结果
     */
    private static final Object UNCANCELLABLE = new Object();

    private Object listeners;
    /**
     * 等待获取结果，也就是阻塞的线程数量
     */
    private short waiters;
    /**
     * 保证监听器只执行一遍
     */
    private volatile boolean notifyingListeners;


    public DefaultPromise(EventExecutor executor) {
        this.executor = executor;
    }

    public DefaultPromise() {
        executor = null;
    }

    /**
     * 继承自 Promise，在 AbstractFuture.get() 被调用，用于阻塞当前线程
     * @return
     * @throws InterruptedException
     */
    @Override
    public Promise<V> await() throws InterruptedException {
        if (isDone()) {
            return this;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        // 检查是否死锁，如果是死锁直接抛出异常
        // TODO 这里没有搞懂为啥要检查死锁
        // 以下为 cqfy 的注释:
        // 如果熟悉了netty之后，就会发现，凡事结果要赋值到promise的任务都是由netty中的单线程执行器来执行的
        // 执行每个任务的执行器是和channel绑定的。如果某个执行器正在执行任务，但是还未获得结果，这时候该执行器
        // 又来获取结果，一个线程怎么能同时执行任务又要唤醒自己呢，所以必然会产生死锁
        checkDeadLock();

        synchronized (this) {
            while (!isDone()) {
                incWaiters();
                try {
                    wait();
                } finally {
                    decWaiters();
                }
            }
        }
        return this;
    }
    @Override
    public Promise<V> awaitUninterruptibly() {
        if (isDone()) {
            return this;
        }
        checkDeadLock();
        boolean interrupted = false;
        synchronized (this) {
            while (!isDone()) {
                incWaiters();
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                } finally {
                    decWaiters();
                }
            }
        }
        // 如果发生异常, 设置中断标志
        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return this;
    }
    public void checkDeadLock() {
        EventExecutor e = executor();
        if (e != null && e.inEventLoop(Thread.currentThread())) {
            throw new BlockingOperationException(toString());
        }
    }
    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return await0(unit.toNanos(timeout), true);
    }
    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return await0(MILLISECONDS.toNanos(timeoutMillis), true);
    }
    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            //不会抛出异常
            return await0(unit.toNanos(timeout), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }
    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) throws InterruptedException  {
        try {
            return await0(MILLISECONDS.toNanos(timeoutMillis), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    /**
     * 同步等待结果
     * @return
     * @throws InterruptedException
     */
    @Override
    public Promise<V> sync() throws InterruptedException {
        await();
        rethrowIfFailed();
        return this;
    }
    private void rethrowIfFailed() {
        Throwable cause = cause();
        if (cause == null) {
            return;
        }
        //暂时先不从源码中引入该工具类
        //PlatformDependent.throwException(cause);
    }

    @Override
    public Promise<V> syncUninterruptibly() {
        awaitUninterruptibly();
        rethrowIfFailed();
        return this;
    }
    private boolean await0(long timeoutNanos, boolean interruptable) throws InterruptedException {
        if (isDone()) {
            return true;
        }
        if (timeoutNanos <= 0) {
            return isDone();
        }
        if (interruptable && Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        checkDeadLock();
        long startTime = System.currentTimeMillis();
        long waitTime = timeoutNanos;
        boolean interrupted = false;
        try {
            for (;;) {
                synchronized (this) {
                    if (isDone()) {
                        return true;
                    }
                    incWaiters();
                    try {
                        wait(waitTime / 1000000, (int) (waitTime % 1000000) );
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        } else {
                            interrupted = true;
                        }
                    } finally {
                        decWaiters();
                    }
                }
                // 走到这里说明线程被唤醒了
                if (isDone()) {
                    return true;
                } else {
                    waitTime = timeoutNanos - (System.nanoTime() - startTime);
                    if (waitTime <= 0) {
                        return isDone();
                    }
                }
            }
        } finally {
            // 退出方法前判断是否要给执行任务的线程添加中断标记
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 继承自 Future，判断任务是否执行结束
     */
    @Override
    public boolean isSuccess() {
        Object result = this.result;
        if (result == null) return false;
        if (result == UNCANCELLABLE) return false;
        if (result instanceof CauseHolder) return false;
        return true;
    }

    /**
     * 继承自 Future，立刻获取任务结果
     */
    @Override
    public V getNow() {
        Object result = this.result;
        // 如果执行失败，只能返回null
        if (!isSuccess()) {
            return null;
        }
        // 如果执行成功，有两种情况: 无需返回值，需要返回值
        if (result == SUCCESS) {
            return null;
        }
        return (V) result;

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // 原子更新器得到当前result的值，如果为null，说明任务还未执行完成，并且没有被取消
        if (RESULT_UPDATER.get(this) == null &&
                // 原子更新器把被包装过的CancellationException赋值给result
                RESULT_UPDATER.compareAndSet(this, null, new CauseHolder(new CancellationException()))) {
            // 如果上面的操作成功了就唤醒之前wait的线程
            if (checkNotifyWaiters()) {
                // 通知所有监听器执行
                notifyListeners();
            }
            return true;
        }
        // 如果取消失败则说明result已经有值了
        return false;
    }

    @Override
    public boolean isCancelled() {
        return result instanceof CauseHolder && ((CauseHolder) result).cause instanceof CancellationException;
    }

    /**
     * 继承自 Future，判断任务是否结束
     * @return
     */
    @Override
    public boolean isDone() {
        return result != null && result != UNCANCELLABLE;
    }

    /**
     * 继承自 Promise，设置当前任务为不可取消
     * @return
     */
    @Override
    public boolean setUncancellable() {
        if (RESULT_UPDATER.compareAndSet(this, null, UNCANCELLABLE)) {
            return true;
        }
        // 走到这里说明之前已经设置过了，所以此次CAS失败。
        return isDone() || isCancelled();
    }

    /**
     * 继承自 Future，判断任务是否取消
     * @return
     */
    @Override
    public boolean isCancellable() {
        return result instanceof CauseHolder && ((CauseHolder) result).cause instanceof CancellationException;
    }

    /**
     * 继承自 Promise，任务执行结束，如果设置结果失败，抛出异常
     * @param result
     * @return
     */
    @Override
    public Promise<V> setSuccess(V result) {
        if (setSuccess0(result)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    /**
     * 继承自 Promise，任务执行结束，如果设置失败不抛出异常
     * @param result
     * @return
     */
    @Override
    public boolean trySuccess(V result) {
        return setSuccess0(result);
    }

    /**
     * 继承自 Promise，任务执行失败
     * @param cause
     * @return
     */
    @Override
    public Promise<V> setFailure(Throwable cause) {
        if (setFailure0(cause)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    /**
     * 继承自 Promise，任务执行失败
     * @param cause
     * @return
     */
    @Override
    public boolean tryFailure(Throwable cause) {
        return setFailure0(cause);
    }

    private boolean setSuccess0(V result) {
        return setValue0(result == null ? SUCCESS : result);
    }
    private boolean setFailure0(Throwable cause) {
        return setValue0(new CauseHolder(cause));
    }
    private boolean setValue0(Object objResult) {
        // 如果CAS赋值成功，可以唤醒所有阻塞的线程
        if (RESULT_UPDATER.compareAndSet(this, null, objResult) ||
                RESULT_UPDATER.compareAndSet(this, UNCANCELLABLE, objResult)) {
            if (checkNotifyWaiters()) {
                // 执行回调
                notifyListeners();
            }
            return true;
        }
        return false;
    }

    private synchronized boolean checkNotifyWaiters() {
        if (waiters > 0) {
            notifyAll();
        }
        // 唤醒之后返回这个 promise 是否需要执行回调方法
        return listeners != null;
    }
    private void notifyListeners() {
        EventExecutor executor = executor();
        // 如果当前线程就是执行器的线程，就立刻执行回调方法
        if (executor.inEventLoop(Thread.currentThread())) {
            notifyListenersNow();
        }
        // 否则就让 executor 执行
        safeExecute(executor, new Runnable() {
            @Override
            public void run() {
                notifyListenersNow();
            }
        });
    }

    private void notifyListenersNow() {
        Object listeners;
        synchronized (this) {
            // 如果已经执行过了，或者没有回调需要执行，直接返回
            if (notifyingListeners || this.listeners == null) {
                return;
            }
            notifyingListeners = true;
            listeners = this.listeners;
            this.listeners = null;
        }
        for (;;) {
            // listeners 变量可能是多个回调，也可能是一个回调
            if (listeners instanceof DefaultFutureListeners) {
                notifyListeners0((DefaultFutureListeners) listeners);
            } else {
                // 说明只有一个监听器
                notifyListener0(this, (GenericFutureListener<?>) listeners);
            }
            // 执行回调之后，重置此 promise 的状态
            synchronized (this) {
                if (this.listeners == null) {
                    notifyingListeners = false;
                    return;
                }
                // 能走到这说明再执行完回调之后 && 重置 promise 状态之前又添加了监听器
                // 这时就要再次执行循环
                listeners = this.listeners;
                this.listeners = null;
            }
        }
    }

    private void notifyListeners0(DefaultFutureListeners listeners) {
        GenericFutureListener<? extends Future<?>>[] a = listeners.listeners();
        int size = listeners.size();
        for (int i = 0; i < size; i++) {
            notifyListener0(this, a[i]);
        }
    }
    private void notifyListener0(Future future, GenericFutureListener listener) {
        try {
            listener.operationComplete(future);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    private void safeExecute(EventExecutor executor, Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * 继承自 Promise，添加回调
     * @param listener
     * @return
     */
    @Override
    public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
        synchronized (this) {
            addListener0(listener);
        }
        if (isDone()) {
            notifyListeners();
        }
        return this;
    }

    private void addListener0(GenericFutureListener<? extends Future<? super V>> listener) {
        if (listeners == null) {
            listeners = listener;
        } else if (listeners instanceof  DefaultFutureListeners) {
            ((DefaultFutureListeners) listeners).add(listener);
        } else {
            listeners = new DefaultFutureListeners((GenericFutureListener<?>) listeners, listener);
        }
    }
    /**
     * 继承自 Promise，添加一组回调
     * @param listeners
     * @return
     */
    @Override
    public Promise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners) {
        synchronized (this) {
            for (GenericFutureListener<? extends Future<? super V>> listener : listeners) {
                addListener0(listener);
            }
        }
        if (isDone()) {
            notifyListeners();
        }
        return this;
    }
    @Override
    public Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener) {

        synchronized (this) {
            //移除监听器
            removeListener0(listener);
        }

        return this;
    }
    @Override
    public Promise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners) {

        synchronized (this) {
            for (GenericFutureListener<? extends Future<? super V>> listener : listeners) {
                if (listener == null) {
                    break;
                }
                removeListener0(listener);
            }
        }

        return this;
    }
    private void removeListener0(GenericFutureListener<? extends Future<? super V>> listener) {
        //如果监听器是数组类型的，就从数组中删除
        if (listeners instanceof DefaultFutureListeners) {
            ((DefaultFutureListeners) listeners).remove(listener);
        } else if (listeners == listener) {
            //如果只有一个监听器，则直接把监听器属性置为null
            listeners = null;
        }
    }

    public EventExecutor executor() {
        return executor;
    }
    private void incWaiters() {
        if (waiters == Short.MAX_VALUE) {
            throw new IllegalStateException("too many waiters: " + this);
        }
        ++waiters;
    }

    /**
     * 继承自 Future，获取任务执行过程中的异常
     * @return
     */
    @Override
    public Throwable cause() {
        Object result = this.result;
        return (result instanceof CauseHolder) ? ((CauseHolder) result).cause : null;
    }

    private void decWaiters() {
        --waiters;
    }
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    protected StringBuilder toStringBuilder() {
        StringBuilder buf = new StringBuilder(64)
                .append(StringUtil.simpleClassName(this))
                .append('@')
                .append(Integer.toHexString(hashCode()));

        Object result = this.result;
        if (result == SUCCESS) {
            buf.append("(success)");
        } else if (result == UNCANCELLABLE) {
            buf.append("(uncancellable)");
        } else if (result instanceof CauseHolder) {
            buf.append("(failure: ")
                    .append(((CauseHolder) result).cause)
                    .append(')');
        } else if (result != null) {
            buf.append("(success: ")
                    .append(result)
                    .append(')');
        } else {
            buf.append("(incomplete)");
        }

        return buf;
    }
    /**
     * 持有异常的静态内部类
     */
    private static final class CauseHolder {
        final Throwable cause;

        public CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }
}
