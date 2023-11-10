package Netty;

import java.util.concurrent.TimeUnit;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-10 16:57
 */
public interface EventExecutorGroup {
    EventExecutor next();

    void shutdownGracefully(long quietPeriod, long timeout, TimeUnit timeUnit);

}
