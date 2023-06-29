package com.xiaohe.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName NettyServer
 * @Description TODO
 * @Author 何
 * @Date 2023-06-29 17:10
 * @Version 1.0
 */
public class NettyServer {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(10);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringEncoder()).addLast(new StringDecoder());
                            // 心跳检测, IdleStateHandler发现3s没发生读操作时会触发超时事件，创建出IdleStateEvent对象，并将该对象交给下一个handler
                            pipeline.addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new NettyHeartBeatServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(9090).sync();
            System.out.println("服务端启动了");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
