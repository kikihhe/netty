package org.xiaohe.util.loop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaohe.channel.Channel;
import org.xiaohe.channel.ChannelFuture;
import org.xiaohe.channel.ChannelPromise;
import org.xiaohe.channel.DefaultChannelPromise;
import org.xiaohe.util.RejectedExecutionHandler;
import org.xiaohe.util.executor.SingleThreadEventExecutor;

import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 14:49
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventLoop.class);

    public static final int DEFAULT_MAX_PENDING_TASKS = Integer.MAX_VALUE;
    public SingleThreadEventLoop(EventLoopGroup parent,
                                 Executor executor,
                                 boolean addTaskWakesUp,
                                 Queue<Runnable> taskQueue,
                                 Queue<Runnable> tailTaskQueue,
                                 RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, addTaskWakesUp, taskQueue, rejectedExecutionHandler);
    }

    @Override
    public EventLoopGroup parent() {
        return null;
    }

    @Override
    public EventLoop next() {
        return this;
    }

    @Override
    protected boolean hasTasks() {
        return super.hasTasks();
    }


    public ChannelFuture register(Channel channel) {
        return register(new DefaultChannelPromise(channel, this));
    }

    public ChannelFuture register(final ChannelPromise promise) {
        promise.channel().register(this, promise);
        return promise;
    }
}
