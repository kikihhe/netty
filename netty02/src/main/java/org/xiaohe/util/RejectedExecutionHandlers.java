package org.xiaohe.util;

import org.xiaohe.util.executor.SingleThreadEventExecutor;

import java.util.concurrent.RejectedExecutionException;

public class RejectedExecutionHandlers {

    private static final RejectedExecutionHandler REJECT = new RejectedExecutionHandler() {
        @Override
        public void rejected(Runnable task, SingleThreadEventExecutor executor) {
            throw new RejectedExecutionException();
        }
    };

    private RejectedExecutionHandlers() { }


    public static RejectedExecutionHandler reject() {
        return REJECT;
    }

}