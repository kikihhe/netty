package NIO;


import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @ClassName TestFileChannel
 * @Description 使用FileChannel
 * @Author 何
 * @Date 2023-06-24 17:25
 * @Version 1.0
 */
public class TestFileChannel {
    public static void main(String[] args) throws Exception {
        FileChannel fileChannel = new RandomAccessFile("NIO测试.txt", "rw").getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (fileChannel.read(byteBuffer) != -1) {
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                char b = (char) byteBuffer.get();
                if (b == ' ') {
                    continue;
                }
                System.out.print(b);
            }
            byteBuffer.clear();
        }
        fileChannel.close();
    }
}
