package com.yida.lucene.repository.query;

import com.yida.lucene.repository.query.wrapper.LambdaQueryWrapper;
import com.yida.lucene.repository.query.wrapper.StrQueryWrapper;
import lombok.experimental.UtilityClass;

/**
 * 查询包装器工具类
 *
 * @author yida
 * @date 2024/8/24 14:45
 */
@UtilityClass
public class Wrappers {

	public <T> StrQueryWrapper<T> strQuery() {
		return new StrQueryWrapper<>();
	}

	public <T> LambdaQueryWrapper<T> lambdaQuery() {
		return new LambdaQueryWrapper<>();
	}

}
