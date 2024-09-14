package com.yida.lucene.repository.handler;


import com.yida.lucene.constant.FieldType;

/**
 * 自定义的字段填充规则
 *
 * @author yida
 * @date 2024/9/1 10:50
 * @see AutoFillHandler
 */
@FunctionalInterface
public interface AutoFillValProvider {

	/**
	 * 获取默认值
	 *
	 * @param fieldName  字段名称
	 * @param fieldClass class类型
	 * @param fieldType  字段类型
	 * @return 默认值
	 */
	Object getVal(String fieldName, Class<?> fieldClass, FieldType fieldType);

}
