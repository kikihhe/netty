package org.xiaohe.util.loop;

import org.xiaohe.channel.Channel;
import org.xiaohe.channel.ChannelFuture;
import org.xiaohe.channel.ChannelPromise;
import org.xiaohe.util.executor.EventExecutorGroup;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-23 22:10
 */
public interface EventLoopGroup extends EventExecutorGroup {
    EventLoop next();

    ChannelFuture register(Channel channel);

    public ChannelFuture register(ChannelPromise promise);
}
