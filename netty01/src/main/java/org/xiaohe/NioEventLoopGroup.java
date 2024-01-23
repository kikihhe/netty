package org.xiaohe;

import java.nio.channels.SocketChannel;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-22 22:53
 */
public class NioEventLoopGroup implements EventLoopGroup {
     private NioEventLoop[] nioEventLoops;
     private int index;

    public NioEventLoopGroup(int threads) {
        nioEventLoops = new NioEventLoop[threads];
        for (int i = 0; i < threads; i++) {
            nioEventLoops[i] = new NioEventLoop();
        }
        index = 0;
    }

    @Override
    public void register(SocketChannel channel, NioEventLoop nioEventLoop) {
        next().register(channel, nioEventLoop);
    }

    @Override
    public EventLoop next() {
        int id = index % nioEventLoops.length;
        index++;
        return nioEventLoops[id];
    }
}
