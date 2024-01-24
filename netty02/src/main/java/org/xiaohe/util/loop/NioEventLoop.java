package org.xiaohe.util.loop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaohe.util.EventLoopTaskQueueFactory;
import org.xiaohe.util.RejectedExecutionHandler;
import org.xiaohe.util.SelectStrategy;
import org.xiaohe.channel.AbstractNioChannel;
import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 14:55
 */
public class NioEventLoop extends SingleThreadEventLoop {
    private static final Logger logger = LoggerFactory.getLogger(NioEventLoop.class);
    private final Selector selector;

    private final SelectorProvider provider;
    private SelectStrategy selectStrategy;

    public NioEventLoop(NioEventLoopGroup parent,
                        Executor executor,
                        SelectorProvider selectorProvider,
                        SelectStrategy strategy,
                        RejectedExecutionHandler rejectedExecutionHandler,
                        EventLoopTaskQueueFactory queueFactory) {
        super(parent, executor, false, newTaskQueue(queueFactory), newTaskQueue(queueFactory), rejectedExecutionHandler);
        if (selectorProvider == null) {
            throw new NullPointerException("selectorProvider");
        }
        if (strategy == null) {
            throw new NullPointerException("selectStrategy");
        }

        provider = selectorProvider;
        selector = openSelector();
        selectStrategy = strategy;

    }
    private static Queue<Runnable> newTaskQueue(EventLoopTaskQueueFactory queueFactory) {
        if (queueFactory == null) {
            return new LinkedBlockingQueue<Runnable>(DEFAULT_MAX_PENDING_TASKS);
        }
        return queueFactory.newTaskQueue(DEFAULT_MAX_PENDING_TASKS);
    }
    private Selector openSelector() {
        final Selector unwrappedSelector;
        try {
            unwrappedSelector = provider.openSelector();
            return unwrappedSelector;
        } catch (Exception e) {
            throw new RuntimeException("failed to open a new Selector");
        }
    }

    public Selector unwrappedSelector() {
        return selector;
    }
    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    protected void run() {
        for (;;) {
            try {
                select();
                processSelectedKeys();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runAllTasks();
            }
        }
    }

    private void select() throws IOException {
        Selector selector = this.selector;
        // 这里是一个死循环
        for (;;){
            // 如果没有就绪事件，就在这里阻塞3秒
            int selectedKeys = selector.select(1000);
            // 如果有事件或者单线程执行器中有任务待执行，就退出循环
            if (selectedKeys != 0 || hasTasks()) {
                break;
            }
        }
    }
    private void processSelectedKeys() throws Exception {
        // 采用优化过后的方式处理事件, Netty默认会采用优化过的 Selector 对就绪事件处理。
        // processSelectedKeysOptimized();
        // 未优化过的处理事件方式
        processSelectedKeysPlain(selector.selectedKeys());
    }
    private void processSelectedKeysPlain(Set<SelectionKey> selectedKeys) throws Exception {
        if (selectedKeys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> i = selectedKeys.iterator();
        for (;;) {
            final SelectionKey k = i.next();
            // 通过 attachment 方法就可以得到nio类的channel
            final Object a = k.attachment();
            i.remove();
            // 处理就绪事件
            if (a instanceof AbstractNioChannel) {
                processSelectedKey(k, (AbstractNioChannel) a);
            }
            if (!i.hasNext()) {
                break;
            }
        }
    }

    private void processSelectedKey(SelectionKey k,AbstractNioChannel ch) throws Exception {
        try {
            // 得到key感兴趣的事件
            int ops = k.interestOps();
            // 如果是连接事件
            if (ops == SelectionKey.OP_CONNECT) {
                // 移除连接事件，否则会一直通知，这里实际上是做了个减法。位运算的门道，我们会放在之后和线程池的状态切换一起讲
                // 这里先了解就行
                ops &= ~SelectionKey.OP_CONNECT;
                // 重新把感兴趣的事件注册一下
                k.interestOps(ops);
                // 然后再注册客户端 channel 感兴趣的读事件
                ch.doBeginRead();
            }
            // 如果是读事件，不管是客户端还是服务端的，都可以直接调用read方法
            // NioSocketChannel和NioServerSocketChannel并不会纠缠
            // 用户创建的是哪个channel，这里抽象类调用就是它的方法
            // 是NioSocketChannel还是NioServerSocketChannel，是哪个，传入的就是哪个。只不过在这里被多态赋值给了抽象类
            // 创建的是子类对象，但在父类中调用了this，得到的仍然是子类对象
            if (ops ==  SelectionKey.OP_READ) {
                ch.read();
            }
            if (ops == SelectionKey.OP_ACCEPT) {
                ch.read();
            }
        } catch (CancelledKeyException ignored) {
            throw new RuntimeException(ignored.getMessage());
        }
    }

    protected void runAllTasks() {
        runAllTasksFrom();
    }
    protected static Runnable pollTaskFrom(Queue<Runnable> taskQueue) {
        return taskQueue.poll();
    }

    protected void runAllTasksFrom(Queue<Runnable> taskQueue) {
        //从任务对立中拉取任务,如果第一次拉取就为null，说明任务队列中没有任务，直接返回即可
        Runnable task = pollTaskFrom(taskQueue);
        if (task == null) {
            return;
        }
        for (;;) {
            //执行任务队列中的任务
            safeExecute(task);
            //执行完毕之后，拉取下一个任务，如果为null就直接返回
            task = pollTaskFrom(taskQueue);
            if (task == null) {
                return;
            }
        }
    }

}
