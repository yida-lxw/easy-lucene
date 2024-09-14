package com.yida.lucene.core;

import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.repository.RepositoryInterceptor;
import com.yida.lucene.transaction.ElTransactionManager;
import com.yida.lucene.transaction.XaSource;
import com.yida.lucene.transaction.XaSourcePool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yida
 * @date 2024/8/4 2:27
 */
@Slf4j
public class EmbeddedLucene extends SourceFactory implements XaSourcePool, Closeable {

	public static EmbeddedLucene create(EmbeddedLuceneConfig config) {
		return new EmbeddedLucene(config);
	}

	private ExecutorService worker;
	private final Map<Class<?>, Source<?>> sources = new ConcurrentHashMap<>();
	private final Map<Class<?>, XaSource<?>> xaSources = new ConcurrentHashMap<>();

	@Getter
	private ElTransactionManager txManager;

	private final EmbeddedLuceneConfig config;
	private volatile boolean started = false;
	private volatile boolean stopped = false;

	protected EmbeddedLucene(EmbeddedLuceneConfig config) {
		this.config = config;
	}

	public synchronized void start() {
		log.debug("Starting EmbeddedLucene...");

		if (started) {
			throw new IllegalStateException("EmbeddedLucene has started");
		}

		txManager = new ElTransactionManager(this);

		// create source
		buildSource();

		worker = config.getWorker();

		// register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(this::close));

		started = true;

		log.debug("EmbeddedLucene started successfully");
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void buildSource() {
		RepositoryInterceptor repositoryInterceptor = new RepositoryInterceptor();
		repositoryInterceptor.setHandlers(config.getRepositoryHandlers());
		config.getDocNameMap().keySet().stream()
				.map(docClass -> {
					// doc class must need no args constructor
					Constructor constructor;
					try {
						constructor = docClass.getConstructor();
					} catch (NoSuchMethodException e) {
						throw EmbeddedLuceneException.of(e);
					}
					DocFactory<?> docFactory = DocFactory.getDocFactory(docClass, constructor);

					Map<Class<?>, Analyzer> classAnalyzerMap = config.getClassAnalyzerMap();
					Analyzer analyzer = classAnalyzerMap.get(docClass);
					if (Objects.isNull(analyzer)) {
						// 根据FieldType组装Analyzer
						analyzer = docFactory.getAnalyzer(config.getFieldTypeAnalyzerMap());
					}

					return this.createSource(
							docFactory,
							config.getIndexPath(),
							config.getDocNameMap().get(docClass),
							analyzer,
							repositoryInterceptor,
							txManager
					);
				})
				.forEach(source -> sources.put(source.getDocClass(), source));
	}

	@Override
	public ExecutorService getExecutor() {
		return worker;
	}

	@Override
	public Set<Class<?>> getLoadedDocClass() {
		return sources.keySet();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Source<T> getSource(Class<T> docClass) {
		return (Source<T>) sources.get(docClass);
	}

	@Override
	@SuppressWarnings("all")
	public synchronized void close() {
		if (!started || stopped) {
			return;
		}
		try {
			worker.shutdown();
			worker.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException ignored) {
		}
		sources.forEach((k, v) -> {
			IndexWriter indexWriter = v.getWriter();
			try {
				indexWriter.close();
			} catch (IOException e) {
				throw EmbeddedLuceneException.of(e);
			}
		});
		sources.clear();
		stopped = true;
	}

	@Override
	public Collection<XaSource<?>> getXaSources() {
		return xaSources.values();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> XaSource<T> getXaSource(Class<T> docClass) {
		XaSource<T> res = (XaSource<T>) xaSources.get(docClass);
		if (Objects.isNull(res)) {
			res = new XaSource<>((Source<T>) sources.get(docClass));
			xaSources.put(docClass, res);
		}
		return res;
	}


}
