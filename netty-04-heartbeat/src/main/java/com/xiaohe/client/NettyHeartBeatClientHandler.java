package com.xiaohe.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @ClassName NettyHeartBeatClientHandler
 * @Description 客户端
 * @Author 何
 * @Date 2023-06-29 17:02
 * @Version 1.0
 */
public class NettyHeartBeatClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("客户端收到的数据: " + msg);
    }
}
