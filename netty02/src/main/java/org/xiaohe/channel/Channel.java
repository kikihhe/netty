package org.xiaohe.channel;

import org.xiaohe.channel.id.ChannelId;

import java.net.SocketAddress;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-23 22:12
 */
public interface Channel {
    ChannelId id();

    EventLoop eventLoop();


    Channel parent();


    ChannelConfig config();


    boolean isOpen();


    boolean isRegistered();


    boolean isActive();


    SocketAddress localAddress();


    SocketAddress remoteAddress();


    ChannelFuture closeFuture();

    /**
     * 该方法并不在此接口，而是在ChannelOutboundInvoker接口，现在先放在这里
     */
    ChannelFuture close();

    /**
     * 该方法并不在此接口，而是在ChannelOutboundInvoker接口，现在先放在这里
     */
    void bind(SocketAddress localAddress, ChannelPromise promise);

    /**
     * 该方法并不在此接口，而是在ChannelOutboundInvoker接口，现在先放在这里
     */
    void connect(SocketAddress remoteAddress, final SocketAddress localAddress,ChannelPromise promise);

    /**
     * 该方法并不在此接口，而是在unsafe接口，现在先放在这里
     */
    void register(EventLoop eventLoop, ChannelPromise promise);

    /**
     * 该方法并不在此接口，而是在unsafe接口，现在先放在这里
     */
    void beginRead();
}
