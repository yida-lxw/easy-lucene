package com.yida.lucene.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段填充
 *
 * @author yida
 * @date 2024/9/1 9:59
 * @see com.yida.lucene.repository.handler.AutoFillValProvider
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFill {

	/**
	 * 有值时，是否覆盖
	 *
	 * @return boolean
	 */
	boolean cover() default false;

}
