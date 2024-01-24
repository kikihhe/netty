package org.xiaohe.util;

import java.util.Queue;

public interface EventLoopTaskQueueFactory {


    Queue<Runnable> newTaskQueue(int maxCapacity);
}