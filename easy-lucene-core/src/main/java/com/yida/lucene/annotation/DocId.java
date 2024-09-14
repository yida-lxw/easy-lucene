package com.yida.lucene.annotation;

import com.yida.lucene.constant.FieldType;
import org.apache.lucene.index.IndexableField;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * id类型注解<br/>
 * 可以理解成数据库主键
 *
 * @author yida
 * @date 2024/8/23 9:31
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DocId {

	/**
	 * 字段类型
	 */
	FieldType type() default FieldType.LONG;

	/**
	 * 扩展的字段
	 *
	 * @return Class[]
	 * @see com.yida.lucene.constant.FieldFactory
	 */
	Class<? extends IndexableField>[] extensionFields() default {};

}
