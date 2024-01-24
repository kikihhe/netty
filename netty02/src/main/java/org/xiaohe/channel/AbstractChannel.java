package org.xiaohe.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaohe.channel.id.ChannelId;
import org.xiaohe.channel.id.DefaultChannelId;
import org.xiaohe.util.loop.EventLoop;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;

/**
 * @author : 小何
 * @Description : Channel接口的顶级实现
 * @date : 2024-01-23 23:54
 */
public abstract class AbstractChannel implements Channel {
    private static final Logger logger = LoggerFactory.getLogger(AbstractChannel.class);
    /**
     * 如果此Channel是 SocketChannel，那么这个 parent 就是 ServerSocketChannel
     */
    private final Channel parent;
    /**
     * 此 Channel 的 id
     */
    private final ChannelId id;


    /**
     * 用于关闭此 Channel 的 Future
     */
    private final CloseFuture closeFuture = new CloseFuture(this);

    private volatile SocketAddress localAddress;
    private volatile SocketAddress remoteAddress;

    private Throwable initialCloseCause;

    /**
     * 每一个 Channel 都要绑定在 EventLoop 上
     * 它们是一对多的关系。EventLoop 中的 Selector 负责循环检测各个channel中是否有事件
     */
    private volatile EventLoop eventLoop;
    /**
     * 此Channel是否已经将自己注册EventLoop上了
     */
    private volatile boolean registered;


    public AbstractChannel(Channel parent) {
        this.parent = parent;
        this.id = newId();
    }
    public AbstractChannel(Channel parent, ChannelId id) {
        this.parent = parent;
        this.id = id;
    }

    protected abstract boolean isCompatible(EventLoop loop);

    @Override
    public final ChannelId id() {
        return id;
    }
    protected ChannelId newId() {
        return DefaultChannelId.newInstance();
    }

    @Override
    public void register(EventLoop eventLoop, ChannelPromise promise) {
        if (eventLoop == null) {
            throw new NullPointerException("eventLoop is null");
        }
        // 如果已经注册过，将这个任务设置为失败
        if (isRegistered()) {
            promise.setFailure(new IllegalStateException("registered to an event loop already"));
            return;
        }
        // 判断当前使用的执行器是否为 NioEventLoop，如果不是，此任务失败
        if (!isCompatible(eventLoop)) {
            promise.setFailure(new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
            return;
        }
        AbstractChannel.this.eventLoop = eventLoop;
        if (eventLoop.inEventLoop(Thread.currentThread())) {
            register0(promise);
        } else {
            try {
                eventLoop.execute(() -> {
                    register0(promise);
                });
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
        }

    }

    private void register0(ChannelPromise promise) {
        try {
            // 注册任务不能被打断
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }
            // 调用注册方法，将此 Channel 注册到 eventLoop 中
            // 这个 doRegister 交给子类实现，比如 SocketChannel.register()、ServerSocketChannel.register()
            doRegister();
            registered = true;
            // 设置注册任务成功
            safeSetSuccess(promise);
            // 给 Channel 注册读事件
            beginRead();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    protected final boolean ensureOpen(ChannelPromise promise) {
        if (isOpen()) {
            return true;
        }
        safeSetFailure(promise, newClosedChannelException(initialCloseCause));
        return false;
    }
    public final void safeSetSuccess(ChannelPromise promise) {
        if (!promise.trySuccess()) {
            logger.error("Failed to mark a promise as success because it is done already :" + promise);
        }
    }
    public final void safeSetFailure(ChannelPromise promise, Throwable cause) {
        if (!promise.tryFailure(cause)) {
            throw new RuntimeException(cause);
        }
    }
    protected void doRegister() throws Exception {}

    protected abstract void doBeginRead() throws Exception;

    protected abstract void doBind(SocketAddress localAddress) throws Exception;
    @Override
    public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
        try {
            doBind(localAddress);
            safeSetSuccess(promise);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void beginRead() {
        if (!isActive()) {
            return;
        }
        try {
            doBeginRead();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ClosedChannelException newClosedChannelException(Throwable cause) {
        ClosedChannelException exception = new ClosedChannelException();
        if (cause != null) {
            exception.initCause(cause);
        }
        return exception;
    }
    /**
     * 获取负责 Channel 的EventLoop
     * @return
     */
    @Override
    public EventLoop eventLoop() {
        EventLoop eventLoop = this.eventLoop;
        if (eventLoop == null) {
            throw new IllegalStateException("channel not registered to an event loop");
        }
        return eventLoop;
    }
    @Override
    public Channel parent() {
        return parent;
    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public SocketAddress localAddress() {
        return null;
    }

    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    public ChannelFuture closeFuture() {
        return closeFuture;
    }

    @Override
    public ChannelFuture close() {
        return null;
    }

    static final class CloseFuture extends DefaultChannelPromise {

        CloseFuture(AbstractChannel ch) {
            super(ch);
        }

        @Override
        public ChannelPromise setSuccess() {
            throw new IllegalStateException();
        }

        @Override
        public ChannelPromise setFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        @Override
        public boolean trySuccess() {
            throw new IllegalStateException();
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        boolean setClosed() {
            return super.trySuccess();
        }
    }
}
