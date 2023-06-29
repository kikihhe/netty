package com.xiaohe.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @ClassName NettyReConnectClientHandler
 * @Description
 * @Author 何
 * @Date 2023-06-29 20:26
 * @Version 1.0
 */
public class NettyReConnectClientHandler extends SimpleChannelInboundHandler<String> {
    private NettyClient nettyClient;

    public NettyReConnectClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    /**
     * 断线后需要执行的方法
     * 需要重新连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务器断线重连中...");
        nettyClient.connect();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("接收到信息: " + msg);
    }
}
