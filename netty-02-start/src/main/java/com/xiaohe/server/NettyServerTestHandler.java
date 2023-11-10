package com.xiaohe.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.SocketAddress;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-07 00:51
 */
public class NettyServerTestHandler extends ChannelInboundHandlerAdapter {
    /**
     * 接收到消息会执行
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("接收到的msg: " + byteBuf.toString(CharsetUtil.UTF_8));
    }

    /**
     * 连接成功调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        System.out.println("客户端: " + socketAddress + "已连接");
    }

    /**
     * 断开连接调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        System.out.println("客户端: " + socketAddress + "断开连接");
    }

    /**
     * 读取信息完成事件  信息读取完成后调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

    }

    /**
     * 异常处理  发生异常调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 异常后 关闭与客户端连接
        ctx.close();
    }
}
