package com.yida.lucene.repository.query.wrapper;

import com.yida.lucene.bean.Pair;
import com.yida.lucene.constant.FieldType;
import com.yida.lucene.core.DocFactory;
import com.yida.lucene.core.Source;
import com.yida.lucene.repository.hightlight.Formatters;
import com.yida.lucene.repository.hightlight.HighlightRender;
import com.yida.lucene.repository.query.Querys;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.highlight.Formatter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author yida
 * @date 2024/8/22 10:12
 */
public abstract class AbstractConditionQueryWrapper<T, C, Q extends AbstractConditionQueryWrapper<T, C, Q>> implements ConditionQueryWrapper<T, C, Q> {

	@SuppressWarnings("unchecked")
	private final Q typedThis = (Q) this;

	private Formatter formatter;
	private boolean enableHighlight;
	private List<Consumer<HighlightRender>> highlighter;
	protected final Set<String> selectFieldSet = new HashSet<>();
	private final List<Pair<Query, BooleanClause.Occur>> custQueries = new LinkedList<>();
	private final List<Supplier<Query>> queryChain = new LinkedList<>();
	private final List<Supplier<SortField>> sortChain = new LinkedList<>();
	private DocFactory<T> docFactory;
	private Analyzer analyzer;

	private int limit = 0;

	/**
	 * 将列转换成名称
	 *
	 * @param column 列
	 * @return 名称
	 */
	protected abstract String columnToStr(C column);

	@Override
	public void setSource(Source<T> source) {
		this.docFactory = source.getDocFactory();
		this.analyzer = source.getWriter().getAnalyzer();
	}

	@Override
	public Set<String> getSelect() {
		return selectFieldSet;
	}

	@Override
	public Q eq(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				if (enableHighlight) {
					highlighter.add(highlightRender -> highlightRender.addRenderField(name));
				}
				return Querys.eq(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q ne(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.ne(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q ge(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.ge(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q query(boolean condition, Query query, BooleanClause.Occur occur) {
		if (condition) {
			custQueries.add(Pair.of(query, occur));
		}
		return typedThis;
	}

	@Override
	public Q sortAsc(boolean condition, C column, SortField.Type type) {
		if (condition) {
			sortChain.add(() -> {
				String name = columnToStr(column);
				return new SortField(name, type);
			});
		}
		return typedThis;
	}

	@Override
	public Q sortDesc(boolean condition, C column, SortField.Type type) {
		if (condition) {
			sortChain.add(() -> {
				String name = columnToStr(column);
				return new SortField(name, type, true);
			});
		}
		return typedThis;
	}

	@Override
	public Q le(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.le(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q gt(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.gt(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q lt(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.lt(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q in(boolean condition, C column, Collection<?> value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				if (enableHighlight) {
					highlighter.add(highlightRender -> highlightRender.addRenderField(name));
				}
				return Querys.in(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q notIn(boolean condition, C column, Collection<?> value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.notIn(type, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q like(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				if (enableHighlight) {
					highlighter.add(highlightRender -> highlightRender.addRenderField(name));
				}
				return Querys.like(type, analyzer, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q likeNoHighlight(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.like(type, analyzer, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Q notLike(boolean condition, C column, Object value) {
		if (condition) {
			queryChain.add(() -> {
				String name = columnToStr(column);
				FieldType type = docFactory.getFieldType(name);
				return Querys.notLike(type, analyzer, name, value);
			});
		}
		return typedThis;
	}

	@Override
	public Query getQuery() {
		if (queryChain.isEmpty() && custQueries.isEmpty()) {
			return Querys.MATCH_ALL_QUERY;
		}
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (Supplier<Query> querySupplier : queryChain) {
			builder.add(querySupplier.get(), BooleanClause.Occur.MUST);
		}
		for (Pair<Query, BooleanClause.Occur> custQuery : custQueries) {
			builder.add(custQuery.getKey(), custQuery.getValue());
		}
		return builder.build();
	}


	@Override
	public Sort getSort() {
		if (sortChain.isEmpty()) {
			return null;
		}
		return new Sort(sortChain.stream().map(Supplier::get).toArray(SortField[]::new));
	}

	@Override
	public HighlightRender getHighlightRender() {
		if (!enableHighlight || highlighter == null || highlighter.isEmpty()) {
			return null;
		}
		HighlightRender highlightRender = new HighlightRender(Objects.isNull(formatter) ? Formatters.DEFAULT : formatter);
		for (Consumer<HighlightRender> c : highlighter) {
			c.accept(highlightRender);
		}
		return highlightRender;
	}


	@Override
	public Q highlight(boolean condition, Formatter formatter) {
		if (condition) {
			this.formatter = formatter;
			// init
			if (!this.enableHighlight) {
				this.enableHighlight = true;
				this.highlighter = new LinkedList<>();
			}
		}
		return typedThis;
	}

	@Override
	public Q limit(boolean condition, int limit) {
		if (condition) {
			this.limit = limit;
		}
		return typedThis;
	}

	@Override
	public int getLimit() {
		return limit;
	}
}
