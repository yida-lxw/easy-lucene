package com.yida.lucene.repository.query;

import com.yida.lucene.util.LambdaUtil;
import com.yida.lucene.util.SerializableFunction;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 排序对象构造工具类
 *
 * @author yida
 * @date 2024/8/29 11:13
 */
public class Sorts<T> {

	@SuppressWarnings("all")
	private static final Sorts EMPTY = new Sorts<>();

	@SuppressWarnings("unchecked")
	public static <T> Sorts<T> empty() {
		return EMPTY;
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	private final List<Supplier<SortField>> sortChain = new LinkedList<>();

	public Sort getSort() {
		if (sortChain.isEmpty()) {
			return null;
		}
		return new Sort(sortChain.stream().map(Supplier::get).toArray(SortField[]::new));
	}

	public static class Builder<T> {

		private final Sorts<T> target = new Sorts<>();

		public Builder<T> asc(SerializableFunction<T, ?> column, SortField.Type type) {
			asc(true, column, type);
			return this;
		}

		public Builder<T> asc(boolean condition, SerializableFunction<T, ?> column, SortField.Type type) {
			if (condition) {
				target.sortChain.add(() -> {
					String name = LambdaUtil.getFieldName(column);
					return new SortField(name, type);
				});
			}
			return this;
		}

		public Builder<T> desc(SerializableFunction<T, ?> column, SortField.Type type) {
			desc(true, column, type);
			return this;
		}

		public Builder<T> desc(boolean condition, SerializableFunction<T, ?> column, SortField.Type type) {
			if (condition) {
				target.sortChain.add(() -> {
					String name = LambdaUtil.getFieldName(column);
					return new SortField(name, type, true);
				});
			}
			return this;
		}

		public Sorts<T> build() {
			return target;
		}

	}
}
