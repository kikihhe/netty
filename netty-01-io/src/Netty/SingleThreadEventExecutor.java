package Netty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-10 11:55
 */
public abstract class SingleThreadEventExecutor implements Executor {
    /**
     * 任务队列的容量，默认是Integer的最大值
     */
    protected static final int DEFAULT_MAX_PENDING_TASKS = Integer.MAX_VALUE;

    /**
     * 任务队列，里面装Worker
     */
    private final Queue<Runnable> taskQueue;

    /**
     * 拒绝策略
     */
    private final RejectedExecutionHandler rejectedExecutionHandler;

    private volatile boolean start = false;



    private Thread thread;

    public SingleThreadEventExecutor() {
        this.taskQueue = newTaskQueue(DEFAULT_MAX_PENDING_TASKS);
        this.rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
    }

    /**
     * 创建任务队列，protected可以被子类访问/重写
     *
     * @param maxPendingTasks
     */
    protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
        return new LinkedBlockingQueue<>(maxPendingTasks);
    }




    @Override
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task is null");
        }
        // 把任务放到队列中
        addTask(task);
        // 启动单线程执行器中的线程
        startThread();
    }

    private void addTask(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task is null");
        }
        // 如果放入队列失败，执行拒绝策略
        if (!offerTask(task)) {
            reject(task);
        }
    }

    final boolean offerTask(Runnable task) {
        return taskQueue.offer(task);
    }

    protected final void reject(Runnable task) {
        // rejectedExecutionHandler.rejectedExecution(task, threadPoolExecutor);
    }

    private void startThread() {
        // 如果已经启动，不必再启动
        if (start) {
            return;
        }
        start = true;
        new Thread(() -> {
            // thread等于这个新new的线程
           thread = Thread.currentThread();
           SingleThreadEventExecutor.this.run();
        }).start();
    }
    public abstract void run();


    protected boolean hasTasks() {
        return !taskQueue.isEmpty();
    }


    protected void runAllTasks() {
        runAllTasksFrom(taskQueue);
    }
    protected void runAllTasksFrom(Queue<Runnable> taskQueue) {
        Runnable task = pollTaskFrom(taskQueue);
        if (task == null) {
            return;
        }
        for (;;) {
            // 把这个任务执行了，再取下一个任务
            safeExecute(task);
            task = pollTaskFrom(taskQueue);
            if (task == null) {
                return;
            }
        }
    }
    private void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            // 打印信息
        }
    }
    protected static Runnable pollTaskFrom(Queue<Runnable> taskQueue) {
        return taskQueue.poll();
    }

    /**
     * 判断当前执行任务的线程是否是执行器的线程
     * @param thread
     * @return
     */
    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }


}
