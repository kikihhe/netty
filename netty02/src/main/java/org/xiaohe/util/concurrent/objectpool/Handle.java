package org.xiaohe.util.concurrent.objectpool;

public interface Handle<T> {
    /**
     * 回收对象的方法
     * @param object
     */
    void recycle(T object);
}