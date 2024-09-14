package com.yida.lucene.repository;

import com.yida.lucene.core.DocFactory;
import com.yida.lucene.core.Source;
import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.repository.query.Querys;
import com.yida.lucene.transaction.ElTransaction;
import com.yida.lucene.transaction.ElTransactionManager;
import com.yida.lucene.transaction.TransactionSupportFutureTask;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author yida
 * @date 2024/8/5 09:49
 */
class ElRepositoryImpl<T> implements ElRepository<T> {
	private static final Logger logger = LoggerFactory.getLogger(ElRepositoryImpl.class);

	private final Source<T> source;
	private final ElTransactionManager txManager;

	@Override
	public Source<T> getSource() {
		return this.source;
	}

	public ElRepositoryImpl(Source<T> source) {
		this.source = source;
		this.txManager = source.getTxManager();
	}

	@Override
	public Future<Integer> insertFuture(T entity) {
		Document doc = source.getDocFactory().createDoc(entity);
		ElTransaction elTransaction = txManager.getElTransaction();
		TransactionSupportFutureTask futureTask = new TransactionSupportFutureTask(
				() -> warpResultWithException(() -> {
					IndexWriter writer = source.getWriter();
					int before = writer.getDocStats().maxDoc;
					writer.addDocument(doc);
					int after = writer.getDocStats().maxDoc;
					return Math.abs(after - before);
				}), elTransaction);
		elTransaction.addTask(futureTask);
		return futureTask;
	}

	@Override
	public Future<Integer> insertFuture(Collection<T> entities) {
		if (entities == null || entities.isEmpty()) {
			return DONE_FUTURE;
		}
		List<Document> collect = entities.stream().map(source.getDocFactory()::createDoc).collect(Collectors.toList());
		ElTransaction elTransaction = txManager.getElTransaction();
		TransactionSupportFutureTask futureTask = new TransactionSupportFutureTask(
				() -> warpResultWithException(() -> {
					IndexWriter writer = source.getWriter();
					int before = writer.getDocStats().maxDoc;
					writer.addDocuments(collect);
					int after = writer.getDocStats().maxDoc;
					return Math.abs(after - before);
				}), elTransaction);
		elTransaction.addTask(futureTask);
		return futureTask;
	}

	@Override
	public Future<Integer> updateFuture(T entity) {
		DocFactory<T> docFactory = source.getDocFactory();
		Serializable id = docFactory.getId(entity);
		if (Objects.isNull(id)) {
			throw EmbeddedLuceneException.of("docId can not been null when update");
		}
		Document doc = source.getDocFactory().createDoc(entity);
		Term idTerm = new Term(docFactory.getIdFieldName(), id.toString());
		ElTransaction elTransaction = txManager.getElTransaction();
		TransactionSupportFutureTask futureTask = new TransactionSupportFutureTask(
				() -> warpResultWithException(() -> {
					IndexWriter writer = source.getWriter();
					int before = writer.getDocStats().maxDoc;
					writer.updateDocument(idTerm, doc);
					int after = writer.getDocStats().maxDoc;
					return Math.abs(after - before);
				}), elTransaction);
		elTransaction.addTask(futureTask);
		return futureTask;
	}

	@Override
	public Future<Integer> updateFuture(Collection<T> entity) {
		if (entity == null || entity.isEmpty()) {
			return DONE_FUTURE;
		}
		DocFactory<T> docFactory = source.getDocFactory();
		Query[] queries = entity.stream().map(docFactory::getIdQuery)
				.toArray(Query[]::new);
		ElTransaction elTransaction = txManager.getElTransaction();
		List<Document> collect = entity.stream().map(docFactory::createDoc).collect(Collectors.toList());
		TransactionSupportFutureTask futureTask = new TransactionSupportFutureTask(
				() -> warpResultWithException(() -> {
					IndexWriter writer = source.getWriter();
					int before = writer.getDocStats().maxDoc;
					writer.deleteDocuments(queries);
					writer.addDocuments(collect);
					int after = writer.getDocStats().maxDoc;
					return Math.abs(after - before);
				}), elTransaction);
		elTransaction.addTask(futureTask);
		return futureTask;
	}

	@Override
	public Future<Integer> deleteFuture(Query... queries) {
		ElTransaction elTransaction = txManager.getElTransaction();
		TransactionSupportFutureTask futureTask = new TransactionSupportFutureTask(
				() -> warpResultWithException(() -> {
					IndexWriter writer = source.getWriter();
					writer.deleteDocuments(queries);
					TotalHitCountCollector collector = Querys.totalHitsCollector();
					source.getSearchHolder().getIndexSearcher().search(Querys.compose(queries), collector);
					return collector.getTotalHits();
				}), elTransaction);
		elTransaction.addTask(futureTask);
		return futureTask;
	}

	@Override
	public void insert(T entity) {
		Document doc = source.getDocFactory().createDoc(entity);
		txManager.getElTransaction().addTask(
				() -> warpResultWithException(() -> {
					source.getWriter().addDocument(doc);
					return null;
				}));
	}

	@Override
	public void update(Collection<T> entity) {
		if (entity == null || entity.isEmpty()) {
			return;
		}
		DocFactory<T> docFactory = source.getDocFactory();
		Query[] queries = entity.stream().map(docFactory::getIdQuery)
				.toArray(Query[]::new);
		txManager.getElTransaction().addTask(
				() -> warpResultWithException(() -> {
					IndexWriter writer = source.getWriter();
					writer.deleteDocuments(queries);
					writer.addDocuments(entity.stream().map(docFactory::createDoc).collect(Collectors.toList()));
					return null;
				}));
	}

	@Override
	public void update(T entity) {
		DocFactory<T> docFactory = source.getDocFactory();
		Serializable id = docFactory.getId(entity);
		if (Objects.isNull(id)) {
			throw EmbeddedLuceneException.of("docId can not been null when update");
		}
		Document doc = source.getDocFactory().createDoc(entity);
		Term idTerm = new Term(docFactory.getIdFieldName(), id.toString());
		txManager.getElTransaction().addTask(
				() -> warpResultWithException(() -> {
					source.getWriter().updateDocument(idTerm, doc);
					return null;
				}));
	}

	@Override
	public void insert(Collection<T> entities) {
		if (entities == null || entities.isEmpty()) {
			return;
		}
		List<Document> collect = entities.stream().map(source.getDocFactory()::createDoc).collect(Collectors.toList());
		txManager.getElTransaction().addTask(
				() -> warpResultWithException(() -> {
					source.getWriter().addDocuments(collect);
					return null;
				}));
	}

	@Override
	public void delete(Query... queries) {
		txManager.getElTransaction().addTask(
				() -> warpResultWithException(() -> {
					source.getWriter().deleteDocuments(queries);
					return null;
				}));
	}

	@Override
	public TopDocs search(Query query, Sort sort, int topK) {
		return warpResultWithException(() -> {
			IndexSearcher searcher = source.getSearchHolder().getIndexSearcher();
			int maxDoc = searcher.getIndexReader().maxDoc();
			if (maxDoc <= 0) {
				return EMPTY_TOP_DOCS;
			}

			TopDocs topDocs;
			if (Objects.nonNull(sort)) {
				topDocs = searcher.search(query, topK <= 0 ? maxDoc : topK, sort);
			} else {
				topDocs = searcher.search(query, topK <= 0 ? maxDoc : topK);
			}
			return topDocs;
		});
	}

	@Override
	public TopDocs searchByKeyword(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap, Sort sort) {
		return warpResultWithException(() -> {
			IndexSearcher searcher = source.getSearchHolder().getIndexSearcher();
			int maxDoc = searcher.getIndexReader().maxDoc();
			if (maxDoc <= 0) {
				return EMPTY_TOP_DOCS;
			}
			TopDocs topDocs = null;
			final Source<T> source = getSource();
			Analyzer analyzer = source.getAnalyzer();
			QueryParser queryParser = new MultiFieldQueryParser(queryFields, analyzer, fieldBoostMap);
			Query query = queryParser.parse(queryKeyword);
			if (Objects.nonNull(sort)) {
				topDocs = searcher.search(query, maxDoc, sort);
			} else {
				topDocs = searcher.search(query, maxDoc);
			}
			return topDocs;
		});
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
}
