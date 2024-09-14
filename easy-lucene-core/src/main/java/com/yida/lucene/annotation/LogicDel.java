package com.yida.lucene.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 逻辑删除, 只支持boolean类型
 *
 * @author yida
 * @date 2024/9/1 9:52
 * @see com.yida.lucene.repository.handler.LogicDeleteHandler
 * @deprecated
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface LogicDel {

}
