package com.yida.lucene.repository.query.wrapper;

import com.yida.lucene.core.ElDocument;
import com.yida.lucene.repository.ElRepository;
import com.yida.lucene.repository.query.Page;
import com.yida.lucene.repository.query.PageQuery;
import com.yida.lucene.util.LambdaUtil;
import com.yida.lucene.util.SerializableFunction;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author yida
 * @date 2024/09/23 14:02
 */
@RequiredArgsConstructor
public class LambdaQueryChainWrapper<T> extends AbstractConditionQueryWrapper<T, SerializableFunction<T, ?>, LambdaQueryChainWrapper<T>> implements ChainWrapper<T> {

	private final ElRepository<T> repository;

	@SafeVarargs
	@Override
	public final LambdaQueryChainWrapper<T> select(SerializableFunction<T, ?>... columns) {
		select(true, columns);
		return this;
	}

	@SafeVarargs
	@Override
	public final LambdaQueryChainWrapper<T> select(boolean condition, SerializableFunction<T, ?>... columns) {
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

	public T one() {
		getQueryWrapper().setSource(repository.getSource());
		return repository.select(this);
	}

	public List<T> list() {
		return repository.selectList(this);
	}

	public Page<T> page(PageQuery pageQuery) {
		return repository.selectPage(this, pageQuery);
	}

	public ElDocument<T> oneDoc() {
		getQueryWrapper().setSource(repository.getSource());
		return repository.selectDoc(this);
	}

	public List<ElDocument<T>> listDoc() {
		return repository.selectDocList(this);
	}

	public Page<ElDocument<T>> pageDoc(PageQuery pageQuery) {
		return repository.selectDocPage(this, pageQuery);
	}

}
