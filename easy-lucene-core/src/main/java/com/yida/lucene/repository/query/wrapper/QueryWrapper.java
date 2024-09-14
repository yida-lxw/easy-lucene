package com.yida.lucene.repository.query.wrapper;

import com.yida.lucene.core.Source;
import com.yida.lucene.repository.hightlight.HighlightRender;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

import java.util.Set;

/**
 * @author yida
 * @date 2024/8/9 12:23
 */
public interface QueryWrapper<T> {


	/**
	 * 获取查询的字段，空即全部
	 *
	 * @return Set
	 */
	Set<String> getSelect();

	/**
	 * 转换成Query
	 *
	 * @return query
	 */
	Query getQuery();

	/**
	 * 转换成Sort
	 *
	 * @return sort
	 */
	Sort getSort();

	/**
	 * 转换成HighlightRender
	 *
	 * @return HighlightRender
	 */
	default HighlightRender getHighlightRender() {
		return null;
	}

	/**
	 * limit
	 *
	 * @return int
	 */
	int getLimit();

	/**
	 * 设置source
	 *
	 * @param source source
	 */
	void setSource(Source<T> source);

}
