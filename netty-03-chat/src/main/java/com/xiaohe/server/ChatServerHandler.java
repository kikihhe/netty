package com.xiaohe.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName ChatServerHandler
 * @Description 处理器
 * @Author 何
 * @Date 2023-06-29 10:51
 * @Version 1.0
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 记录所有channel
     * GlobalEventExecutor.INSTANCE 是一个功能线程，1s内没任务就销毁
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 客户端上线
     * @param ctx channel 上下文
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 得到该客户端的channel
        Channel channel = ctx.channel();
        // 得到所有客户端的channel
        String advice = channel.remoteAddress() + "于" + simpleDateFormat.format(new Date()) + "上线了";
        System.out.println(advice);
        // 发送上线提醒
        channelGroup.writeAndFlush(advice);
        // 将本channel加入到channel组中。
        channelGroup.add(channel);
    }

    /**
     * 处理事件
     * @param ctx           the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                      belongs to
     * @param msg           the message to handle
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        Channel nowChannel = ctx.channel();
        channelGroup.forEach(channel -> {
            if (!nowChannel.equals(channel)) {
                channel.writeAndFlush(message);
            }
        });


    }

    /**
     * 客户端下线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String message = channel.remoteAddress() + "于" + simpleDateFormat.format(new Date()) + "下线了";
        channelGroup.writeAndFlush(message);
    }

}
