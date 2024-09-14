package com.yida.lucene.repository.query.wrapper;

import java.util.Arrays;

/**
 * @author yida
 * @date 2024/8/22 10:21
 */
public class StrQueryWrapper<T> extends AbstractConditionQueryWrapper<T, String, StrQueryWrapper<T>> {

	@Override
	public final StrQueryWrapper<T> select(String... columns) {
		select(true, columns);
		return this;
	}

	@Override
	public final StrQueryWrapper<T> select(boolean condition, String... columns) {
		if (condition) {
			selectFieldSet.addAll(Arrays.asList(columns));
		}
		return this;
	}

	@Override
	protected String columnToStr(String column) {
		return column;
	}
}
