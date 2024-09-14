package com.yida.lucene.spring.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 参考 https://segmentfault.com/a/1190000008863340
 *
 * @author yida
 * @date 2024/08/25 12:10
 */
@Slf4j
@UtilityClass
@SuppressWarnings("all")
public class AnnotationScanner {

	public <T extends Annotation> Map<Class<?>, T> scanAnno(Set<String> packages, Class<T> anno) {
		return scan(packages, anno, false);
	}

	private final String RESOURCE_PATTERN = "**/%s/**/*.class";
	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private <T extends Annotation> Map<Class<?>, T> scan(Set<String> confPkgs, Class<? extends T> annotation, boolean isInterface) {
		Map<Class<?>, T> res = new HashMap();
		AnnotationTypeFilter typeFilters = null;
		if (Objects.nonNull(annotation)) {
			typeFilters = new AnnotationTypeFilter(annotation, false);
		} else {
			return Collections.emptyMap();
		}
		if (null != confPkgs) {
			for (String pkg : confPkgs) {
				String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + String.format(RESOURCE_PATTERN, ClassUtils.convertClassNameToResourcePath(pkg));
				try {
					Resource[] resources = resourcePatternResolver.getResources(pattern);
					MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
					for (Resource resource : resources) {
						if (resource.isReadable()) {
							MetadataReader reader = readerFactory.getMetadataReader(resource);
							String className = reader.getClassMetadata().getClassName();
							if (ifMatchesEntityType(reader, readerFactory, typeFilters)) {
								Class curClass = Thread.currentThread().getContextClassLoader().loadClass(className);
								res.put(curClass, (T) curClass.getAnnotation(annotation));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * 检查当前扫描到的类是否含有任何一个指定的注解标记
	 *
	 * @param reader
	 * @param readerFactory
	 * @return ture/false
	 */
	private boolean ifMatchesEntityType(
			MetadataReader reader,
			MetadataReaderFactory readerFactory,
			AnnotationTypeFilter typeFilter) {
		try {
			if (typeFilter.match(reader, readerFactory)) {
				return true;
			}
		} catch (IOException e) {
			log.error("过滤匹配类型时出错 {}", e.getMessage());
		}
		return false;
	}

}