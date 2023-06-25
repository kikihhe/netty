package NIO;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

/**
 * @ClassName TestDatagramChannel
 * @Description 基于UDP的IO模式
 * @Author 何
 * @Date 2023-06-24 21:09
 * @Version 1.0
 */
public class TestDatagramChannel {
    public static void main(String[] args) throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        DatagramSocket datagramSocket = datagramChannel.socket();
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        datagramChannel.receive(byteBuffer);


    }
}
