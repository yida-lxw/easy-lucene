package com.yida.lucene.annotation;

import com.yida.lucene.constant.FieldType;
import org.apache.lucene.index.IndexableField;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段类型注解
 *
 * @author yida
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface DocField {

	/**
	 * 是否存储
	 */
	boolean store() default true;

	/**
	 * 字段类型
	 */
	FieldType type() default FieldType.TEXT;

	/**
	 * 扩展的字段
	 *
	 * @return Class[]
	 * @see com.yida.lucene.constant.FieldFactory
	 */
	Class<? extends IndexableField>[] extensionFields() default {};

}