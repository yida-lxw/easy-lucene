package com.yida.lucene.repository.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询
 *
 * @author yida
 * @date 2024/8/19 10:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PageQuery {

	protected int current = 1;

	protected int size = 10;

}
