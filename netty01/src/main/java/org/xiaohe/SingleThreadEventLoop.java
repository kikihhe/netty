package org.xiaohe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author : 小何
 * @Description : 将 SocketChannel 注册到 NioEventLoop 的 selector 上
 * @date : 2024-01-22 17:52
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventLoop.class);


    @Override
    public void register(SocketChannel socketChannel, NioEventLoop nioEventLoop) {
        nioEventLoop.execute(() -> {
            try {
                socketChannel.configureBlocking(false);
                socketChannel.register(nioEventLoop.selector(), SelectionKey.OP_READ);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void register0(SocketChannel socketChannel, NioEventLoop nioEventLoop) {

    }

    @Override
    public EventLoop next() {
        return this;
    }
}
