package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;


public class TestSelector {
    public static void main(String[] args) throws IOException {
        // 获取连接
        Selector selector = Selector.open();
        // 获取服务端
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 服务端监听8989端口
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8989));
        // 与 Selector 一起使用时，Channel 必须处于非阻塞模式下
        serverSocketChannel.configureBlocking(false);
        // 将服务端绑定到选择器上，感兴趣的事件为 连接。
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端就绪");
        while (true) {
            // 每1s选择一次，查看是否有就绪连接
            int select = selector.select(1000);
            if (select == 0) {
                continue;
            }
            // 获取这1s内的所有就绪连接，遍历
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                // 如果选择键接收事件就绪
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 处理socket逻辑

                } else if (selectionKey.isConnectable()) {
                    // 如果选择键连接事件就绪
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    // 处理SocketChannel事件

                } else if (selectionKey.isReadable()) {
                    // 如果选择键读事件就绪
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    channel.read(byteBuffer);
                    // 从buffer中读取数据
                    // ......

                } else if (selectionKey.isWritable()) {
                    // 如果选择键写事件就绪

                }
                iterator.remove();
            }
        }

    }
}
