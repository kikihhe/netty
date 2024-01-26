package org.xiaohe.util.concurrent.threadlocal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaohe.util.internal.SystemPropertyUtil;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-25 16:50
 */
public class InternalThreadLocalMap extends UnpaddedInternalThreadLocalMap {
    private static final Logger logger = LoggerFactory.getLogger(InternalThreadLocalMap.class);
    private static final int DEFAULT_ARRAY_LIST_INITIAL_CAPACITY = 8;
    private static final int STRING_BUILDER_INITIAL_SIZE;
    private static final int STRING_BUILDER_MAX_SIZE;

    public static final Object UNSET = new Object();

    private BitSet cleanerFlags;

    static {
        STRING_BUILDER_INITIAL_SIZE =
                SystemPropertyUtil.getInt("io.netty.threadLocalMap.stringBuilder.initialSize", 1024);
        logger.debug("-Dio.netty.threadLocalMap.stringBuilder.initialSize: {}", STRING_BUILDER_INITIAL_SIZE);

        STRING_BUILDER_MAX_SIZE = SystemPropertyUtil.getInt("io.netty.threadLocalMap.stringBuilder.maxSize", 1024 * 4);
        logger.debug("-Dio.netty.threadLocalMap.stringBuilder.maxSize: {}", STRING_BUILDER_MAX_SIZE);
    }

    InternalThreadLocalMap() {
        super(newIndexedVariableTable());
    }


    /**
     * 得到当前线程的 ThreadLocalMap，有可能是 InternalThreadLocalMap
     * @return
     */
    public static InternalThreadLocalMap getIfSet() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            return ((FastThreadLocalThread) thread).threadLocalMap();
        }
        return slowThreadLocalMap.get();
    }

    public static InternalThreadLocalMap get() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            return fastGet((FastThreadLocalThread) thread);
        } else {
            return slowGet();
        }
    }

    private static InternalThreadLocalMap fastGet(FastThreadLocalThread thread) {
        InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
        if (threadLocalMap == null) {
            thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
        }
        return threadLocalMap;
    }

    private static InternalThreadLocalMap slowGet() {
        ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = UnpaddedInternalThreadLocalMap.slowThreadLocalMap;
        InternalThreadLocalMap threadLocalMap = slowThreadLocalMap.get();
        if (threadLocalMap == null) {
            threadLocalMap = new InternalThreadLocalMap();
            slowThreadLocalMap.set(threadLocalMap);
        }
        return threadLocalMap;
    }

    public static void remove() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            ((FastThreadLocalThread) thread).setThreadLocalMap(null);
        } else {
            slowThreadLocalMap.remove();
        }
    }

    public static void destroy() {
        slowThreadLocalMap.remove();
    }

    /**
     * 返回一个可用的 index
     * @return
     */
    public static int nextVariableIndex() {
        int index = nextIndex.getAndIncrement();
        if (index < 0) {
            nextIndex.decrementAndGet();
            throw new IllegalStateException();
        }
        return index;
    }

    /**
     * 返回最后一个 FastThreadLocal 的下标
     * @return
     */
    public static int lastVariableIndex() {
        return nextIndex.get() - 1;
    }
    private static Object[] newIndexedVariableTable() {
        Object[] array = new Object[32];
        Arrays.fill(array, UNSET);
        return array;
    }
    public int size() {
        int count = 0;
        for (Object o: indexedVariables) {
            if (o != UNSET) {
                count ++;
            }
        }
        return count - 1;
    }

    public Object indexedVariable(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length? lookup[index] : UNSET;
    }

    public boolean setIndexVariable(int index, Object value) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object oldValue = lookup[index];
            lookup[index] = value;
            return oldValue == UNSET;
        } else {
            // 扩容
            expandIndexedVariableTableAndSet(index, value);
            return true;
        }
    }

    private void expandIndexedVariableTableAndSet(int index, Object value) {
        Object[] oldArray = indexedVariables;
        final int oldCapacity = oldArray.length;;
        int newCapacity = index;
        newCapacity |= newCapacity >>> 1;
        newCapacity |= newCapacity >>> 2;
        newCapacity |= newCapacity >>> 4;
        newCapacity |= newCapacity >>> 8;
        newCapacity |= newCapacity >>> 16;
        newCapacity++;
        // 将原来那部分数据放过来
        Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
        // 多的位置填充UNSET
        Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
        newArray[index] = value;
        indexedVariables = newArray;
    }

    /**
     * 删除某个位置的元素，并重新赋值为 UNSET
     * @param index
     * @return
     */
    public Object removeIndexedVariable(int index) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object o = lookup[index];
            lookup[index] = UNSET;
            return o;
        } else {
            return UNSET;
        }
    }

    public boolean isIndexedVariableSet(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length && lookup[index] != UNSET;
    }
}
