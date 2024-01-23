package org.xiaohe;

import java.util.concurrent.TimeUnit;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-22 23:15
 */
public interface EventExecutorGroup {
    public EventExecutor next();
    public void shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit);
}
