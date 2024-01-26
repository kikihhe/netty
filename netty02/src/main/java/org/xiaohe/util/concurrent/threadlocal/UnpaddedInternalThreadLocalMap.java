package org.xiaohe.util.concurrent.threadlocal;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : 小何
 * @Description :
 * @date : 2024-01-25 16:45
 */
public class UnpaddedInternalThreadLocalMap {
    /**
     * 如果线程使用的不是 Netty 中的Thread，就用原生的 ThreadLocal
     */
    public static final ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = new ThreadLocal<InternalThreadLocalMap>();

    /**
     * 给每一个 FastThreadLocal 的索引
     */
    protected static final AtomicInteger nextIndex = new AtomicInteger();

    /**
     * 真正存放数据的数组，就是InternalThreadLocalMap中存放数据的数组
     */
    Object[] indexedVariables;

    int futureListenerStackDepth;
    int localChannelReaderStackDepth;
    Map<Class<?>, Boolean> handlerSharableCache;
    ThreadLocalRandom random;
    StringBuilder stringBuilder;
    Map<Charset, CharsetEncoder> charsetEncoderCache;
    Map<Charset, CharsetDecoder> charsetDecoderCache;
    ArrayList<Object> arrayList;

    UnpaddedInternalThreadLocalMap(Object[] indexedVariables) {
        this.indexedVariables = indexedVariables;
    }
}
