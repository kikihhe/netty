package org.xiaohe.util.executor;

import java.util.concurrent.TimeUnit;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-24 15:28
 */
public abstract class AbstractEventExecutorGroup implements EventExecutorGroup {
    @Override
    public void shutdownGracefully() {

    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void awaitTermination(Integer integer, TimeUnit timeUnit) throws InterruptedException {

    }

    @Override
    public void execute(Runnable command) {
        next().execute(command);
    }
}
