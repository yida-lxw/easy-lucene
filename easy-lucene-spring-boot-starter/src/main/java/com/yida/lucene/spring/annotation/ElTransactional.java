package com.yida.lucene.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事务
 *
 * @author yida
 * @date 2024/09/01 13:14
 * @see com.yida.lucene.spring.ElTransactionAspect
 * @see com.alone.embedded.lucene.transaction.ElTransactionManager
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElTransactional {

	/**
	 * 单位: 秒
	 */
	int timeout() default -1;

	Class<? extends Throwable>[] rollbackFor() default {RuntimeException.class};

}

