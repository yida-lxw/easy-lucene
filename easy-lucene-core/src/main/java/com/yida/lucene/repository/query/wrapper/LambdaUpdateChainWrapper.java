package com.yida.lucene.repository.query.wrapper;

import com.yida.lucene.constant.TypeChecker;
import com.yida.lucene.core.DocFactory;
import com.yida.lucene.repository.ElRepository;
import com.yida.lucene.util.LambdaUtil;
import com.yida.lucene.util.SerializableFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author yida
 * @date 2024/09/23 14:15
 */
public class LambdaUpdateChainWrapper<T> extends AbstractConditionQueryWrapper<T, SerializableFunction<T, ?>, LambdaUpdateChainWrapper<T>> implements ChainWrapper<T> {

	private final ElRepository<T> repository;
	private final DocFactory<T> docFactory;
	private final Map<String, Object> updateSet = new HashMap<>(4);

	public LambdaUpdateChainWrapper(ElRepository<T> repository) {
		this.repository = repository;
		this.docFactory = repository.getSource().getDocFactory();
	}

	@SafeVarargs
	@Override
	public final LambdaUpdateChainWrapper<T> select(SerializableFunction<T, ?>... columns) {
		select(true, columns);
		return this;
	}

	@SafeVarargs
	@Override
	public final LambdaUpdateChainWrapper<T> select(boolean condition, SerializableFunction<T, ?>... columns) {
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

	@Override
	public ElRepository<T> getRepository() {
		return repository;
	}

	@Override
	public QueryWrapper<T> getQueryWrapper() {
		return this;
	}

	public LambdaUpdateChainWrapper<T> set(SerializableFunction<T, ?> column, Object value) {
		String fieldName = LambdaUtil.getFieldName(column);
		TypeChecker.getChecker(docFactory.getFieldType(fieldName)).checkThenThrowIfNeed(value);
		updateSet.put(fieldName, value);
		return this;
	}

	public void update() {
		repository.update(selectBeforeUpdate());
	}

	public Future<Integer> updateFuture() {
		return repository.updateFuture(selectBeforeUpdate());
	}

	public void delete() {
		repository.delete(select());
	}

	public Future<Integer> deleteFuture() {
		return repository.deleteFuture(select());
	}

	private List<T> selectBeforeUpdate() {
		List<T> res = repository.selectList(this);
		for (T item : res) {
			for (Map.Entry<String, Object> entry : updateSet.entrySet()) {
				docFactory.setVal(item, entry.getKey(), entry.getValue());
			}
		}
		return res;
	}

	private List<T> select() {
		return repository.selectList(this);
	}

}
