package com.yida.lucene.repository;

import com.yida.lucene.core.DocFactory;
import com.yida.lucene.core.ElDocument;
import com.yida.lucene.core.Source;
import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.repository.hightlight.HighlightRender;
import com.yida.lucene.repository.query.Page;
import com.yida.lucene.repository.query.PageQuery;
import com.yida.lucene.repository.query.Querys;
import com.yida.lucene.repository.query.Sorts;
import com.yida.lucene.repository.query.wrapper.LambdaQueryChainWrapper;
import com.yida.lucene.repository.query.wrapper.LambdaUpdateChainWrapper;
import com.yida.lucene.repository.query.wrapper.QueryWrapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author yida
 * @date 2024/09/23 4:48
 */
public interface ElRepository<T> {
	TopDocs EMPTY_TOP_DOCS = new TopDocs(null, null);
	Future<Integer> DONE_FUTURE = new Future<Integer>() {
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public Integer get() {
			return 0;
		}

		@Override
		public Integer get(long timeout, TimeUnit unit) {
			return 0;
		}
	};

	/**
	 * 插入一条记录
	 *
	 * @param entity 实体对象
	 * @return Future<Integer> 影响行数
	 */
	Future<Integer> insertFuture(T entity);

	/**
	 * 插入多条记录
	 *
	 * @param entity 实体对象
	 * @return Future<Integer> 影响行数
	 */
	Future<Integer> insertFuture(Collection<T> entity);

	/**
	 * 根据 ID 修改
	 *
	 * @param entity 实体对象
	 * @return Future<Long> 影响行数
	 */
	Future<Integer> updateFuture(T entity);

	/**
	 * 根据 ID 修改
	 *
	 * @param entity 实体对象
	 * @return Future<Integer> 影响行数
	 */
	Future<Integer> updateFuture(Collection<T> entity);

	/**
	 * 删除
	 *
	 * @param queries queries
	 * @return Future<Integer> 影响行数
	 */
	Future<Integer> deleteFuture(Query... queries);

	/**
	 * 插入一条记录
	 *
	 * @param entity 实体对象
	 */
	void insert(T entity);

	/**
	 * 插入多条记录
	 *
	 * @param entity 实体对象
	 */
	void insert(Collection<T> entity);

	/**
	 * 根据 ID 修改
	 *
	 * @param entity 实体对象
	 */
	void update(T entity);

	/**
	 * 根据 ID 修改
	 *
	 * @param entity 实体对象
	 */
	void update(Collection<T> entity);

	/**
	 * 删除
	 *
	 * @param queries queries
	 */
	void delete(Query... queries);

	/**
	 * 原生查询
	 *
	 * @param query query
	 * @param sort  sort
	 * @param topK  topK
	 * @return TopDocs
	 */
	TopDocs search(Query query, Sort sort, int topK);

	/**
	 * @param queryKeyword
	 * @param queryFields
	 * @param fieldBoostMap
	 * @param sort
	 * @description 根据关键词进行Lucene搜索
	 * @author yida
	 * @date 2024-09-14 15:29:55
	 */
	TopDocs searchByKeyword(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap, Sort sort);

	/**
	 * @param queryKeyword
	 * @param queryFields
	 * @param sort
	 * @return {@link TopDocs}
	 * @description 根据关键词进行Lucene搜索
	 * @author yida
	 * @date 2024-09-14 15:29:55
	 */
	TopDocs searchByKeyword(String queryKeyword, String[] queryFields, Sort sort);

	/**
	 * @param queryKeyword
	 * @param queryFields
	 * @param fieldBoostMap
	 * @return {@link TopDocs}
	 * @description 根据关键词进行Lucene搜索
	 * @author yida
	 * @date 2024-09-14 15:29:55
	 */
	TopDocs searchByKeyword(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap);

	/**
	 * @param queryKeyword
	 * @param queryFields
	 * @return {@link TopDocs}
	 * @description 根据关键词进行Lucene搜索
	 * @author yida
	 * @date 2024-09-14 15:29:55
	 */
	TopDocs searchByKeyword(String queryKeyword, String[] queryFields);

	/**
	 * @param queryKeyword
	 * @param queryField
	 * @return {@link TopDocs}
	 * @description 根据关键词进行Lucene搜索
	 * @author yida
	 * @date 2024-09-14 15:29:55
	 */
	TopDocs searchByKeyword(String queryKeyword, String queryField);

	/**
	 * 获取文档资源
	 *
	 * @return Source
	 */
	Source<T> getSource();

	/**
	 * 链式调用
	 *
	 * @return ChainWrapper
	 */
	default LambdaQueryChainWrapper<T> lambdaQuery() {
		return new LambdaQueryChainWrapper<>(this);
	}

	/**
	 * 链式调用
	 *
	 * @return ChainWrapper
	 */
	default LambdaUpdateChainWrapper<T> lambdaUpdate() {
		return new LambdaUpdateChainWrapper<>(this);
	}


	/**
	 * 创建代理对象
	 *
	 * @param source source
	 * @param <T>    T
	 * @return 代理对象
	 */
	@SuppressWarnings("unchecked")
	static <T> ElRepository<T> get(Source<T> source) {
		ElRepository<T> target = (ElRepository<T>) source.getRepositoryInterceptor().plugin(new ElRepositoryImpl<>(source));
		return new ElRepositoryDelegate<>(target, source.getTxManager());
	}

	/**
	 * 包装异常
	 *
	 * @param supplier supplier
	 * @param <R>      R
	 * @return R
	 */
	default <R> R warpResultWithException(SupplierWhitException<R> supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw EmbeddedLuceneException.of(e);
		}
	}

	@FunctionalInterface
	interface SupplierWhitException<T> {

		/**
		 * Gets a result.
		 *
		 * @return a result
		 * @throws Exception any exception
		 */
		T get() throws Exception;
	}

	default List<T> getJavaBean(ScoreDoc[] scoreDoc, HighlightRender highlightRender, Set<String> selectFieldSet) throws IOException {
		if (null == scoreDoc || scoreDoc.length == 0) {
			return Collections.emptyList();
		}
		List<T> res = new ArrayList<>(scoreDoc.length);
		Source<T> source = getSource();
		for (ScoreDoc doc : scoreDoc) {
			res.add(source.getJavaBean(doc.doc, highlightRender, selectFieldSet));
		}
		return res;
	}

	default List<ElDocument<T>> getDocument(ScoreDoc[] scoreDoc, HighlightRender highlightRender, Set<String> selectFieldSet) throws IOException {
		if (null == scoreDoc || scoreDoc.length == 0) {
			return Collections.emptyList();
		}
		List<ElDocument<T>> res = new ArrayList<>(scoreDoc.length);
		Source<T> source = getSource();
		for (ScoreDoc doc : scoreDoc) {
			res.add(source.getElDocument(doc.doc, highlightRender, selectFieldSet));
		}
		return res;
	}

	/**
	 * 根据实体(ID)删除
	 *
	 * @param entity 实体对象
	 * @return Future<Integer> 影响行数
	 */
	default Future<Integer> deleteFuture(T entity) {
		return deleteFuture(getSource().getDocFactory().getIdQuery(entity));
	}

	/**
	 * 删除（根据ID或实体 批量删除）
	 *
	 * @param idList 主键ID列表或实体列表(不能为 null 以及 empty)
	 * @return Future<Integer> 影响行数
	 */
	default Future<Integer> deleteByIdsFuture(Collection<? extends Serializable> idList) {
		if (idList == null || idList.isEmpty()) {
			return DONE_FUTURE;
		}
		DocFactory<T> docFactory = getSource().getDocFactory();
		Query[] queries = idList.stream()
				.map(docFactory::getIdQuery)
				.toArray(Query[]::new);
		return deleteFuture(queries);
	}

	/**
	 * 根据实体wrapper删除
	 *
	 * @param wrapper wrapper
	 * @return Future<Integer> 影响行数
	 */
	default Future<Integer> deleteFuture(QueryWrapper<T> wrapper) {
		return deleteFuture(wrapper.getQuery());
	}

	/**
	 * 根据 ID 删除
	 *
	 * @param id 主键ID
	 * @return Future<Integer> 影响行数
	 */
	default Future<Integer> deleteFuture(Serializable id) {
		return deleteFuture(select(id));
	}

	/**
	 * 删除
	 *
	 * @return Future<Integer> 影响行数
	 */
	default Future<Integer> deleteFuture() {
		return deleteFuture(Querys.MATCH_ALL_QUERY);
	}

	/**
	 * 删除（根据ID或实体 批量删除）
	 *
	 * @param entities entities
	 */
	default Future<Integer> deleteFuture(Collection<T> entities) {
		if (entities == null || entities.isEmpty()) {
			return DONE_FUTURE;
		}
		DocFactory<T> docFactory = getSource().getDocFactory();
		Query[] queries = entities.stream()
				.map(docFactory::getIdQuery)
				.toArray(Query[]::new);
		return deleteFuture(queries);
	}

	/**
	 * 根据实体(ID)删除
	 *
	 * @param entity 实体对象
	 */
	default void delete(T entity) {
		delete(getSource().getDocFactory().getIdQuery(entity));
	}

	/**
	 * 删除（根据ID或实体 批量删除）
	 *
	 * @param entities entities
	 */
	default void delete(Collection<T> entities) {
		if (entities == null || entities.isEmpty()) {
			return;
		}
		DocFactory<T> docFactory = getSource().getDocFactory();
		Query[] queries = entities.stream()
				.map(docFactory::getIdQuery)
				.toArray(Query[]::new);
		delete(queries);
	}

	/**
	 * 根据 ID 删除
	 *
	 * @param id 主键ID
	 */
	default void delete(Serializable id) {
		delete(getSource().getDocFactory().getIdQuery(id));
	}

	/**
	 * 删除（根据ID或实体 批量删除）
	 *
	 * @param idList 主键ID列表或实体列表(不能为 null 以及 empty)
	 */
	default void deleteByIds(Collection<? extends Serializable> idList) {
		if (idList == null || idList.isEmpty()) {
			return;
		}
		DocFactory<T> docFactory = getSource().getDocFactory();
		Query[] queries = idList.stream()
				.map(docFactory::getIdQuery)
				.toArray(Query[]::new);
		delete(queries);
	}

	/**
	 * 删除
	 */
	default void delete() {
		delete(Querys.MATCH_ALL_QUERY);
	}

	/**
	 * 根据实体wrapper删除
	 *
	 * @param wrapper wrapper
	 */
	default void delete(QueryWrapper<T> wrapper) {
		delete(wrapper.getQuery());
	}

	/**
	 * 原生查询
	 *
	 * @param query           query
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<T> selectList(Query query, Sort sort, int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return warpResultWithException(() -> {
			final Source<T> source = getSource();
			TopDocs topDocs = search(query, sort, topK);
			if (topDocs.equals(EMPTY_TOP_DOCS)) {
				return Collections.emptyList();
			}
			if (topDocs.scoreDocs.length == 0) {
				return Collections.emptyList();
			} else {
				HighlightRender.init(highlightRender, query, source.getAnalyzer());
				return getJavaBean(topDocs.scoreDocs, highlightRender, selectFieldSet);
			}
		});
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryField      queryField
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<T> selectList(String queryKeyword, String queryField, Sort sort,
							   int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectList(queryKeyword, new String[]{queryField}, sort, topK, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<T> selectList(String queryKeyword, String[] queryFields, Sort sort,
							   int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectList(queryKeyword, queryFields, null, sort, topK, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param fieldBoostMap   fieldBoostMap
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<T> selectList(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap, Sort sort,
							   int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		final Source<T> source = getSource();
		Analyzer analyzer = source.getAnalyzer();
		QueryParser queryParser = new MultiFieldQueryParser(queryFields, analyzer, fieldBoostMap);
		try {
			Query query = queryParser.parse(queryKeyword);
			return selectList(query, sort, topK, highlightRender, selectFieldSet);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryField      queryField
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<ElDocument<T>> selectDocList(String queryKeyword, String queryField,
											  Sort sort, int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectDocList(queryKeyword, new String[]{queryField}, sort, topK, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<ElDocument<T>> selectDocList(String queryKeyword, String[] queryFields,
											  Sort sort, int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectDocList(queryKeyword, queryFields, null, sort, topK, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param fieldBoostMap   fieldBoostMap
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<ElDocument<T>> selectDocList(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap,
											  Sort sort, int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		final Source<T> source = getSource();
		Analyzer analyzer = source.getAnalyzer();
		QueryParser queryParser = new MultiFieldQueryParser(queryFields, analyzer, fieldBoostMap);
		try {
			Query query = queryParser.parse(queryKeyword);
			return selectDocList(query, sort, topK, highlightRender, selectFieldSet);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	/**
	 * 原生查询
	 *
	 * @param query           query
	 * @param sort            sort
	 * @param topK            topK
	 * @param highlightRender highlightRender
	 * @return List
	 */
	default List<ElDocument<T>> selectDocList(Query query, Sort sort, int topK, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return warpResultWithException(() -> {
			final Source<T> source = getSource();
			TopDocs topDocs = search(query, sort, topK);
			if (topDocs.equals(EMPTY_TOP_DOCS)) {
				return Collections.emptyList();
			}
			if (topDocs.scoreDocs.length == 0) {
				return Collections.emptyList();
			} else {
				HighlightRender.init(highlightRender, query, source.getAnalyzer());
				return getDocument(topDocs.scoreDocs, highlightRender, selectFieldSet);
			}
		});
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryField      queryField
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<T> selectPage(String queryKeyword, String queryField, PageQuery pageQuery,
							   Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectPage(queryKeyword, new String[]{queryField}, pageQuery, sort, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<T> selectPage(String queryKeyword, String[] queryFields, PageQuery pageQuery,
							   Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectPage(queryKeyword, queryFields, null, pageQuery, sort, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param fieldBoostMap   fieldBoostMap
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<T> selectPage(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap, PageQuery pageQuery,
							   Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		final Source<T> source = getSource();
		Analyzer analyzer = source.getAnalyzer();
		QueryParser queryParser = new MultiFieldQueryParser(queryFields, analyzer, fieldBoostMap);
		try {
			Query query = queryParser.parse(queryKeyword);
			return selectPage(query, pageQuery, sort, highlightRender, selectFieldSet);
		} catch (Exception e) {
			return Page.of(Collections.emptyList(), 0, 0, 10, 1);
		}
	}


	/**
	 * 原生查询
	 *
	 * @param query           query
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<T> selectPage(Query query, PageQuery pageQuery, Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		if (null == pageQuery) {
			pageQuery = new PageQuery();
		}
		PageQuery finalPageQuery = pageQuery;
		return warpResultWithException(() -> {
			final int current = finalPageQuery.getCurrent();
			final int size = finalPageQuery.getSize();

			final Source<T> source = getSource();
			IndexSearcher searcher = source.getSearchHolder().getIndexSearcher();
			TotalHitCountCollector collector = Querys.totalHitsCollector();
			searcher.search(query, collector);

			long total = collector.getTotalHits();
			int totalPage = Math.toIntExact(total / size) + (total % size > 0 ? 1 : 0);

			if ((long) (current - 1) * size > total) {
				return Page.of(Collections.emptyList(), total, totalPage, size, current);
			}

			TopDocs topDocs = search(query, sort, current * size);
			if (topDocs.equals(EMPTY_TOP_DOCS)) {
				return Page.of(Collections.emptyList(), total, totalPage, size, current);
			}

			int fromIndex = (current - 1) * size;
			int toIndex = fromIndex + size;
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			if (null != scoreDocs && scoreDocs.length != 0) {
				scoreDocs = Arrays.copyOfRange(scoreDocs, fromIndex, Math.min(toIndex, (int) total));
			}
			HighlightRender.init(highlightRender, query, source.getAnalyzer());
			List<T> records = getJavaBean(scoreDocs, highlightRender, selectFieldSet);
			return Page.of(records, total, totalPage, size, current);
		});
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryField      queryField
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<ElDocument<T>> selectDocPage(String queryKeyword, String queryField,
											  PageQuery pageQuery, Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectDocPage(queryKeyword, new String[]{queryField}, pageQuery, sort, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<ElDocument<T>> selectDocPage(String queryKeyword, String[] queryFields,
											  PageQuery pageQuery, Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return selectDocPage(queryKeyword, queryFields, null, pageQuery, sort, highlightRender, selectFieldSet);
	}

	/**
	 * 原生查询
	 *
	 * @param queryKeyword    queryKeyword
	 * @param queryFields     queryFields
	 * @param fieldBoostMap   fieldBoostMap
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<ElDocument<T>> selectDocPage(String queryKeyword, String[] queryFields, Map<String, Float> fieldBoostMap,
											  PageQuery pageQuery, Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		final Source<T> source = getSource();
		Analyzer analyzer = source.getAnalyzer();
		QueryParser queryParser = new MultiFieldQueryParser(queryFields, analyzer, fieldBoostMap);
		try {
			Query query = queryParser.parse(queryKeyword);
			return selectDocPage(query, pageQuery, sort, highlightRender, selectFieldSet);
		} catch (Exception e) {
			return Page.of(Collections.emptyList(), 0, 0, 10, 1);
		}
	}

	/**
	 * 原生查询
	 *
	 * @param query           query
	 * @param pageQuery       pageQuery
	 * @param sort            sort
	 * @param highlightRender highlightRender
	 * @return Page
	 */
	default Page<ElDocument<T>> selectDocPage(Query query, PageQuery pageQuery, Sort sort, HighlightRender highlightRender, Set<String> selectFieldSet) {
		if (null == pageQuery) {
			pageQuery = new PageQuery();
		}
		PageQuery finalPageQuery = pageQuery;
		return warpResultWithException(() -> {
			final int current = finalPageQuery.getCurrent();
			final int size = finalPageQuery.getSize();

			final Source<T> source = getSource();
			IndexSearcher searcher = source.getSearchHolder().getIndexSearcher();
			TotalHitCountCollector collector = Querys.totalHitsCollector();
			searcher.search(query, collector);

			long total = collector.getTotalHits();
			int totalPage = Math.toIntExact(total / size) + (total % size > 0 ? 1 : 0);

			if ((long) (current - 1) * size > total) {
				return Page.of(Collections.emptyList(), total, totalPage, size, current);
			}
			TopDocs topDocs = search(query, sort, current * size);
			if (topDocs.equals(EMPTY_TOP_DOCS)) {
				return Page.of(Collections.emptyList(), total, totalPage, size, current);
			}

			int fromIndex = (current - 1) * size;
			int toIndex = fromIndex + size;
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			if (null != scoreDocs && scoreDocs.length != 0) {
				scoreDocs = Arrays.copyOfRange(scoreDocs, fromIndex, Math.min(toIndex, (int) total));
			}
			HighlightRender.init(highlightRender, query, source.getAnalyzer());
			List<ElDocument<T>> records = getDocument(scoreDocs, highlightRender, selectFieldSet);
			return Page.of(records, total, totalPage, size, current);
		});
	}

	/**
	 * 根据 ID 查询
	 *
	 * @param id 主键ID
	 * @return T
	 */
	default T select(Serializable id) {
		List<T> res = selectList(getSource().getDocFactory().getIdQuery(id), null, 1, null, null);
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}
		return null;
	}

	/**
	 * 条件包装器查询
	 *
	 * @param wrapper wrapper
	 * @return one
	 */
	default T select(QueryWrapper<T> wrapper) {
		List<T> res = selectList(wrapper.getQuery(), wrapper.getSort(), 1, wrapper.getHighlightRender(), wrapper.getSelect());
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}
		return null;
	}

	/**
	 * 根据 ID 查询
	 *
	 * @param id 主键ID
	 * @return T
	 */
	default ElDocument<T> selectDoc(Serializable id) {
		List<ElDocument<T>> res = selectDocList(getSource().getDocFactory().getIdQuery(id), null, 1, null, null);
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}
		return null;
	}

	/**
	 * 条件包装器查询
	 *
	 * @param wrapper wrapper
	 * @return one
	 */
	default ElDocument<T> selectDoc(QueryWrapper<T> wrapper) {
		List<ElDocument<T>> res = selectDocList(wrapper.getQuery(), wrapper.getSort(), 1, wrapper.getHighlightRender(), wrapper.getSelect());
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}
		return null;
	}

	/**
	 * 查询（根据ID 批量查询）
	 *
	 * @param idList 主键ID列表(不能为 null 以及 empty)
	 * @return List
	 */
	default List<T> selectList(Collection<? extends Serializable> idList) {
		if (idList == null || idList.isEmpty()) {
			return Collections.emptyList();
		}
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		idList.stream()
				.map(id -> getSource().getDocFactory().getIdQuery(id))
				.forEach(query -> builder.add(query, BooleanClause.Occur.SHOULD));
		return selectList(builder.build(), null, idList.size(), null, null);
	}

	/**
	 * 查询（根据ID 批量查询）
	 *
	 * @param idList 主键ID列表(不能为 null 以及 empty)
	 * @return List
	 */
	default List<ElDocument<T>> selectDocList(Collection<? extends Serializable> idList) {
		if (idList == null || idList.isEmpty()) {
			return Collections.emptyList();
		}
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		idList.stream()
				.map(id -> getSource().getDocFactory().getIdQuery(id))
				.forEach(query -> builder.add(query, BooleanClause.Occur.SHOULD));
		return selectDocList(builder.build(), null, idList.size(), null, null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @return list
	 */
	default List<T> selectList() {
		return selectList(Querys.MATCH_ALL_QUERY, null, -1, null, null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @return list
	 */
	default List<ElDocument<T>> selectDocList() {
		return selectDocList(Querys.MATCH_ALL_QUERY, null, -1, null, null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param wrapper wrapper
	 * @return list
	 */
	default List<T> selectList(QueryWrapper<T> wrapper) {
		wrapper.setSource(getSource());
		return selectList(wrapper.getQuery(), wrapper.getSort(), wrapper.getLimit(), wrapper.getHighlightRender(), wrapper.getSelect());
	}

	/**
	 * 条件包装器查询
	 *
	 * @param wrapper wrapper
	 * @return list
	 */
	default List<ElDocument<T>> selectDocList(QueryWrapper<T> wrapper) {
		wrapper.setSource(getSource());
		return selectDocList(wrapper.getQuery(), wrapper.getSort(), wrapper.getLimit(), wrapper.getHighlightRender(), wrapper.getSelect());
	}

	/**
	 * 条件包装器查询
	 *
	 * @param wrapper   wrapper
	 * @param pageQuery pageQuery
	 * @return page
	 */
	default Page<T> selectPage(QueryWrapper<T> wrapper, PageQuery pageQuery) {
		wrapper.setSource(getSource());
		return selectPage(wrapper.getQuery(), pageQuery, wrapper.getSort(), wrapper.getHighlightRender(), wrapper.getSelect());
	}

	/**
	 * 条件包装器查询
	 *
	 * @param wrapper   wrapper
	 * @param pageQuery pageQuery
	 * @return page
	 */
	default Page<ElDocument<T>> selectDocPage(QueryWrapper<T> wrapper, PageQuery pageQuery) {
		wrapper.setSource(getSource());
		return selectDocPage(wrapper.getQuery(), pageQuery, wrapper.getSort(), wrapper.getHighlightRender(), wrapper.getSelect());
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery pageQuery
	 * @return page
	 */
	default Page<T> selectPage(PageQuery pageQuery) {
		return selectPage(pageQuery, Sorts.empty(), null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery pageQuery
	 * @return page
	 */
	default Page<ElDocument<T>> selectDocPage(PageQuery pageQuery) {
		return selectDocPage(pageQuery, Sorts.empty(), null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery pageQuery
	 * @param sorts     sorts
	 * @return page
	 */
	default Page<T> selectPage(PageQuery pageQuery, Sorts<T> sorts) {
		return selectPage(pageQuery, sorts, null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery pageQuery
	 * @param sorts     sorts
	 * @return page
	 */
	default Page<ElDocument<T>> selectDocPage(PageQuery pageQuery, Sorts<T> sorts) {
		return selectDocPage(pageQuery, sorts, null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery       pageQuery
	 * @param highlightRender highlightRender
	 * @return page
	 */
	default Page<T> selectPage(PageQuery pageQuery, HighlightRender highlightRender) {
		return selectPage(pageQuery, Sorts.empty(), highlightRender);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery       pageQuery
	 * @param highlightRender highlightRender
	 * @return page
	 */
	default Page<ElDocument<T>> selectDocPage(PageQuery pageQuery, HighlightRender highlightRender) {
		return selectDocPage(pageQuery, Sorts.empty(), highlightRender);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery       pageQuery
	 * @param sorts           sorts
	 * @param highlightRender highlightRender
	 * @return page
	 */
	default Page<T> selectPage(PageQuery pageQuery, Sorts<T> sorts, HighlightRender highlightRender) {
		return selectPage(Querys.MATCH_ALL_QUERY, pageQuery, sorts.getSort(), highlightRender, null);
	}

	/**
	 * 条件包装器查询
	 *
	 * @param pageQuery       pageQuery
	 * @param sorts           sorts
	 * @param highlightRender highlightRender
	 * @return page
	 */
	default Page<ElDocument<T>> selectDocPage(PageQuery pageQuery, Sorts<T> sorts, HighlightRender highlightRender) {
		return selectDocPage(Querys.MATCH_ALL_QUERY, pageQuery, sorts.getSort(), highlightRender, null);
	}

}
