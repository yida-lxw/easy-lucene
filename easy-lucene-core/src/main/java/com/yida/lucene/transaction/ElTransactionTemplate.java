package com.yida.lucene.transaction;

import com.yida.lucene.exception.ElAssert;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

/**
 * @author yida
 * @date 2024/9/2 13:09
 */
@Setter
@NoArgsConstructor
public class ElTransactionTemplate {

	private ElTransactionManager txManager;

	public ElTransactionTemplate(ElTransactionManager txManager) {
		this.txManager = txManager;
	}

	public <T> T execute(Supplier<T> action, ElTransactionDef definition) {
		ElAssert.nonNull(this.txManager, "No ElTransactionManager set");
		txManager.begin();
		txManager.setTransactionTimeout(definition.getTimeout());
		T result;
		try {
			result = action.get();
			txManager.commit();
		} catch (Throwable e) {
			for (Class<? extends Throwable> rollback : definition.getRollbackFor()) {
				if (rollback.isInstance(e)) {
					txManager.rollback();
					break;
				}
			}
			throw e;
		}
		return result;
	}

	public <T> T execute(Supplier<T> action) {
		return execute(action, ElTransactionDef.DEFAULT);
	}

	public void executeWithoutResult(Runnable action) {
		execute(() -> {
			action.run();
			return null;
		}, ElTransactionDef.DEFAULT);
	}

	public void executeWithoutResult(Runnable action, ElTransactionDef definition) {
		execute(() -> {
			action.run();
			return null;
		}, definition);
	}
}
