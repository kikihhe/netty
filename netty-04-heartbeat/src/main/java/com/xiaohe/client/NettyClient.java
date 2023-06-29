package com.xiaohe.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * @ClassName NettyClient
 * @Description 客户端: 心跳连接
 * @Author 何
 * @Date 2023-06-29 16:39
 * @Version 1.0
 */
public class NettyClient {
    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {

            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder()).addLast(new StringEncoder());
                            pipeline.addLast(new NettyHeartBeatClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress("127.0.0.1", 9090)).sync();
            Random random = new Random();
            String heartbeat = "heart beat";
            Channel channel = channelFuture.channel();
            while (true) {
                int num = random.nextInt(15);
                System.out.println("隔了" + num + "s发送心跳");
                Thread.sleep(num * 1000);
                channel.writeAndFlush(heartbeat);
            }


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }


    }
}
