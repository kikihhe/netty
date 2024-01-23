package org.xiaohe.channel;

import org.xiaohe.util.executor.EventExecutorGroup;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-23 22:10
 */
public interface EventLoopGroup extends EventExecutorGroup {
    EventLoop next();

    ChannelFuture register(Channel channel);

    ChannelFuture regsiter(ChannelPromise promise);
}
