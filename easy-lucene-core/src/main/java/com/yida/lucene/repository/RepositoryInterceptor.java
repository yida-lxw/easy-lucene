package com.yida.lucene.repository;

import com.yida.lucene.plugin.Interceptor;
import com.yida.lucene.plugin.Intercepts;
import com.yida.lucene.plugin.Invocation;
import com.yida.lucene.plugin.Plugin;
import com.yida.lucene.plugin.Signature;
import com.yida.lucene.repository.handler.RepositoryHandler;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yida
 * @date 2024/8/29 11:55
 */
@Intercepts(
		{
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.INSERT, args = {Object.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.INSERT, args = {Collection.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.UPDATE, args = {Object.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.UPDATE, args = {Collection.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.DELETE, args = {Query[].class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.INSERT_FUTURE, args = {Object.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.INSERT_FUTURE, args = {Collection.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.UPDATE_FUTURE, args = {Object.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.UPDATE_FUTURE, args = {Collection.class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.DELETE_FUTURE, args = {Query[].class}),
				@Signature(type = ElRepository.class, method = RepositoryInterceptor.SEARCH, args = {Query.class, Sort.class, int.class}),
		}
)
public class RepositoryInterceptor implements Interceptor {

	public static final String INSERT = "insert";
	public static final String DELETE = "delete";
	public static final String UPDATE = "update";
	public static final String SEARCH = "search";
	public static final String INSERT_FUTURE = "insertFuture";
	public static final String DELETE_FUTURE = "deleteFuture";
	public static final String UPDATE_FUTURE = "updateFuture";

	private List<RepositoryHandler> handlers;

	public void setHandlers(List<RepositoryHandler> handlers) {
		this.handlers = handlers.stream()
				.sorted(Comparator.comparing(RepositoryHandler::order))
				.collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("all")
	public Object intercept(Invocation invocation) throws Throwable {
		final Method method = invocation.getMethod();
		final ElRepository repository = (ElRepository) invocation.getTarget();
		String methodName = method.getName();
		try {
			for (RepositoryHandler handler : handlers) {
				switch (methodName) {
					case INSERT:
					case INSERT_FUTURE:
						handler.beforeInsert(invocation);
						if (!handler.willDoInsert(invocation)) {
							return null;
						}
						break;
					case DELETE:
					case DELETE_FUTURE:
						handler.beforeDelete(invocation);
						if (!handler.willDoDelete(invocation)) {
							return null;
						}
						break;
					case UPDATE:
					case UPDATE_FUTURE:
						handler.beforeUpdate(invocation);
						if (!handler.willDoUpdate(invocation)) {
							return null;
						}
						break;
					case SEARCH:
						handler.beforeSearch(invocation);
						if (!handler.willDoSearch(invocation)) {
							return null;
						}
						break;
				}
			}
			Object res = method.invoke(repository, invocation.getArgs());
			for (RepositoryHandler handler : handlers) {
				switch (methodName) {
					case INSERT:
					case INSERT_FUTURE:
						handler.afterInsert(invocation);
						break;
					case DELETE:
					case DELETE_FUTURE:
						handler.afterDelete(invocation);
						break;
					case UPDATE:
					case UPDATE_FUTURE:
						handler.afterUpdate(invocation);
						break;
					case SEARCH:
						handler.afterSearch(invocation, (TopDocs) res);
						break;
				}
			}
			return res;
		} catch (Throwable e) {
			for (RepositoryHandler handler : handlers) {
				switch (methodName) {
					case INSERT:
					case INSERT_FUTURE:
						handler.exceptionInsert(invocation, e);
						break;
					case DELETE:
					case DELETE_FUTURE:
						handler.exceptionDelete(invocation, e);
						break;
					case UPDATE:
					case UPDATE_FUTURE:
						handler.exceptionUpdate(invocation, e);
						break;
					case SEARCH:
						handler.exceptionSearch(invocation, e);
						break;
				}
			}
			throw e;
		} finally {
			for (RepositoryHandler handler : handlers) {
				switch (methodName) {
					case INSERT:
					case INSERT_FUTURE:
						handler.finallyInsert(invocation);
						break;
					case DELETE:
					case DELETE_FUTURE:
						handler.finallyDelete(invocation);
						break;
					case UPDATE:
					case UPDATE_FUTURE:
						handler.finallyUpdate(invocation);
						break;
					case SEARCH:
						handler.finallySearch(invocation);
						break;
				}
			}
		}
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof ElRepository) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

}
