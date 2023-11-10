package com.xiaohe.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;

import java.net.InetSocketAddress;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-07 00:44
 */
public class NettyServer {
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup workGroup = new NioEventLoopGroup();

    public void serverStart() throws InterruptedException {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    // 指定通信方式
                    .channel(NioServerSocketChannel.class)
                    // 绑定端口
                    .localAddress(new InetSocketAddress(11111))

                    // 服务端可连接队列数，对应TCP/IP协议listen函数中backlog参数
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new NettyServerTestHandler());
                            // 自定义编码器
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 2, 4, -16, 16));
                        }
                    });
            ChannelFuture future = bootstrap.bind().sync();

            if (future.isSuccess()) {
                System.out.println("Netty Server 启动成功");
            }
            // 阻塞服务端
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyServer().serverStart();
    }
}
