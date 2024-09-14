package com.yida.lucene.transaction;

import com.yida.lucene.core.EmbeddedLucene;
import com.yida.lucene.exception.EmbeddedLuceneException;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.Collection;
import java.util.Objects;

/**
 * 事务管理器<br/>
 * 支持当前事务，如果不存在，则创建一个新事务。<br/>
 * 传播机制类似Spring中的Propagation.REQUIRED<br/>
 * 隔离级别为Serialize串行
 *
 * @author yida
 * @date 2024/9/2 9:19
 */
public class ElTransactionManager implements TransactionManager, XaSourcePool {

	private final ThreadLocal<ElTransaction> txHolder = new InheritableThreadLocal<>();
	private final EmbeddedLucene lucene;
	private final ElXidProvider elXidProvider;

	public ElTransactionManager(EmbeddedLucene lucene) {
		this.lucene = lucene;
		this.elXidProvider = new ElXidProvider(lucene.getXaSources());
	}

	public ElTransaction getElTransaction() {
		return txHolder.get();
	}

	@Override
	public Transaction getTransaction() {
		return txHolder.get();
	}

	@Override
	public void begin() {
		ElTransaction transaction = txHolder.get();
		if (Objects.isNull(transaction)) {
			transaction = new ElTransaction(elXidProvider.get(), lucene.getExecutor());
			txHolder.set(transaction);
		}
	}

	@Override
	public void commit() {
		ElTransaction transaction = txHolder.get();
		if (Objects.isNull(transaction)) {
			throw new IllegalStateException("current thread is not associated with a transaction");
		}
		try {
			transaction.commit();
		} finally {
			transaction.finallyHook();
		}
		txHolder.remove();
	}

	@Override
	public void rollback() {
		ElTransaction transaction = txHolder.get();
		if (Objects.isNull(transaction)) {
			throw new IllegalStateException("current thread is not associated with a transaction");
		}
		try {
			transaction.rollback();
		} finally {
			txHolder.remove();
		}
	}

	@Override
	public int getStatus() {
		ElTransaction transaction = txHolder.get();
		if (Objects.isNull(transaction)) {
			return Status.STATUS_NO_TRANSACTION;
		}
		return transaction.getStatus();
	}

	@Override
	public void resume(Transaction transaction) {
		if (Objects.isNull(transaction)) {
			throw EmbeddedLuceneException.of("resumed transaction must not been null");
		}
		ElTransaction elTransaction;
		if (transaction instanceof ElTransaction) {
			elTransaction = (ElTransaction) transaction;
		} else {
			throw EmbeddedLuceneException.of("resumed transaction must been type of ElTransaction");
		}
		ElTransaction among = txHolder.get();
		if (Objects.nonNull(among) && !among.equals(transaction)) {
			throw new IllegalStateException("resumed thread already has transaction");
		}
		txHolder.set(elTransaction);
	}

	@Override
	public void setRollbackOnly() {
		ElTransaction transaction = txHolder.get();
		if (Objects.isNull(transaction)) {
			throw new IllegalStateException("current thread is not associated with a transaction");
		}
		transaction.setRollbackOnly();
	}

	@Override
	public void setTransactionTimeout(int seconds) {
		if (seconds <= 0) {
			return;
		}
		ElTransaction transaction = txHolder.get();
		transaction.setTimeout(seconds);
	}

	@Override
	public Transaction suspend() {
		Transaction transaction = getTransaction();
		txHolder.remove();
		return transaction;
	}

	@Override
	public <T> XaSource<T> getXaSource(Class<T> docClass) {
		return lucene.getXaSource(docClass);
	}

	@Override
	public Collection<XaSource<?>> getXaSources() {
		return lucene.getXaSources();
	}

}
