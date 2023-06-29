package com.xiaohe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @ClassName TestByteBuf
 * @Description 使用ByteBuf
 * @Author 何
 * @Date 2023-06-28 16:42
 * @Version 1.0
 */
public class TestByteBuf {
    public static void main(String[] args) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte((byte)1);
        byteBuf.writeByte((byte)2);
        byteBuf.writeByte((byte)3);

        byteBuf.readByte();
        byteBuf.readByte();


        byteBuf.writeByte((byte)4);
        byteBuf.writeByte((byte)5);

    }
}
