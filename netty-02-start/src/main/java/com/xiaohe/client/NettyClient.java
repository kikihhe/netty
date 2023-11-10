package com.xiaohe.client;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-07 01:00
 */
public class NettyClient {
    private EventLoopGroup group = new NioEventLoopGroup();
    // NettyServer 的端口
    private int serverPort = 11111;

    private String host = "127.0.0.1";

    public void start() {
        try {
            Bootstrap bootstrap = new io.netty.bootstrap.Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, serverPort)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new NettyClientTestHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect();
            // 客户端断线重连
            future.addListener((ChannelFutureListener)future1 -> {
               if (future1.isSuccess()) {
                   System.out.println("连接Netty服务器成功");
               } else {
                   System.out.println("连接失败，进行断线重连...");
                   future1.channel().eventLoop().schedule(() -> {
                       start();
                   }, 20, TimeUnit.SECONDS);
               }
            });
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyClient().start();
    }
}
