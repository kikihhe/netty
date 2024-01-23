package org.xiaohe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author : 小何
 * @Description : 单线程执行器
 * @date : 2024-01-22 16:54
 */
public abstract class SingleThreadEventExecutor implements Executor, EventExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventExecutor.class);

    /**
     * 任务队列的最大容量
     */
    protected static final int DEFAULT_MAX_PENDING_TASKS = Integer.MAX_VALUE;

    /**
     * 任务队列中的任务都是 register 类型的。
     */
    private final Queue<Runnable> taskQueue;
    private final RejectedExecutionHandler rejectedExecutionHandler;



    private volatile boolean start = false;

    private Thread thread;

    public SingleThreadEventExecutor() {
        this.taskQueue = new LinkedBlockingQueue<>(DEFAULT_MAX_PENDING_TASKS);
        this.rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
    }

    private void addTask(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task is null");
        }
        if (!taskQueue.offer(task)) {
            // 使用拒绝策略去拒绝这个任务
            // rejectedExecutionHandler.rejectedExecution(task, this);
        }
    }

    @Override
    public void execute(Runnable runnable) {
        addTask(runnable);
        startThread();
    }

    private void startThread() {
        if (start) {
            return;
        }
        start = true;
        new Thread(() -> {
            thread = Thread.currentThread();
            // 执行 SingleThreadEventExecutor 的 run方法
            SingleThreadEventExecutor.this.run();
        }).start();

    }


    public boolean hasTasks() {
        return !taskQueue.isEmpty();
    }
    public void runAllTasks() {
        Runnable task = taskQueue.poll();
        if (task == null) {
            return;
        }
        for (;;) {
            safeExecute(task);
            task = taskQueue.poll();
            if (task == null) {
                return;
            }
        }
    }

    private void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger.warn("A task raised an exception. Task: {}", task, e);
        }
    }

    /**
     * 判断给定线程是否是工作线程
     * @param thread
     * @return
     */
    public boolean inEventLoop(Thread thread) {
        return this.thread == thread;
    }

    public abstract void run();

    public void shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {

    }

}
