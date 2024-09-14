package com.yida.lucene.transaction;

import com.yida.lucene.core.Source;
import com.yida.lucene.exception.EmbeddedLuceneException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * {@link Source}事务支持适配器
 *
 * @author yida
 * @date 2024/9/2 9:51
 */
@Getter
@EqualsAndHashCode
public class XaSource<T> implements XAResource {

	private final PriorityBlockingQueue<Xid> queue = new PriorityBlockingQueue<>(11, Comparator.comparing(Xid::getFormatId));
	private final Source<T> delegate;
	private volatile Xid lastXid;

	public XaSource(Source<T> delegate) {
		this.delegate = delegate;
	}

	public void waitForAllXidToComplete() {
		while (true) {
			if (queue.peek() == null) {
				return;
			}
		}
	}

	@Override
	public void start(Xid xid, int flags) {
		if (lastXid == null) {
			lastXid = xid;
			queue.offer(xid);
			return;
		}
		if (lastXid.getFormatId() >= xid.getFormatId()) {
			throw EmbeddedLuceneException.of("The XID already exists, code : " + XAException.XAER_DUPID);
		}
		queue.offer(xid);
	}

	@Override
	public void commit(Xid xid, boolean onePhase) {
		while (true) {
			try {
				if (xid.equals(queue.peek())) {
					queue.take();
					delegate.commit();
					return;
				}
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				throw EmbeddedLuceneException.of(e);
			}
		}
	}

	@Override
	public void rollback(Xid xid) {
		while (true) {
			try {
				if (xid.equals(queue.peek())) {
					queue.take();
					delegate.rollback();
					return;
				}
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				throw EmbeddedLuceneException.of(e);
			}
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean isSameRM(XAResource res) {
		if (res instanceof XaSource) {
			XaSource xas = (XaSource) res;
			return xas.getDelegate().getDocClass().equals(delegate.getDocClass());
		}
		return false;
	}

	@Override
	public int getTransactionTimeout() {
		return 0;
	}

	@Override
	public boolean setTransactionTimeout(int seconds) {
		return false;
	}

	/**
	 * 不支持下列分布式事务特性
	 */

	@Override
	public int prepare(Xid xid) {
		throw EmbeddedLuceneException.of("unsupported operate");
	}

	@Override
	public void end(Xid xid, int flags) {
		throw EmbeddedLuceneException.of("unsupported operate");
	}

	@Override
	public void forget(Xid xid) {
		throw EmbeddedLuceneException.of("unsupported operate");
	}

	@Override
	public Xid[] recover(int flag) {
		throw EmbeddedLuceneException.of("unsupported operate");
	}

}
