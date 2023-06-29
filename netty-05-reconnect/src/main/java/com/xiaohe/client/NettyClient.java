package com.xiaohe.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName NettyClient
 * @Description TODO
 * @Author 何
 * @Date 2023-06-29 19:38
 * @Version 1.0
 */
public class NettyClient {

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    private Bootstrap bootstrap;

    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        NettyClient nettyClient = new NettyClient();
        nettyClient.setBootstrap(bootstrap);


        try {
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NettyReConnectClientHandler(nettyClient));
                        }
                    });

            nettyClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    public  void connect() {
        // 异步连接
        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress("127.0.0.1", 9090));
        // 添加监听器查看连接结果
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boolean success = future.isSuccess();
                // 连接失败就重试
                if (!success) {
                    channelFuture.channel().eventLoop().schedule(()-> {
                        System.out.println("连接失败，重试中...");
                        connect();
                    }, 1000, TimeUnit.SECONDS);

                } else {
                    System.out.println("连接成功");

                }
            }
        });

    }
}
