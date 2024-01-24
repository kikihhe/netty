package org.xiaohe.util.executor;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaohe.util.RejectedExecutionHandler;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 14:13
 */
public abstract class SingleThreadEventExecutor implements EventExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventExecutor.class);
    /**
     * 未启动
     */
    private static final int ST_NOT_STARTED = 1;
    /**
     * 已启动
     */
    private static final int ST_STARTED = 2;

    private volatile int state = ST_NOT_STARTED;

    private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "state");

    /**
     * 任务队列的最大容量，默认Integer最大值
     */
    private static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Integer.MAX_VALUE;

    protected final Queue<Runnable> taskQueue;

    /**
     * 工作线程
     */
    private volatile Thread thread;

    private Executor executor;
    private EventExecutorGroup parent;
    private boolean addTaskWakeUp;
    private volatile boolean interrupted;

    private final RejectedExecutionHandler rejectedExecutionHandler;

    public SingleThreadEventExecutor(EventExecutorGroup parent,
                                     Executor executor,
                                     boolean addTaskWakeUp,
                                     Queue<Runnable> taskQueue,
                                     RejectedExecutionHandler rejectedHandler) {
        this.parent = parent;
        this.addTaskWakeUp = addTaskWakeUp;
        this.taskQueue = taskQueue;
        rejectedExecutionHandler = rejectedHandler;
    }

    public Queue<Runnable> newTaskQueue(int maxPendingTasks) {
        return new LinkedBlockingQueue<>(maxPendingTasks);
    }
    /**
     * 单线程执行器要执行的方法
     */
    protected abstract void run();

    @Override
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        // 将任务提交到任务队列中
        addTask(task);
        // 启动单线程执行器中的线程
        startThread();
    }


    private void addTask(Runnable task) {
        // 如果添加失败，执行拒绝策略
        if (!taskQueue.offer(task)) {
            rejectedExecutionHandler.rejected(task, this);
        }
    }


    private void startThread() {
        if (state == ST_NOT_STARTED) {
            if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
                boolean success = false;
                try {
                    doStartThread();
                    success = true;
                } finally {
                    if (!success) {
                        STATE_UPDATER.compareAndSet(this, ST_STARTED, ST_NOT_STARTED);
                    }
                }
            }
        }
    }

    private void doStartThread() {
        executor.execute(() -> {
            thread = Thread.currentThread();
            if (interrupted) {
                thread.interrupt();
            }
            // 线程开始轮询处理IO事件，这里调用的是 NioEventLoop 中的run方法
            SingleThreadEventExecutor.this.run();
            logger.info("单线程执行器错误结束了");
        });
    }

    protected boolean hasTasks() {
        return !taskQueue.isEmpty();
    }
    @Override
    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }

    protected void interruptThread() {
        Thread currentThread = thread;
        if (currentThread == null) {
            interrupted = true;
        } else {
            //中断线程并不是直接让该线程停止运行，而是提供一个中断信号
            //也就是标记，想要停止线程仍需要在运行流程中结合中断标记来判断
            currentThread.interrupt();
        }
    }

    @Override
    public void shutdownGracefully() {

    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void awaitTermination(Integer integer, TimeUnit timeUnit) throws InterruptedException{

    }

    public Queue<Runnable> getTaskQueue() {
        return taskQueue;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

}
