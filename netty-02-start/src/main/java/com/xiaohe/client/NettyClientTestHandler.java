package com.xiaohe.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.SocketAddress;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-07 01:10
 */
public class NettyClientTestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf msg1 = (ByteBuf) msg;
        System.out.println("接收到服务端的消息: " + msg1.toString(CharsetUtil.UTF_8));
    }
    // 连接上 就给服务端发送数据
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        SocketAddress socketAddress = ctx.channel().remoteAddress();
        System.out.println(socketAddress + " 已与服务端连接");

        for (int i = 0; i < 100; i++) {
            ctx.writeAndFlush(Unpooled.copiedBuffer("Hello Server", CharsetUtil.UTF_8));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " 已断开连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
