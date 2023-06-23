package BIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName TestBIO
 * @Description 使用Socket模拟BIO
 * @Author 何
 * @Date 2023-06-23 22:13
 * @Version 1.0
 */
public class TestBIO {
    public static void main(String[] args) throws IOException {
        // 创建一个服务端，监听8989端口
        ServerSocket serverSocket = new ServerSocket(8989);
        while (true) {
            // 如果没有客户连接，代码会一直阻塞在accept处。
            // 如果有链接，一个连接对应一个socket, 一个socket我们开一个线程去处理它
            Socket socket = serverSocket.accept();

            new Thread(() -> {
                System.out.println("有客户连接");
                try {
                    byte[] b = new byte[1024];
                    InputStream inputStream = socket.getInputStream();
                    int read = inputStream.read(b);
                    System.out.println("socket发送来的信息: " + new String(b, 0, read));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                // 处理后续业务
            });
        }
    }
}
