package com.yida.lucene.core;

import com.yida.lucene.constant.FieldType;
import com.yida.lucene.exception.ElAssert;
import com.yida.lucene.repository.handler.RepositoryHandler;
import lombok.Getter;
import org.apache.lucene.analysis.Analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yida
 * @date 2024/8/26 10:02
 */
@Getter
public class EmbeddedLuceneConfig {

	private File indexPath;
	private ExecutorService worker;
	private final Map<Class<?>, String> docNameMap = new HashMap<>();
	private final Map<Class<?>, Analyzer> classAnalyzerMap = new HashMap<>();
	private final Map<FieldType, Analyzer> fieldTypeAnalyzerMap = new HashMap<>();
	private final List<RepositoryHandler> repositoryHandlers = new ArrayList<>();

	private EmbeddedLuceneConfig() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final EmbeddedLuceneConfig target = new EmbeddedLuceneConfig();
		private int threadNum = Runtime.getRuntime().availableProcessors();

		public Builder workerThreadNum(int threadNum) {
			this.threadNum = threadNum;
			return this;
		}

		public Builder indexPath(String indexPath) {
			target.indexPath = new File(indexPath);
			return this;
		}

		public Builder registerSource(Class<?> docClass) {
			target.docNameMap.put(docClass, docClass.getName());
			return this;
		}

		public Builder registerSource(Class<?> docClass, String docName) {
			target.docNameMap.put(docClass, docName);
			return this;
		}

		public Builder registerSource(Class<?> docClass, Analyzer analyzer) {
			target.docNameMap.put(docClass, docClass.getName());
			target.classAnalyzerMap.put(docClass, analyzer);
			return this;
		}

		public Builder registerSource(Class<?> docClass, String docName, Analyzer analyzer) {
			target.docNameMap.put(docClass, docName);
			target.classAnalyzerMap.put(docClass, analyzer);
			return this;
		}

		public Builder fieldTypeAnalyzer(FieldType fieldType, Analyzer analyzer) {
			target.fieldTypeAnalyzerMap.put(fieldType, analyzer);
			return this;
		}

		public Builder repositoryHandler(List<RepositoryHandler> handlers) {
			target.repositoryHandlers.addAll(handlers);
			return this;
		}

		public Builder repositoryHandler(RepositoryHandler handler) {
			target.repositoryHandlers.add(handler);
			return this;
		}

		public EmbeddedLuceneConfig build() {
			ElAssert.nonNull(target.indexPath, "indexPath cannot be empty");
			target.fillDefaultAnalyzer();
			target.worker = target.createWorker(threadNum);
			return target;
		}
	}

	private ExecutorService createWorker(int threadNum) {
		return new ThreadPoolExecutor(
				threadNum,
				threadNum,
				0L,
				TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(threadNum * 50),
				new DaemonNamedThreadFactory("Embedded-Lucene"),
				new ThreadPoolExecutor.CallerRunsPolicy()
		);
	}

	private void fillDefaultAnalyzer() {
		for (FieldType fieldType : FieldType.values()) {
			Analyzer analyzer = fieldTypeAnalyzerMap.get(fieldType);
			if (Objects.isNull(analyzer)) {
				fieldTypeAnalyzerMap.put(fieldType, DocFactory.DEFAULT_ANALYZER);
			}
		}
	}

	/**
	 * 守护线程工厂
	 */
	private static final class DaemonNamedThreadFactory implements ThreadFactory {
		private static final AtomicInteger THREAD_POOL_NUMBER = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private static final String NAME_PATTERN = "%s-%d-thread";
		private final String threadNamePrefix;

		public DaemonNamedThreadFactory(String threadNamePrefix) {
			SecurityManager s = System.getSecurityManager();
			this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.threadNamePrefix = String.format(Locale.ROOT, NAME_PATTERN, checkPrefix(threadNamePrefix), THREAD_POOL_NUMBER.getAndIncrement());
		}

		private static String checkPrefix(String prefix) {
			return prefix != null && prefix.length() != 0 ? prefix : "Lucene";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(this.group, r, String.format(Locale.ROOT, "%s-%d", this.threadNamePrefix, this.threadNumber.getAndIncrement()), 0L);
			t.setDaemon(true);
			t.setPriority(5);
			return t;
		}
	}

}
