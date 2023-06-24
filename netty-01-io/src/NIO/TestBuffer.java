package NIO;

import java.nio.*;

/**
 * @ClassName TestBuffer
 * @Description NIO的使用
 * @Author 何
 * @Date 2023-06-23 22:36
 * @Version 1.0
 */
public class TestBuffer {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);


        byteBuffer.put((byte)5);
        byteBuffer.put((byte)7);
        byteBuffer.put((byte)2);
        byteBuffer.put((byte)6);
        byteBuffer.put((byte)4);


        // 将limit = position
        // 将position = 0
        byteBuffer.flip();

        while (byteBuffer.hasRemaining()) {
            System.out.println(byteBuffer.get());
        }



    }
}
