package org.xiaohe.channel;

import org.xiaohe.channel.id.ChannelId;
import org.xiaohe.util.internal.SocketUtils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 01:18
 */
public class NioSocketChannel extends AbstractNioByteChannel {
    private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
    private static SocketChannel newSocket(SelectorProvider provider) {
        try {
            return provider.openSocketChannel();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open a socket.", e);
        }
    }

    public NioSocketChannel() {
        this(DEFAULT_SELECTOR_PROVIDER);
    }

    public NioSocketChannel(SelectorProvider provider) {
        this(newSocket(provider));
    }

    public NioSocketChannel(SocketChannel socket) {
        this(null, socket);
    }

    public NioSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    @Override
    public SocketChannel javaChannel() {
        return (SocketChannel) super.javaChannel();
    }



    @Override
    public boolean isActive() {
        // channel是否为Connected状态，是客户端channel判断是否激活的条件。
        SocketChannel ch = javaChannel();
        return ch.isOpen() && ch.isConnected();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        doBind0(localAddress);
    }
    private void doBind0(SocketAddress localAddress) throws Exception {
        SocketUtils.bind(javaChannel(), localAddress);
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        // 如果连接的时候把本地地址也传入，就要在与远端创建连接时，监听本地端口号
        if (localAddress != null) {
            doBind0(localAddress);
        }
        boolean success = false;
        try {
            // 如果连接返回成功，那就是建立连接成功
            // 如果返回 false，不一定就是失败，有可能 ack 还在路上，所以要监听 CONNECT 事件。
            boolean connect = SocketUtils.connect(javaChannel(), remoteAddress);
            if (!connect) {
                selectionKey().interestOps(SelectionKey.OP_CONNECT);
            }
            success = true;
            return connect;
        } finally {
            if (!success) {
                doClose();
            }
        }
    }
    protected void doClose() throws Exception {
        javaChannel().close();
    }
    @Override
    protected int doReadBytes(ByteBuffer byteBuf) throws Exception {
        int len = javaChannel().read(byteBuf);
        byte[] buffer = new byte[len];
        byteBuf.flip();
        byteBuf.get(buffer);
        System.out.println("客户端收到消息:{}"+new String(buffer));
        //返回读取到的字节长度
        return len;
    }
}
