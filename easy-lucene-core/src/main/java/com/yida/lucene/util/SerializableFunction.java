package com.yida.lucene.util;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author yida
 * @date 2024/09/25 13:30
 */
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
