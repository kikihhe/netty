package org.xiaohe;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-22 23:17
 */
public abstract class MultiThreadEventExecutorGroup implements EventExecutorGroup {
    private EventExecutor[] eventExecutors;
    private int index;

    public MultiThreadEventExecutorGroup(int threads) {
        this.eventExecutors = new EventExecutor[threads];
        for (int i = 0; i < eventExecutors.length; i++) {
//            eventExecutors[i] = new
        }
        this.index = 0;
    }
    public EventExecutor next() {
        int id = index % eventExecutors.length;
        index++;
        return eventExecutors[id];
    }
}
