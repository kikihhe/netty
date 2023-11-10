package Netty;

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
 * @date : 2023-11-10 14:05
 */
public class NioEventLoop extends SingleThreadEventLoop {

    private final SelectorProvider provider;

    private Selector selector;

    public NioEventLoop() {
        // 通过SelectorProvider可以获得 ServerSocketChannel 和 SocketChannel
        this.provider = SelectorProvider.provider();
        this.selector = openSelector();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 没有事件就阻塞在这里
                select();
                // 如果走到这里就说明selector没有阻塞了
                processSelectedKeys(selector.selectedKeys());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 执行单线程执行器的所有任务
                runAllTasks();
            }
        }
    }
    /**
     * 得到用于多路复用的selector
     * @return
     */
    private Selector openSelector() {
        try {
            selector = provider.openSelector();
            return selector;
        } catch (IOException e) {
            throw new RuntimeException("failed to open a new selector", e);
        }
    }

    private void select() throws IOException {
        Selector selector = this.selector;
        for (;;) {
            // 每次等待连接3s，没等到重新循环等
            int selectedKeys = selector.select(3000);
            // 如果有了连接或者有了任务就退出执行processSelectedKeys方法
            if (selectedKeys != 0 || hasTasks()) {
                break;
            }
        }
    }

    /**
     * 处理所有事件
     * @param selectedKeys
     */
    private void processSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
        if (selectedKeys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> i = selectedKeys.iterator();
        for (;;) {
            final SelectionKey k = i.next();
            i.remove();
            processSelectedKey(k);
            if (!i.hasNext()) {
                break;
            }
        }
    }

    /**
     * 处理单个事件
     * @param k
     */
    private void processSelectedKey(SelectionKey k) throws IOException {
        // 如果是读事件
        if (k.isReadable()) {
            SocketChannel channel = (SocketChannel) k.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int len = channel.read(byteBuffer);
            if (len == -1) {
                channel.close();
                return;
            }
            byte[] bytes = new byte[len];
            byteBuffer.flip();
            byteBuffer.get(bytes);
            System.out.println("新线程接收到客户端发送的数据: " + new String(bytes));

        }
    }
}
