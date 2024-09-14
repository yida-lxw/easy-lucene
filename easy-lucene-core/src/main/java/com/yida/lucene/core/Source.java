package com.yida.lucene.core;

import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.repository.RepositoryInterceptor;
import com.yida.lucene.repository.hightlight.HighlightRender;
import com.yida.lucene.transaction.ElTransactionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 核心资源上下文
 *
 * @author yida
 * @date 2024/8/4 9:24
 */
@Slf4j
public class Source<T> {

	private final Path indexPath;

	@Getter
	private final Analyzer analyzer;

	private final SourceFactory factory;

	@Getter
	private final DocFactory<T> docFactory;

	@Getter
	private final Class<T> docClass;

	@Getter
	private final RepositoryInterceptor repositoryInterceptor;

	@Getter
	private final ElTransactionManager txManager;

	@Getter
	private volatile IndexWriter writer;

	private volatile SearchHolder searchHolder;
	private final Lock updateLock = new ReentrantLock();
	private static final ScheduledExecutorService CLOSE_THREAD = Executors.newSingleThreadScheduledExecutor();

	Source(Class<T> docClass,
		   Path indexPath,
		   Analyzer analyzer,
		   SourceFactory factory,
		   DocFactory<T> docFactory,
		   RepositoryInterceptor repositoryInterceptor,
		   ElTransactionManager txManager
	) {
		this.docClass = docClass;
		this.indexPath = indexPath;
		this.analyzer = analyzer;

		this.factory = factory;
		IndexWriter writer = factory.createWriter(indexPath, analyzer);
		this.writer = writer;
		this.searchHolder = new SearchHolder(writer);

		this.docFactory = docFactory;
		this.repositoryInterceptor = repositoryInterceptor;
		this.txManager = txManager;
	}

	public void commit() {
		updateLock.lock();
		try {
			if (writer.hasUncommittedChanges()) {
				writer.flush();
				writer.commit();
				searchHolder.destroy();
			}
		} catch (IOException e) {
			throw EmbeddedLuceneException.of(e);
		} finally {
			updateLock.unlock();
		}
	}

	public void rollback() {
		updateLock.lock();
		try {
			if (writer.hasUncommittedChanges()) {
				writer.rollback();
				IndexWriter oldWriter = writer;
				SearchHolder oldSearcher = searchHolder;

				writer = factory.createWriter(indexPath, analyzer);
				searchHolder = new SearchHolder(writer);

				oldWriter.close();
				oldSearcher.destroy();
			}
		} catch (IOException e) {
			throw EmbeddedLuceneException.of(e);
		} finally {
			updateLock.unlock();
		}
	}

	public T getJavaBean(int doc, HighlightRender highlightRender, Set<String> selectFieldSet) throws IOException {
		if (null == selectFieldSet || selectFieldSet.isEmpty()) {
			return docFactory.toJavaBean(searchHolder.getIndexSearcher().doc(doc), highlightRender);
		} else {
			return docFactory.toJavaBean(searchHolder.getIndexSearcher().doc(doc, selectFieldSet), highlightRender);
		}
	}

	public ElDocument<T> getElDocument(int doc, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return docFactory.getElDocument(doc, this, highlightRender, selectFieldSet);
	}

	public SearchHolder getSearchHolder() {
		return searchHolder;
	}

	/**
	 * IndexSearcher创建过程有一些计算量，实际上大部分的IndexSearcher不会真的被使用，所以这里懒加载一下
	 */
	public class SearchHolder {
		private final IndexWriter writer;
		private IndexSearcher indexSearcher;

		public SearchHolder(IndexWriter writer) {
			this.writer = writer;
		}

		public synchronized IndexSearcher getIndexSearcher() {
			if (indexSearcher != null) {
				return indexSearcher;
			}
			indexSearcher = factory.createSearcher(writer);
			return indexSearcher;
		}

		public synchronized void destroy() throws IOException {
			if (indexSearcher == null) {
				return;
			}
			IndexReader indexReader = indexSearcher.getIndexReader();
			indexSearcher = null;
			// 这个indexSearcher可能在其他地方还被拿着使用，所以我们延迟10s关闭它
			CLOSE_THREAD.schedule(() -> {
				try {
					indexReader.close();
				} catch (IOException e) {
					log.error("close indexReader failed", e);
				}
			}, 10, TimeUnit.SECONDS);
		}
	}

}