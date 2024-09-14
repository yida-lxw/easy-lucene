package com.yida.lucene.repository.query.wrapper;

import com.yida.lucene.repository.hightlight.Formatters;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.highlight.Formatter;

import java.util.Collection;

/**
 * @author yida
 * @date 2024/8/22 10:04
 */
@SuppressWarnings("all")
public interface ConditionQueryWrapper<T, C, Q extends ConditionQueryWrapper<T, C, Q>> extends QueryWrapper<T> {

	default Q select(C... columns) {
		return select(true, columns);
	}

	Q select(boolean condition, C... columns);

	Q eq(boolean condition, C column, Object value);

	default Q eq(C column, Object value) {
		return eq(true, column, value);
	}

	Q ne(boolean condition, C column, Object value);

	default Q ne(C column, Object value) {
		return ne(true, column, value);
	}

	Q ge(boolean condition, C column, Object value);

	default Q ge(C column, Object value) {
		return ge(true, column, value);
	}

	Q le(boolean condition, C column, Object value);

	default Q le(C column, Object value) {
		return le(true, column, value);
	}

	Q gt(boolean condition, C column, Object value);

	default Q gt(C column, Object value) {
		return gt(true, column, value);
	}

	Q lt(boolean condition, C column, Object value);

	default Q lt(C column, Object value) {
		return lt(true, column, value);
	}

	Q in(boolean condition, C column, Collection<?> value);

	default Q in(C column, Collection<?> value) {
		return in(true, column, value);
	}

	Q notIn(boolean condition, C column, Collection<?> value);

	default Q notIn(C column, Collection<?> value) {
		return notIn(true, column, value);
	}

	Q like(boolean condition, C column, Object value);

	default Q like(C column, Object value) {
		return like(true, column, value);
	}

	Q likeNoHighlight(boolean condition, C column, Object value);

	default Q likeNoHighlight(C column, Object value) {
		return likeNoHighlight(true, column, value);
	}

	Q notLike(boolean condition, C column, Object value);

	default Q notLike(C column, Object value) {
		return notLike(true, column, value);
	}

	Q sortAsc(boolean condition, C column, SortField.Type type);

	default Q sortAsc(C column, SortField.Type type) {
		return sortAsc(true, column, type);
	}

	Q sortDesc(boolean condition, C column, SortField.Type type);

	default Q sortDesc(C column, SortField.Type type) {
		return sortDesc(true, column, type);
	}

	default Q query(boolean condition, Query query) {
		return query(condition, query, BooleanClause.Occur.MUST);
	}

	default Q query(Query query) {
		return query(true, query, BooleanClause.Occur.MUST);
	}

	default Q query(Query query, BooleanClause.Occur occur) {
		return query(true, query, occur);
	}

	Q query(boolean condition, Query query, BooleanClause.Occur occur);

	default Q limit(int limit) {
		return limit(true, limit);
	}

	Q limit(boolean condition, int limit);

	/**
	 * 开启高亮查询
	 *
	 * @param formatter
	 */
	Q highlight(boolean condition, Formatter formatter);

	default Q highlight(Formatter formatter) {
		return highlight(true, formatter);
	}

	default Q highlight() {
		return highlight(true, Formatters.DEFAULT);
	}

	default Q highlight(boolean condition) {
		return highlight(condition, Formatters.DEFAULT);
	}

}
