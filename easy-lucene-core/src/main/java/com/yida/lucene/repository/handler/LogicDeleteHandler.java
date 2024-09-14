package com.yida.lucene.repository.handler;

import com.yida.lucene.annotation.LogicDel;
import com.yida.lucene.bean.Pair;
import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.plugin.Invocation;
import com.yida.lucene.repository.ElRepository;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * @author yida
 * @date 2024/9/1 10:03
 * @deprecated
 */
@Deprecated
public class LogicDeleteHandler implements RepositoryHandler {

	private final ThreadLocal<Boolean> willDoDelete = new InheritableThreadLocal<>();

	@Override
	public int order() {
		return 0;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void beforeDelete(Invocation invocation) {
		ElRepository repository = RepositoryHandler.getReposity(invocation);
		Pair<Field, LogicDel> logicDel = repository.getSource().getDocFactory().getLogicDel();
		if (Objects.isNull(logicDel)) {
			willDoDelete.set(true);
			return;
		} else {
			willDoDelete.set(false);
		}
		Field field = logicDel.getKey();
		Object query = invocation.getArgs()[0];
		try {
			if (query instanceof Query[]) {
				Query[] queries = (Query[]) query;
				if (queries.length == 0) {
					return;
				}
				BooleanQuery.Builder builder = new BooleanQuery.Builder();
				for (Query q : queries) {
					builder.add(q, BooleanClause.Occur.SHOULD);
				}
				List list = repository.selectList(builder.build(), null, -1, null, null);
				for (Object o : list) {
					field.set(o, true);
				}
				repository.update(list);
			}
		} catch (IllegalAccessException e) {
			throw EmbeddedLuceneException.of(e);
		}
	}

	@Override
	public boolean willDoDelete(Invocation invocation) {
		return willDoDelete.get();
	}

	@Override
	public void finallyDelete(Invocation invocation) {
		willDoDelete.remove();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void beforeSearch(Invocation invocation) {

		Pair<Field, LogicDel> pair = RepositoryHandler.getReposity(invocation).getSource().getDocFactory().getLogicDel();

		if (Objects.isNull(pair)) {
			return;
		}

		Field field = pair.getKey();

		Query logicDeleteQuery = new TermQuery(new Term(field.getName(), Boolean.FALSE.toString()));
		Query orgQuery = (Query) invocation.getArgs()[0];

		invocation.getArgs()[0] = new BooleanQuery.Builder()
				.add(orgQuery, BooleanClause.Occur.MUST)
				.add(logicDeleteQuery, BooleanClause.Occur.MUST)
				.build();

	}

}
