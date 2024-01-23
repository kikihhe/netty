package org.xiaohe.util.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-23 20:13
 */
public interface EventExecutorGroup extends Executor {
    EventExecutor next();

    void shutdownGracefully();

    boolean isTerminated();

    void awaitTermination(Integer integer, TimeUnit unit) throws InterruptedException;
}
