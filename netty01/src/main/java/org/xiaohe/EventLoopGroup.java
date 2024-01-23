package org.xiaohe;

import java.nio.channels.SocketChannel;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-22 22:55
 */
public interface EventLoopGroup {
    void register(SocketChannel channel, NioEventLoop nioEventLoop);
    EventLoop next();
}
