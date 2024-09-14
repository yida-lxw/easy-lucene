package com.yida.lucene.repository;

import com.yida.lucene.core.Source;
import com.yida.lucene.transaction.ElTransaction;
import com.yida.lucene.transaction.ElTransactionManager;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * 保证所有方法的动态代理生效<br/>
 * 事务支持
 *
 * @author yida
 * @date 2024/8/30 14:04
 */
class ElRepositoryDelegate<T> implements ElRepository<T> {

	private final ElRepository<T> target;
	private final ElTransactionManager txManager;
	private final Class<T> docClass;
	private final Source<T> source;

	ElRepositoryDelegate(ElRepository<T> target, ElTransactionManager txManager) {
		this.target = target;
		this.source = target.getSource();
		this.txManager = txManager;
		this.docClass = source.getDocClass();
	}

	@Override
	public Future<Integer> insertFuture(T entity) {
		return updateTransactionAdapt(() -> target.insertFuture(entity));
	}

	@Override
	public Future<Integer> insertFuture(Collection<T> entity) {
		return updateTransactionAdapt(() -> target.insertFuture(entity));
	}

	@Override
	public Future<Integer> updateFuture(T entity) {
		return updateTransactionAdapt(() -> target.updateFuture(entity));
	}

	@Override
	public Future<Integer> updateFuture(Collection<T> entity) {
		return updateTransactionAdapt(() -> target.updateFuture(entity));
	}

	@Override
	public Future<Integer> deleteFuture(Query... queries) {
		return updateTransactionAdapt(() -> target.deleteFuture(queries));
	}

	@Override
	public void insert(T entity) {
		updateTransactionAdapt(() -> {
			target.insert(entity);
			return null;
		});
	}

	@Override
	public void insert(Collection<T> entity) {
		updateTransactionAdapt(() -> {
			target.insert(entity);
			return null;
		});
	}

	@Override
	public void update(T entity) {
		updateTransactionAdapt(() -> {
			target.update(entity);
			return null;
		});
	}

	@Override
	public void update(Collection<T> entity) {
		updateTransactionAdapt(() -> {
			target.update(entity);
			return null;
		});
	}

	@Override
	public void delete(Query... queries) {
		updateTransactionAdapt(() -> {
			target.delete(queries);
			return null;
		});
	}

	@Override
	public TopDocs search(Query query, Sort sort, int limit) {
		searchTransactionAdapt();
		return target.search(query, sort, limit);
	}

	@Override
	public TopDocs searchByKeyword(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap, Sort sort) {
		searchTransactionAdapt();
		return target.searchByKeyword(queryKeyword, queryFields, fieldBoostMap, sort);
	}

	@Override
	public TopDocs searchByKeyword(String queryKeyword, String[] queryFields, Sort sort) {
		return searchByKeyword(queryKeyword, queryFields, null, sort);
	}

	@Override
	public TopDocs searchByKeyword(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap) {
		return searchByKeyword(queryKeyword, queryFields, fieldBoostMap, null);
	}

	@Override
	public TopDocs searchByKeyword(String queryKeyword, String[] queryFields) {
		return searchByKeyword(queryKeyword, queryFields, (Sort) null);
	}

	@Override
	public TopDocs searchByKeyword(String queryKeyword, String queryField) {
		return searchByKeyword(queryKeyword, new String[]{queryField});
	}

	@Override
	public Source<T> getSource() {
		return source;
	}

	private <R> R updateTransactionAdapt(Supplier<R> update) {
		boolean hasTx = false;
		ElTransaction elTransaction = txManager.getElTransaction();
		if (Objects.nonNull(elTransaction)) {
			hasTx = true;
		} else {
			txManager.begin();
		}
		try {
			txManager.getElTransaction().enlistResource(txManager.getXaSource(docClass));
			R r = update.get();
			if (!hasTx) {
				txManager.commit();
			}
			return r;
		} catch (Throwable e) {
			if (!hasTx) {
				txManager.rollback();
			}
			throw e;
		}
	}

	private void searchTransactionAdapt() {
		ElTransaction elTransaction = txManager.getElTransaction();
		if (Objects.nonNull(elTransaction)) {
			elTransaction.enlistResource(txManager.getXaSource(docClass));
		}
	}

}
