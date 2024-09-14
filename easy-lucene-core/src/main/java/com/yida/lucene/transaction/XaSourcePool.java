package com.yida.lucene.transaction;

import java.util.Collection;

/**
 * @author yida
 * @date 2024/9/2 11:14
 */
public interface XaSourcePool {

	/**
	 * 获取XaSource
	 *
	 * @param docClass docClass
	 * @param <T>      T
	 * @return XaSource
	 */
	<T> XaSource<T> getXaSource(Class<T> docClass);

	/**
	 * 获取XaSources
	 *
	 * @return Collection
	 */
	Collection<XaSource<?>> getXaSources();
}
