package org.xiaohe.channel;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 00:57
 */
public abstract class AbstractNioMessageChannel extends AbstractNioChannel {
    /**
     * 停止接收来自任何客户端的数据
     */
    boolean inputShutdown;

    /**
     * 存放与此服务端连接的所有客户端
     */
    private final List<Object> readBuf = new ArrayList<>();
    public AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
        super(parent, ch, readInterestOp);
    }
    protected abstract int doReadMessages(List<Object> buf) throws Exception;

    /**
     * 是否开始接收数据，就是将此 Channel 对读事件感兴趣
     * @throws Exception
     */
    @Override
    protected void doBeginRead() throws Exception {
        if (inputShutdown) {
            return;
        }
        super.doBeginRead();
    }


    /**
     * 虽然名字叫 read，但是在 ServerSocketChannel 中并不是读取数据，而是创建客户端连接并放进集合中
     */
    @Override
    protected void read() {
        assert eventLoop().inEventLoop(Thread.currentThread());
        boolean closed = false;
        Throwable exception = null;
        try {
            do {
                // 接受并创建客户端的连接，存放在集合中
                int localRead = doReadMessages(readBuf);
                // 为0表示现在没有连接
                if (localRead == 0) {
                    break;
                }
            } while (true);
        } catch (Throwable e) {
            exception = e;
        }
        int size = readBuf.size();
        for (int i = 0; i < size; i++) {
            readPending = false;
            Channel child = (Channel) readBuf.get(i);
            System.out.println("收到客户端的Channel了");
            // TODO
        }
        readBuf.clear();
        if (exception != null) {
            throw new RuntimeException(exception);
        }
    }
}
