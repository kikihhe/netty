package org.xiaohe.util;

import org.xiaohe.util.executor.SingleThreadEventExecutor;

public interface RejectedExecutionHandler {

    void rejected(Runnable task, SingleThreadEventExecutor executor);
}
