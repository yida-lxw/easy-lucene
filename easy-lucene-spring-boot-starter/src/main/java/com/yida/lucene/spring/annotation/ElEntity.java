package com.yida.lucene.spring.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记实体类，自动创建关联的repository实例注册到ioc容器中
 *
 * @author yida
 * @date 2024/8/25 12:02
 * @see com.yida.lucene.spring.ElRepositoryRegistry
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElEntity {

	/**
	 * docName
	 */
	@AliasFor("docName")
	String value() default "";

	@AliasFor("value")
	String docName() default "";

	String analyzerBeanName() default "";

	/**
	 * Field对应的分词器映射JSON文件路径
	 */
	String analyzerMappingJSONFilePath() default "";
}
