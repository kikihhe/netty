package org.xiaohe.util.concurrent.threadlocal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-25 17:02
 */
public class FastThreadLocal<V> {
    private static final Logger logger = LoggerFactory.getLogger(FastThreadLocal.class);

    /**
     * 静态变量，默认为0
     */
    private static final int variablesToRemoveIndex = InternalThreadLocalMap.nextVariableIndex();

    private final int index;

    public FastThreadLocal() {
        index = InternalThreadLocalMap.nextVariableIndex();
    }


    public final V get() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
        // 根据这个 FastThreadLocal 的下标，拿到Map中对应位置的元素
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }
        return initialize(threadLocalMap);
    }
    public final V getIfExists() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap != null) {
            Object v = threadLocalMap.indexedVariable(index);
            if (v != InternalThreadLocalMap.UNSET) {
                return (V) v;
            }
        }
        return null;
    }
    public final V get(InternalThreadLocalMap threadLocalMap) {
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }

        return initialize(threadLocalMap);
    }
    private V initialize(InternalThreadLocalMap threadLocalMap) {
        V v = null;
        try {
            v = initialValue();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        threadLocalMap.setIndexVariable(index, v);
        // TODO AddToVariablesToRemove
        return v;
    }
    public final void set(V value) {
        if (value != InternalThreadLocalMap.UNSET) {
            InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
            setKnownNotUnset(threadLocalMap, value);
        } else {
            remove();
        }
    }
    public final void set(InternalThreadLocalMap threadLocalMap, V value) {
        if (value != InternalThreadLocalMap.UNSET) {
            setKnownNotUnset(threadLocalMap, value);
        } else {
            remove(threadLocalMap);
        }
    }
    public static void removeAll() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return;
        }
        try {
            // 拿到 set
            Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
            if (v != null && v != InternalThreadLocalMap.UNSET) {

            }
            // 如果 v 为空，说明现在没有 FastThreadLocal放进去
            Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
            FastThreadLocal[] variablesToRemoveArray = variablesToRemove.toArray(new FastThreadLocal[0]);
            for (FastThreadLocal fastThreadLocal : variablesToRemoveArray) {
                fastThreadLocal.remove();
            }
        } finally {
            InternalThreadLocalMap.remove();
        }
    }
    private void setKnownNotUnset(InternalThreadLocalMap threadLocalMap, V value) {
        if (threadLocalMap.setIndexVariable(index, value)) {
            // 把 ThreadLocal 对象放进这个 threadLocalMap 的 set 中
            addToVariablesToRemove(threadLocalMap, this);
        }
    }

    private void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<V> variable) {
        Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
        Set<FastThreadLocal<?>> variablesToRemove;
        if (v == InternalThreadLocalMap.UNSET || v == null) {
            variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
            threadLocalMap.setIndexVariable(variablesToRemoveIndex, variablesToRemove);
        } else {
            variablesToRemove = (Set<FastThreadLocal<?>>) v;
        }
        variablesToRemove.add(variable);
    }

    public static int size() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return 0;
        } else {
            return threadLocalMap.size();
        }
    }

    public static void destroy() {
        InternalThreadLocalMap.destroy();
    }

    public final void remove() {
        remove(InternalThreadLocalMap.getIfSet());
    }
    public final void remove(InternalThreadLocalMap threadLocalMap) {
        if (threadLocalMap == null) {
            return;
        }
        // 用fastthreadlocal的下标从map中得到存储的数据
        Object v = threadLocalMap.removeIndexedVariable(index);
        // 从map0号位置的set中删除fastthreadlocal对象
        removeFromVariablesToRemove(threadLocalMap, this);
        if (v != InternalThreadLocalMap.UNSET) {
            try {
                // 该方法可以由用户自己实现，可以对value做一些处理
                onRemoval((V) v);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void removeFromVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<V> variable) {
        //根据0下标获得set集合
        Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
        if (v == InternalThreadLocalMap.UNSET || v == null) {
            return;
        }
        Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
        variablesToRemove.remove(variable);
    }

    protected V initialValue() throws Exception {
        return null;
    }
    protected void onRemoval(@SuppressWarnings("UnusedParameters") V value) throws Exception { }
}
