package com.yida.lucene.transaction;

import lombok.RequiredArgsConstructor;

import javax.transaction.UserTransaction;

/**
 * @author yida
 * @date 2024/9/2 12:11
 */
@RequiredArgsConstructor
public class ElUserTransaction implements UserTransaction {

	private final ElTransactionManager target;

	@Override
	public void begin() {
		target.begin();
	}

	@Override
	public void commit() {
		target.commit();
	}

	@Override
	public void rollback() {
		target.rollback();
	}

	@Override
	public void setRollbackOnly() {
		target.setRollbackOnly();
	}

	@Override
	public int getStatus() {
		return target.getStatus();
	}

	@Override
	public void setTransactionTimeout(int seconds) {
		target.setTransactionTimeout(seconds);
	}

}
