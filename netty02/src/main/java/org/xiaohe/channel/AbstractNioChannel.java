package org.xiaohe.channel;

import org.xiaohe.util.loop.EventLoop;
import org.xiaohe.util.loop.NioEventLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 00:34
 */
public abstract class AbstractNioChannel extends AbstractChannel {
    /**
     * 这个抽象类是 ServerSocketChannel 和 SocketChannel 的父类
     * 放在此处用于表示它俩其中的一个
     */
    private final SelectableChannel ch;

    /**
     * 此 Channel 感兴趣的事件
     */
    protected final int readInterestOp;

    /**
     * Channel 注册到 Selector 上后返回的 key
     */
    volatile SelectionKey selectionKey;
    /**
     * 是否还有未读取的数据
     */
    boolean readPending;

    public AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
        super(parent);
        this.ch = ch;
        this.readInterestOp = readInterestOp;
        // 在创建的时候就设置此 Channel 为非阻塞的 Channel
        try {
            ch.configureBlocking(false);
        } catch (IOException e) {
            try {
                ch.close();
            } catch (IOException e1) {
                throw new RuntimeException(e1.getMessage());
            }
            throw new RuntimeException("Failed to enter non-blocking mode.", e);
        }
    }

    public boolean isOpen() {
        return this.ch.isOpen();
    }

    /**
     * 返回此 Channel 关联的 Java NIO Channel
     * @return
     */
    public SelectableChannel javaChannel() {
        return this.ch;
    }

    public SelectionKey selectionKey() {
        assert selectionKey != null;
        return selectionKey;
    }

    /**
     * 此 loop 是否为 NioEventLoop
     * @param loop
     * @return
     */
    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof NioEventLoop;
    }

    /**
     * 注册，在注册的时候将 this 放里面，由于这个方法肯定是子类调用，那么 this 就是 NioSocketChannel 或者 NioServerSocketChannel
     * @throws Exception
     */
    @Override
    protected void doRegister() throws Exception {
        selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
    }

    /**
     * 给 Channel 设置对 OP_READ事件 感兴趣
     * @throws Exception
     */
    @Override
    public void doBeginRead() throws Exception {
        final SelectionKey selectionKey = this.selectionKey;
        if (!selectionKey.isValid()) {
            return;
        }
        int interestOps = selectionKey.interestOps();
        if ((interestOps & readInterestOp) == 0) {
            selectionKey.interestOps(interestOps | readInterestOp);
        }
    }

    /**
     * 建立连接, 由于不知道自己是 SocketChannel 还是 ServerSocketChannel，建立连接过程需要子类实现
     * @param remoteAddress
     * @param localAddress
     * @param promise
     */
    @Override
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        try {
            boolean b = doConnect(remoteAddress, localAddress);
            if (!b) {
                promise.trySuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception;
    public abstract void read();
}
