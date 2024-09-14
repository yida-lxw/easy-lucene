package com.yida.lucene.transaction;

import com.yida.lucene.exception.EmbeddedLuceneException;
import lombok.Setter;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author yida
 * @date 2024/9/2 9:22
 */
public class ElTransaction implements Transaction {

	private final Map<XaSource<?>, Boolean> sourceSet = new ConcurrentHashMap<>();
	private final ExecutorService worker;
	private final ElXid currentXid;
	private boolean rollbackOnly = false;
	private int status;
	@Setter
	private int timeout = -1;


	/**
	 * 延时 update 操作到刷新 index writer 提交前一并执行
	 * 解决一个事务存在同时提交了其他并行事务的修改内容问题
	 */
	private final ComposedRunnable composedRunnable = new ComposedRunnable();

	public ElTransaction(ElXid elXid, ExecutorService worker) {
		this.currentXid = elXid;
		this.worker = worker;
		this.status = Status.STATUS_ACTIVE;
	}

	public void addTask(Runnable runnable) {
		composedRunnable.add(runnable);
	}

	@Override
	public void commit() {
		if (rollbackOnly) {
			return;
		}
		this.status = Status.STATUS_COMMITTING;
		Runnable runnable = () -> {
			composedRunnable.run();
			sourceSet.keySet().stream()
					// 并行处理
					.map(xaSource -> getFuture(() -> xaSource.commit(currentXid, true)))
					.forEach(CompletableFuture::join);
		};
		timeoutHandle(runnable);

		this.status = Status.STATUS_COMMITTED;
	}

	private void timeoutHandle(Runnable runnable) {
		FutureTask<Void> task = new FutureTask<>(runnable, null);
		task.run();
		try {
			if (timeout > 0) {
				task.get(timeout, TimeUnit.SECONDS);
			} else {
				task.get();
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw EmbeddedLuceneException.of("transaction timeout!");
		}
	}

	@Override
	public void rollback() {
		this.status = Status.STATUS_ROLLING_BACK;
		Runnable runnable = () -> sourceSet.keySet().stream()
				// 并行处理
				.map(xaSource -> getFuture(() -> xaSource.rollback(currentXid)))
				.forEach(CompletableFuture::join);
		timeoutHandle(runnable);

		this.status = Status.STATUS_ROLLEDBACK;
	}

	@Override
	public boolean delistResource(XAResource xaRes, int flag) {
		throw EmbeddedLuceneException.of("unsupported operate");
	}

	@Override
	public boolean enlistResource(XAResource xaRes) {
		if (!(xaRes instanceof XaSource)) {
			return false;
		}
		for (XaSource<?> xaSource : sourceSet.keySet()) {
			if (xaSource.isSameRM(xaRes)) {
				return false;
			}
		}
		XaSource<?> source = (XaSource<?>) xaRes;
		sourceSet.put(source, true);
		source.start(currentXid, XAResource.TMNOFLAGS);
		return true;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	@Override
	public void setRollbackOnly() {
		this.rollbackOnly = true;
		this.status = Status.STATUS_MARKED_ROLLBACK;
	}


	@Override
	public void registerSynchronization(Synchronization sync) {
		throw EmbeddedLuceneException.of("unsupported operate");
	}

	private static class ComposedRunnable implements Runnable {

		private List<Runnable> runnableList;

		@Override
		public void run() {
			if (runnableList == null) {
				return;
			}
			for (Runnable runnable : runnableList) {
				runnable.run();
			}
		}

		public void add(Runnable runnable) {
			if (runnableList == null) {
				runnableList = new ArrayList<>(4);
			}
			runnableList.add(runnable);
		}

	}


	public CompletableFuture<Void> getFuture(Runnable task) {
		return CompletableFuture.runAsync(task, worker);
	}

	@Setter
	private Runnable finallyHook;

	public void finallyHook() {
		if (Objects.nonNull(finallyHook)) {
			finallyHook.run();
			finallyHook = null;
		}
	}

}
