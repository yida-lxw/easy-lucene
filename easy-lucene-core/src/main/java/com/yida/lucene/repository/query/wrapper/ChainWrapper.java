package com.yida.lucene.repository.query.wrapper;

import com.yida.lucene.repository.ElRepository;

/**
 * @author yida
 * @date 2024/09/23 14:00
 */
interface ChainWrapper<T> {

	/**
	 * 获取仓库类
	 *
	 * @return ElRepository
	 */
	ElRepository<T> getRepository();

	/**
	 * 获取匹配条件
	 *
	 * @return QueryWrapper
	 */
	QueryWrapper<T> getQueryWrapper();
}
