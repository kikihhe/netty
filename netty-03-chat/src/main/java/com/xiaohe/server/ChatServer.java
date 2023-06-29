package com.xiaohe.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @ClassName ChatServer
 * @Description 聊天服务端
 * @Author 何
 * @Date 2023-06-29 10:42
 * @Version 1.0
 */
public class ChatServer {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 配置参数
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加编码器、解码器
                            pipeline.addLast(new StringEncoder()).addLast(new StringDecoder());

                            pipeline.addLast(new ChatServerHandler());
                        }
                    });

            // 同步关闭、同步启动
            ChannelFuture channelFuture = serverBootstrap.bind(9090).sync();
            System.out.println("聊天室启动成功");
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
