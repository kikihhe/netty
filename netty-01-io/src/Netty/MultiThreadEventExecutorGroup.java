package Netty;

import java.util.concurrent.TimeUnit;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-10 16:59
 */
public abstract class MultiThreadEventExecutorGroup implements EventExecutorGroup {
    private EventExecutor[] eventExecutor;

    public MultiThreadEventExecutorGroup(int threads) {
        eventExecutor = new EventExecutor[threads];
        for (int i = 0; i < threads; i++) {
            eventExecutor[i] = newChild();
        }
    }

    protected abstract EventExecutor newChild();

    @Override
    public EventExecutor next() {
        int id = index % eventExecutor.length;
        index++;
        return eventExecutor[id];
    }

    @Override
    public void shutdownGracefully(long quietPeriod, long timeout, TimeUnit timeUnit) {
        next().shutdownGracefully(quietPeriod, timeout, timeUnit);
    }
}
