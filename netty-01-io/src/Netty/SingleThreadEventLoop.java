package Netty;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-10 14:02
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor {
    public SingleThreadEventLoop() {

    }

    public void register(SocketChannel socketChannel, NioEventLoop nioEventLoop) {
        if (inEventLoop(Thread.currentThread())) {
            register0(socketChannel, nioEventLoop);
        } else {
            this.execute(() -> {
                register0(socketChannel);
            });
        }
    }

    private void register0(SocketChannel channel, NioEventLoop nioEventLoop) {
        try {
            channel.configureBlocking(false);
            channel.register(nioEventLoop.selector(), SelectionKey.OP_READ);
        } catch (Exception e) {

        }
    }
}
