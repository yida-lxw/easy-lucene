package com.yida.lucene.core;

import com.yida.lucene.repository.RepositoryInterceptor;
import com.yida.lucene.transaction.ElTransactionManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author yida
 * @date 2024/8/4 9:31
 */
@Slf4j
public abstract class SourceFactory {


	/**
	 * 获取已加载的文档类
	 *
	 * @return Set
	 */
	public abstract Set<Class<?>> getLoadedDocClass();

	/**
	 * 根据文档类获取Source
	 *
	 * @param docClass 文档类型
	 * @param <T>      T
	 * @return Source
	 */
	public abstract <T> Source<T> getSource(Class<T> docClass);

	@SuppressWarnings("all")
	public final <T> Source<T> createSource(
			DocFactory<T> docFactory,
			File indexPath,
			String docName,
			Analyzer analyzer,
			RepositoryInterceptor repositoryInterceptor,
			ElTransactionManager txManager
	) {
		Class<T> docClass = docFactory.getDocClass();
		log.debug("Creating Source bind from class : {}", docClass.getName());

		File doc = new File(indexPath, docName);
		if (!doc.exists()) {
			doc.mkdirs();
		}
		Path path = doc.toPath();
		return new Source<>(
				docClass,
				path,
				analyzer,
				this,
				docFactory,
				repositoryInterceptor,
				txManager
		);
	}

	/**
	 * 获取工作线程
	 *
	 * @return Executor
	 */
	public abstract ExecutorService getExecutor();

	@SneakyThrows
	public final IndexSearcher createSearcher(IndexWriter writer) {
		return new IndexSearcher(DirectoryReader.open(writer), getExecutor());
	}

	@SneakyThrows
	public final IndexWriter createWriter(Path indexPath, Analyzer analyzer) {
		return new IndexWriter(
				getFsDirectory(indexPath),
				new IndexWriterConfig(analyzer)
						.setCommitOnClose(false)
						.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
						.setInfoStream(LoggerInfoStreamAdapter.INSTANCE)
		);
	}

	/**
	 * 1、SimpleFSDirectory
	 * 最简单的FSDirectory子类，使用java.io.*API将文件存入文件系统中，不能很好支持多线程操作。
	 * 因为要做到这点就必须在内部加入锁，而java.io.*并不支持按位置读取。
	 * <p>
	 * 2、NIOFSDirectory
	 * 使用java.io.*API所提供的位置读取接口，能很好的支持除Windows之外的多线程操作，原因是Sun的JRE在Windows平台上长期存在问题。
	 * NIOFSDirectory在Windows操作系统的性能比较差，甚至可能比SimpleFSDirecory的性能还差。
	 * <p>
	 * 3、MmapDirectory
	 * 使用内存映射的I/O接口进行读操作，这样不需要采取锁机制，并能很好的支持多线程读操作。
	 * 但由于内存映射的I/O所消耗的地址空间是与索引尺寸相等，所以建议最好只是用64位JRE。
	 *
	 * @param indexPath
	 * @return FSDirectory
	 */
	@SneakyThrows
	@SuppressWarnings("all")
	private static FSDirectory getFsDirectory(Path indexPath) {
		LockFactory lockFactory = NoLockFactory.INSTANCE;
		FSDirectory fsDirectory;
		String vmName = System.getProperty("java.vm.name").toLowerCase();
		if (vmName.contains(BIT_64)) {
			fsDirectory = MMapDirectory.open(indexPath, lockFactory);
		} else {
			String systemName = System.getProperty("os.name").toLowerCase();
			if (systemName.contains(WIN)) {
				fsDirectory = SimpleFSDirectory.open(indexPath, lockFactory);
			} else {
				fsDirectory = NIOFSDirectory.open(indexPath, lockFactory);
			}
		}
		return fsDirectory;
	}

	private static final String WIN = "win";
	private static final String BIT_64 = "64";

}
