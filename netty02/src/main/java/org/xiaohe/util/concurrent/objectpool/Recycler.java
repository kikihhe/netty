package org.xiaohe.util.concurrent.objectpool;

import org.omg.CORBA.INITIALIZE;
import org.xiaohe.util.concurrent.threadlocal.FastThreadLocal;

import java.util.Arrays;


/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-25 19:12
 */
public abstract class Recycler<T> {
    private static final int DEFAULT_MAX_CAPACITY_PRE_THREAD = 4 * 1024;


    private final FastThreadLocal<Stack<T>> threadLocal = new FastThreadLocal<Stack<T>>() {
        @Override
        protected Stack<T> initialValue() throws Exception {
            return new Stack<T>(DEFAULT_MAX_CAPACITY_PRE_THREAD);
        }
    };


    public Recycler() {

    }
    public final T get() {
        // 根据 threadLocal 获取对象池
        Stack<T> stack = threadLocal.get();
        DefaultHandle<T> handle = stack.pop();
        if (handle == null) {
            handle = stack.newHandle();
            handle.value = newObject(handle);
        }
        return (T) handle;
    }
    public void recycle(Object object) {

    }
    protected abstract T newObject(Handle<T> handle);


    static final class Stack<T> {
        private int INITIAL_CAPACITY = 256;
        /**
         * 存放 Handle 的数组容器
         */
        private DefaultHandle<?>[] elements;
        /**
         * 对象池现有容量
         */
        private int size;
        private final int maxCapacity;
        public Stack(int maxCapacity) {
            this.maxCapacity = maxCapacity;
            elements = new DefaultHandle[INITIAL_CAPACITY];
        }
        DefaultHandle<T> newHandle() {
            return new DefaultHandle<>(this);
        }
        public DefaultHandle pop() {
            int size = this.size;
            if (size == 0) {
                return null;
            }
            size--;
            DefaultHandle object = elements[size];
            elements[size] = null;
            this.size = size;
            return object;
        }
        /**
         * 将对象归还给对象池
         * @param object
         */
        public void push(DefaultHandle<?> object) {
            int size = this.size;
            // 如果超过最大了就不能扩容了
            if (size >= maxCapacity) {
                return;
            }
            if (size == elements.length) {
                elements = Arrays.copyOf(elements, Math.min(size << 1, maxCapacity));
            }
            elements[size] = object;
            this.size = size + 1;
        }
    }
    static final class DefaultHandle<T> implements Handle<T> {
        /**
         * 这个对象是从哪个对象池中获取的
         */
        private Stack<?> stack;
        /**
         * handle 拥有的对象池中的对象
         */
        private Object value;
        DefaultHandle(Stack<?> stack) {
            this.stack = stack;
        }
        @Override
        public void recycle(Object object) {
            if (object != value) {
                throw new IllegalArgumentException();
            }
            Stack<?> stack = this.stack;
            // 将此 handle 放回到对象池中
            stack.push(this);
        }
    }
}
