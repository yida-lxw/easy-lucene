package com.yida.lucene.spring.annotation;

import com.yida.lucene.spring.EmbeddedLuceneAutoConfiguration;
import com.yida.lucene.spring.EmbeddedLuceneProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启EmbeddedLucene自动配置
 *
 * @author yida
 * @date 2024/8/26 12:27
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableConfigurationProperties(EmbeddedLuceneProperties.class)
@Import(EmbeddedLuceneAutoConfiguration.class)
public @interface EnableEmbeddedLucene {
}
