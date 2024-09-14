package com.yida.lucene.transaction;

import javax.transaction.Status;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 目前事务执行中无法等待获取影响行数结果
 *
 * @author yida
 * @date 2024/9/27 15:00
 */
public class TransactionSupportFutureTask extends FutureTask<Integer> {

	private final ElTransaction elTransaction;

	public TransactionSupportFutureTask(Callable<Integer> callable, ElTransaction elTransaction) {
		super(callable);
		this.elTransaction = elTransaction;
	}

	public TransactionSupportFutureTask(Runnable runnable, Integer result, ElTransaction elTransaction) {
		super(runnable, result);
		this.elTransaction = elTransaction;
	}

	@Override
	public Integer get() throws InterruptedException, ExecutionException {
		int status = elTransaction.getStatus();
		if (status == Status.STATUS_MARKED_ROLLBACK || status == Status.STATUS_ROLLING_BACK || status == Status.STATUS_ROLLEDBACK) {
			return 0;
		}
		if (status == Status.STATUS_ACTIVE || status == Status.STATUS_COMMITTING) {
			throw new InterruptedException("In the current transaction state, the result cannot be obtained!");
		}
		return super.get();
	}

	@Override
	public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		int status = elTransaction.getStatus();
		if (status == Status.STATUS_MARKED_ROLLBACK || status == Status.STATUS_ROLLING_BACK || status == Status.STATUS_ROLLEDBACK) {
			return 0;
		}
		if (status == Status.STATUS_ACTIVE || status == Status.STATUS_COMMITTING) {
			throw new InterruptedException("In the current transaction state, the result cannot be obtained!");
		}
		return super.get(timeout, unit);
	}

}
