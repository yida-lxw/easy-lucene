package com.yida.lucene.repository.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果
 *
 * @author yida
 * @date 2024/8/19 10:14
 */
@Data
@Getter
@NoArgsConstructor(staticName = "empty")
@AllArgsConstructor(staticName = "of")
public class Page<T> {

	/**
	 * 查询数据列表
	 */
	protected List<T> records = Collections.emptyList();

	/**
	 * 总数
	 */
	protected long total = 0;

	/**
	 * 总页数
	 */
	protected int totalPage = 0;

	/**
	 * 每页显示条数，默认 10
	 */
	protected int size = 10;

	/**
	 * 当前页
	 */
	protected int current = 1;

}
