package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @ClassName TestServerSocketChannel
 * @Description SocketChannel
 * @Author 何
 * @Date 2023-06-24 19:48
 * @Version 1.0
 */
public class TestServerSocketChannel {
    public static final String GREETING = "Hello java nio.\r\n";
    public static void main(String[] args) throws IOException {
        // 创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 获取内部的socket
        ServerSocket socket = serverSocketChannel.socket();
        // 让socket监听8989端口
        socket.bind(new InetSocketAddress(8989));
        // 设置serverSocketChannel为非阻塞
        serverSocketChannel.configureBlocking(false);

        while (true) {
            // 连接socketChannel
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel == null) {
                System.out.println("socketChannel为空");
                continue;
            }
            System.out.println("有连接进来了");
        }

    }
}
