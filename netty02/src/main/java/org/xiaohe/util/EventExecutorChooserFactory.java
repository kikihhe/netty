package org.xiaohe.util;

import org.xiaohe.util.executor.EventExecutor;

public interface EventExecutorChooserFactory {


    EventExecutorChooser newChooser(EventExecutor[] executors);


    interface EventExecutorChooser {

        /**
         * Returns the new {@link EventExecutor} to use.
         */
        EventExecutor next();
    }
}