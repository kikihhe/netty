package Netty;

import java.nio.channels.SocketChannel;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-10 18:12
 */
public interface EventLoopGroup extends EventExecutorGroup {
    @Override
    EventExecutor next();

    void register(SocketChannel socketChannel, NioEventLoop nioEventLoop);
}
