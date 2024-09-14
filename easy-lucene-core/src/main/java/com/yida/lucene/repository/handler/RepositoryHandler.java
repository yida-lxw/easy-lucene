package com.yida.lucene.repository.handler;

import com.yida.lucene.plugin.Invocation;
import com.yida.lucene.repository.ElRepository;
import org.apache.lucene.search.TopDocs;

/**
 * @author yida
 * @date 2024/8/29 2:42
 * @see com.yida.lucene.repository.RepositoryInterceptor#intercept(Invocation)
 */
@SuppressWarnings("all")
public interface RepositoryHandler {

	/**
	 * 从小到大排序
	 *
	 * @return
	 */
	int order();

	default boolean willDoInsert(Invocation invocation) {
		return true;
	}

	default boolean willDoDelete(Invocation invocation) {
		return true;
	}

	default boolean willDoUpdate(Invocation invocation) {
		return true;
	}

	default boolean willDoSearch(Invocation invocation) {
		return true;
	}

	default void beforeInsert(Invocation invocation) {
	}

	default void afterInsert(Invocation invocation) {
	}

	default void exceptionInsert(Invocation invocation, Throwable e) {
	}

	default void finallyInsert(Invocation invocation) {
	}

	default void beforeUpdate(Invocation invocation) {
	}

	default void afterUpdate(Invocation invocation) {
	}

	default void exceptionUpdate(Invocation invocation, Throwable e) {
	}

	default void finallyUpdate(Invocation invocation) {
	}

	default void beforeDelete(Invocation invocation) {
	}

	default void afterDelete(Invocation invocation) {
	}

	default void exceptionDelete(Invocation invocation, Throwable e) {
	}

	default void finallyDelete(Invocation invocation) {
	}

	default void beforeSearch(Invocation invocation) {
	}

	default void afterSearch(Invocation invocation, TopDocs res) {
	}

	default void exceptionSearch(Invocation invocation, Throwable e) {
	}

	default void finallySearch(Invocation invocation) {
	}

	static ElRepository getReposity(Invocation invocation) {
		return ((ElRepository) invocation.getTarget());
	}

}
