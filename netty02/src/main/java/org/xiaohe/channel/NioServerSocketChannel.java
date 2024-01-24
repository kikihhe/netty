package org.xiaohe.channel;

import org.xiaohe.util.internal.SocketUtils;
import org.xiaohe.util.loop.NioEventLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 01:06
 */
public class NioServerSocketChannel extends AbstractNioMessageChannel {
    private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();

    private static ServerSocketChannel newSocket(SelectorProvider provider) {
        try {
            return provider.openServerSocketChannel();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    public NioServerSocketChannel() {
        this(newSocket(DEFAULT_SELECTOR_PROVIDER));
    }
    public NioServerSocketChannel(ServerSocketChannel channel) {
        //创建的为 NioServerSocketChannel 时，没有父类channel，SelectionKey.OP_ACCEPT是服务端channel的关注事件
        super(null, channel, SelectionKey.OP_ACCEPT);
    }

    @Override
    public ServerSocketChannel javaChannel() {
        return (ServerSocketChannel) (super.javaChannel());
    }


    @Override
    public boolean isActive() {
        if (!isOpen()) {
            return false;
        }
        if (!javaChannel().socket().isBound()) {
            return false;
        }
        return true;
    }

    @Override
    public NioEventLoop eventLoop() {
        return (NioEventLoop) super.eventLoop();
    }

    /**
     * 这里做空实现即可，服务端的channel并不会做连接动作
     */
    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        ServerSocketChannel serverSocketChannel = javaChannel();
        serverSocketChannel.bind(localAddress, 128);
        if (isActive()) {
            System.out.println("服务端绑定端口成功!");
            // 绑定成功后对 SelectionKey.OP_ACCEPT 感兴趣
            doBeginRead();
        }
    }
    protected void doClose() throws Exception {
        javaChannel().close();
    }

    /**
     * 当有 连接事件到来时调用此方法，接收连接，创建 SocketChannel
     * @param buf
     * @return
     * @throws Exception
     */
    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel socketChannel = SocketUtils.accept(javaChannel());
        try {
            if (socketChannel != null) {
                buf.add(new NioSocketChannel(this, socketChannel));
                return 1;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            try {
                socketChannel.close();
            } catch (Throwable t1) {
                throw new RuntimeException();
            }
        }
        return 0;
    }
}
