package org.xiaohe.util.loop;

import org.xiaohe.util.executor.EventExecutor;
import org.xiaohe.util.executor.EventExecutorGroup;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-23 22:09
 */
public interface EventLoop extends EventExecutor, EventLoopGroup {
    @Override
    EventExecutorGroup parent();
}
