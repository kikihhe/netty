package org.xiaohe.util.concurrent.listener;

import org.xiaohe.util.concurrent.future.Future;

import java.util.EventListener;

/**
 * @author : 小何
 * @Description : 回调方法
 * @date : 2024-01-23 19:15
 */
public interface GenericFutureListener<F extends Future<?>> extends EventListener {
    void operationComplete(F future) throws Exception;
}
