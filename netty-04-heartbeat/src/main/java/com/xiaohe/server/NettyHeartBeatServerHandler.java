package com.xiaohe.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @ClassName NettyHeartBeatServerHandler
 * @Description TODO
 * @Author 何
 * @Date 2023-06-29 17:17
 * @Version 1.0
 */
public class NettyHeartBeatServerHandler extends SimpleChannelInboundHandler<String> {
    private int readIdleTimes = 0;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        readIdleTimes = 0;
        System.out.println("服务端收到的心跳: " + msg);
    }

    /**
     * 超时的业务处理
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        switch (event.state()) {
            case READER_IDLE:
                readIdleTimes++;
                System.out.println("读超时次数: " + readIdleTimes);
                break;
            case WRITER_IDLE:
                System.out.println("写超时");
                break;
            case ALL_IDLE:
                System.out.println("读写超时");
                break;
        }
        if (readIdleTimes > 3) {
            System.out.println("读超时次数大于3");
            ctx.writeAndFlush("您已超时");
            ctx.close();
        }
    }
}
