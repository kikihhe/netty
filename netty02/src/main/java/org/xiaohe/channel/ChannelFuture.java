package org.xiaohe.channel;

import org.xiaohe.util.concurrent.future.Future;
import org.xiaohe.util.concurrent.listener.GenericFutureListener;

/**
 * @author : 小何
 * @Description : 本质上是一个异步任务
 * @date : 2024-01-23 22:13
 */
public interface ChannelFuture extends Future<Void> {
    public Channel channel();

    @Override
    public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);
    @Override
    public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listener);
    @Override
    public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);
    @Override
    public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    public ChannelFuture sync() throws InterruptedException;

    @Override
    public ChannelFuture syncUninterruptibly();

    @Override
    public ChannelFuture await() throws InterruptedException;

    @Override
    public ChannelFuture awaitUninterruptibly();

    public boolean isVoid();
}
