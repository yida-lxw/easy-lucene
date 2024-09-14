package com.yida.lucene.repository.query.wrapper;


import com.yida.lucene.util.LambdaUtil;
import com.yida.lucene.util.SerializableFunction;

/**
 * @author yida
 * @date 2024/8/22 10:21
 */
public class LambdaQueryWrapper<T> extends AbstractConditionQueryWrapper<T, SerializableFunction<T, ?>, LambdaQueryWrapper<T>> {

	@SafeVarargs
	@Override
	public final LambdaQueryWrapper<T> select(SerializableFunction<T, ?>... columns) {
		select(true, columns);
		return this;
	}

	@SafeVarargs
	@Override
	public final LambdaQueryWrapper<T> select(boolean condition, SerializableFunction<T, ?>... columns) {
		if (condition) {
			for (SerializableFunction<T, ?> column : columns) {
				selectFieldSet.add(columnToStr(column));
			}
		}
		return this;
	}

	@Override
	protected String columnToStr(SerializableFunction<T, ?> column) {
		return LambdaUtil.getFieldName(column);
	}
}
