package com.yida.lucene.repository.handler;

import com.yida.lucene.plugin.Invocation;
import com.yida.lucene.repository.ElRepository;
import com.yida.lucene.repository.RepositoryInterceptor;
import com.yida.lucene.transaction.ElTransaction;
import com.yida.lucene.transaction.ElTransactionManager;
import com.yida.lucene.util.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;

import java.util.Arrays;
import java.util.Objects;

/**
 * 日志输出
 *
 * @author yida
 * @date 2024/8/30 3:01
 */
@Slf4j
public class LogHandler implements RepositoryHandler {

	private final ThreadLocal<StopWatch> stopWatcher = new InheritableThreadLocal<>();
	private ElTransactionManager txManager;

	private void prepare(Invocation invocation) {
		if (Objects.isNull(txManager)) {
			txManager = RepositoryHandler.getReposity(invocation).getSource().getTxManager();
		}
		final StopWatch stopWatch = new StopWatch();
		stopWatcher.set(stopWatch);
		stopWatch.start();
	}

	private void handleFinally(String method) {
		ElTransaction transaction = txManager.getElTransaction();
		final StopWatch stopWatch = stopWatcher.get();
		if (Objects.nonNull(transaction)) {
			transaction.setFinallyHook(() -> {
				stopWatch.stop();
				log.info("\n======= execute method : {} , time consumed : {} ms =======", method, stopWatch.getTotalTimeMillis());
				stopWatcher.remove();
			});
		} else {
			stopWatch.stop();
			log.info("\n======= execute method : {} , time consumed : {} ms =======", method, stopWatch.getTotalTimeMillis());
			stopWatcher.remove();
		}
	}

	@Override
	public void afterSearch(Invocation invocation, TopDocs res) {
		TotalHits totalHits = res.totalHits;
		boolean emptyRes = res.equals(ElRepository.EMPTY_TOP_DOCS);
		final Object[] args = invocation.getArgs();
		String scoreDocs;
		log.info("\n╔═════════════════ Search Info ═════════════════╗\n" +
						"║ Query：{}\n" +
						"║ Sort：{}\n" +
						"║ Limit：{}\n" +
						"║ ----------------------------------------------\n" +
						"║ TotalHits：{}\n" +
						"║ Relation：{}\n" +
						"║ ScoreDocs：{}\n" +
						"╚═══════════════════════════════════════════════╝\n",
				args[0].toString(),
				Objects.nonNull(args[1]) ? args[1].toString() : " ",
				args[2].toString(),
				emptyRes ? " " : totalHits.toString(),
				emptyRes ? " " : totalHits.relation,
				emptyRes ? "[]"
						: (scoreDocs = Arrays.toString(res.scoreDocs)).length() >= 200
						? scoreDocs.substring(0, 200) + "..."
						: scoreDocs

		);
	}

	@Override
	public int order() {
		return Integer.MIN_VALUE;
	}

	@Override
	public void beforeInsert(Invocation invocation) {
		prepare(invocation);
	}

	@Override
	public void beforeUpdate(Invocation invocation) {
		prepare(invocation);
	}

	@Override
	public void beforeDelete(Invocation invocation) {
		prepare(invocation);
	}

	@Override
	public void beforeSearch(Invocation invocation) {
		prepare(invocation);
	}

	@Override
	public void finallyInsert(Invocation invocation) {
		handleFinally(RepositoryInterceptor.INSERT);
	}

	@Override
	public void finallyUpdate(Invocation invocation) {
		handleFinally(RepositoryInterceptor.UPDATE);
	}

	@Override
	public void finallyDelete(Invocation invocation) {
		handleFinally(RepositoryInterceptor.DELETE);
	}

	@Override
	public void finallySearch(Invocation invocation) {
		handleFinally(RepositoryInterceptor.SEARCH);
	}

}
