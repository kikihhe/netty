package com.xiaohe.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @ClassName ChatClientHandler
 * @Description 客户端处理消息
 * @Author 何
 * @Date 2023-06-29 12:17
 * @Version 1.0
 */
public class ChatClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
    }
}
