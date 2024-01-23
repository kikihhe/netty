package org.xiaohe.channel;

import org.xiaohe.channel.id.ChannelId;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author : 小何
 * @Description : 按照字节读, 将读到的ByteBuffer交由子类
 * @date : 2024-01-24 00:51
 */
public abstract class AbstractNioByteChannel extends AbstractNioChannel {
    public AbstractNioByteChannel(Channel parent, SelectableChannel ch) {
        super(parent, ch, SelectionKey.OP_READ);
    }

    @Override
    protected void read() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            doReadBytes(byteBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract int doReadBytes(ByteBuffer buf) throws Exception;
}
