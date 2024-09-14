package com.yida.lucene.transaction;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yida
 * @date 2024/9/5 11:38
 */
public class ElXidProvider {

	private final AtomicInteger stamp = new AtomicInteger(0);
	private final Collection<XaSource<?>> xaSources;

	public ElXidProvider(Collection<XaSource<?>> xaSources) {
		this.xaSources = xaSources;
	}

	public ElXid get() {
		int val = stamp.getAndIncrement();
		while (stamp.get() < 0) {
			if (stamp.compareAndSet(stamp.get(), 0)) {
				for (XaSource<?> xaSource : xaSources) {
					xaSource.waitForAllXidToComplete();
				}
				break;
			}
		}
		return new ElXid(val);
	}

}
