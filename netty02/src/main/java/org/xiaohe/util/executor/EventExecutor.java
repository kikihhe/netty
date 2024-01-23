package org.xiaohe.util.executor;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-23 20:14
 */
public interface EventExecutor extends EventExecutorGroup {
    EventExecutor next();
    EventExecutorGroup parent();

    boolean inEventLoop(Thread thread);
}
