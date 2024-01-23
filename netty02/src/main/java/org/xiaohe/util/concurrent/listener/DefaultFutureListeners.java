package org.xiaohe.util.concurrent.listener;

import jdk.nashorn.internal.objects.GenericPropertyDescriptor;
import org.xiaohe.util.concurrent.future.Future;

import java.util.Arrays;

/**
 * @author : 小何
 * @Description : 监听器数组，每一个 promise 持有一个
 * @date : 2024-01-23 19:35
 */
public class DefaultFutureListeners {
    private GenericFutureListener<? extends Future<?>>[] listeners;
    private int size;
    private int progressiveSize;
    public DefaultFutureListeners(GenericFutureListener<? extends Future<?>> first, GenericFutureListener<? extends Future<?>> second) {
        listeners = new GenericFutureListener[2];
        listeners[0] = first;
        listeners[1] = second;
        size = 2;
        if (first instanceof GenericProgressiveFutureListener) {
            progressiveSize++;
        }
        if (second instanceof GenericProgressiveFutureListener) {
            progressiveSize++;
        }
    }
    public void add(GenericFutureListener<? extends Future<?>> listener) {
        GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
        final int size = this.size;
        // 如果已经满了，扩容为原来的两倍
        if (size == listeners.length) {
            this.listeners = listeners = Arrays.copyOf(listeners, size << 1);
        }
        listeners[size] = listener;
        this.size = size + 1;
        if (listener instanceof GenericProgressiveFutureListener) {
            progressiveSize++;
        }
    }

    public void remove(GenericFutureListener<? extends Future<?>> listener) {
        final GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
        int size = this.size;
        for (int i = 0; i < size; i++) {
            if (listeners[i] == listener) {
                int listenersToMove = size - i - 1;
                if (listenersToMove > 0) {
                    System.arraycopy(listeners, i + 1, listeners, i, listenersToMove);
                }
                listeners[--size] = null;
                this.size = size;
                if (listener instanceof GenericProgressiveFutureListener) {
                    progressiveSize--;
                }
            }
            return;
        }
    }

    public GenericFutureListener<? extends Future<?>>[] listeners() {
        return listeners;
    }

    public int size() {
        return size;
    }

    public int progressiveSize() {
        return progressiveSize;
    }
}
