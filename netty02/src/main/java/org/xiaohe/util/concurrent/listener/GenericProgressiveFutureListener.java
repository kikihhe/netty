package org.xiaohe.util.concurrent.listener;

import org.xiaohe.util.concurrent.future.ProgressiveFuture;

public interface GenericProgressiveFutureListener<F extends ProgressiveFuture<?>> extends GenericFutureListener<F> {

    void operationProgressed(F future, long progress, long total) throws Exception;
}