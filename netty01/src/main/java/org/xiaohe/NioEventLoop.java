package org.xiaohe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-22 17:54
 */
public class NioEventLoop extends SingleThreadEventLoop {
    private static final Logger logger = LoggerFactory.getLogger(NioEventLoop.class);
    private final SelectorProvider provider;
    private final Selector selector;

    public NioEventLoop() {
        this.provider = SelectorProvider.provider();
        this.selector = openSelector();
    }
    private Selector openSelector() {
        try {
            return provider.openSelector();
        } catch (IOException e) {
            throw new RuntimeException("failed to open a new selector", e);
        }
    }
    public void select() throws IOException {
        Selector selector = this.selector;
        while (true) {
            int select = selector.select(3000);
            if (select != 0 || hasTasks()) {
                break;
            }
        }
    }

    /**
     * 处理事件/任务
     * @param selectionKeys 本次的所有事件
     */
    private void processSelectedKeys(Set<SelectionKey> selectionKeys) throws IOException {
        if (selectionKeys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            processSelectedKey(selectionKey);
            iterator.remove();
        }
    }
    private void processSelectedKey(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.slice();
            int length = socketChannel.read(buffer);
            if (length == -1) {
                logger.info("客户端要关闭");
                socketChannel.close();
                return;
            }
            buffer.flip();
            byte[] bytes = new byte[length];
            buffer.get(bytes);
            logger.info("线程接收到的数据: {}", new String(bytes));
        }
    }
    public void run() {
        while (true) {
            try {
                // 如果没有事件或者没有任务，线程就阻塞在 select 方法中
                select();
                // 走出 select() 方法说明有事件或任务了，处理事件/任务
                processSelectedKeys(selector.selectedKeys());

            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                // 将所有任务执行完毕
                runAllTasks();
            }
        }
    }
    public Selector selector() {
        return this.selector;
    }
}
