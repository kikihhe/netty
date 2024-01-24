package org.xiaohe.util.executor;

import org.xiaohe.util.DefaultEventExecutorChooserFactory;
import org.xiaohe.util.DefaultThreadFactory;
import org.xiaohe.util.EventExecutorChooserFactory;
import org.xiaohe.util.ThreadPerTaskExecutor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 15:29
 */
public abstract class MultithreadEventExecutorGroup extends AbstractEventExecutorGroup {
    private final EventExecutor[] children;
    private final Set<EventExecutor> readonlyChildren;
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    //private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
    private final EventExecutorChooserFactory.EventExecutorChooser chooser;
    protected MultithreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        this(nThreads, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
    }

    protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
        this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
    }

    protected MultithreadEventExecutorGroup(int nThreads, Executor executor,
                                            EventExecutorChooserFactory chooserFactory, Object... args) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }

        if (executor == null) {
            executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
        }
        // 在这里给线程组赋值，如果没有定义线程数，线程数默认就是cpu核数*2
        children = new EventExecutor[nThreads];

        for (int i = 0; i < nThreads; i ++) {
            boolean success = false;
            try {
                // 创建每一个线程执行器，这个方法在NioEventLoopGroup中实现。
                children[i] = newChild(executor, args);
                success = true;
            } catch (Exception e) {
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                if (!success) {
                    // 如果第一个线程执行器就没创建成功，剩下的方法都不会执行
                    // 如果从第二个线程执行器开始，执行器没有创建成功，那么就会关闭之前创建好的线程执行器。
                    for (int j = 0; j < i; j ++) {
                        children[j].shutdownGracefully();
                    }

                    for (int j = 0; j < i; j ++) {
                        EventExecutor e = children[j];
                        try {
                            // 判断正在关闭的执行器的状态，如果还没终止，就等待一些时间再终止
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interrupted) {
                            // 给当前线程设置一个中断标志
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
        // 执行器选择器
        chooser = chooserFactory.newChooser(children);
        Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
        Collections.addAll(childrenSet, children);
        readonlyChildren = Collections.unmodifiableSet(childrenSet);
    }

    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultThreadFactory(getClass());
    }

    @Override
    public EventExecutor next() {
        return chooser.next();
    }


    @Override
    public void shutdownGracefully() {
        for (EventExecutor l: children) {
            l.shutdownGracefully();
        }
    }

    public final int executorCount() {
        return children.length;
    }

    protected abstract EventExecutor newChild(Executor executor, Object... args) throws Exception;

}
