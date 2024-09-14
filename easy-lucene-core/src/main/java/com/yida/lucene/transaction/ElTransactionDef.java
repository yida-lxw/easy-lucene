package com.yida.lucene.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yida
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElTransactionDef {

	public static final ElTransactionDef DEFAULT = new ElTransactionDef(-1, new Class[]{RuntimeException.class});

	private int timeout;

	private Class<? extends Throwable>[] rollbackFor;

}
