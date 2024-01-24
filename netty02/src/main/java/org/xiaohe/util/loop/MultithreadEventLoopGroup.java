package org.xiaohe.util.loop;

import org.xiaohe.channel.Channel;
import org.xiaohe.channel.ChannelFuture;
import org.xiaohe.channel.ChannelPromise;
import org.xiaohe.util.DefaultThreadFactory;
import org.xiaohe.util.EventExecutorChooserFactory;
import org.xiaohe.util.NettyRuntime;
import org.xiaohe.util.executor.MultithreadEventExecutorGroup;
import org.xiaohe.util.internal.SystemPropertyUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public abstract class MultithreadEventLoopGroup extends MultithreadEventExecutorGroup implements EventLoopGroup {

    private static final int DEFAULT_EVENT_LOOP_THREADS;

    //如果用户没有设定线程数量，则线程数默认使用这里的cpu核数乘2
    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

    }
    //通常情况下，这个构造器会从子类被调用
    protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
    }

    protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
    }

    protected MultithreadEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
                                        Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, chooserFactory, args);
    }

    @Override
    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultThreadFactory(getClass(), Thread.MAX_PRIORITY);
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    @Override
    protected abstract EventLoop newChild(Executor executor, Object... args) throws Exception;

    @Override
    public ChannelFuture register(Channel channel) {
        return next().register(channel);
    }

    @Override
    public ChannelFuture register(ChannelPromise promise) {
        return next().register(promise);
    }

}