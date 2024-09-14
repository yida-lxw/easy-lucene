package com.yida.lucene.spring.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yida
 * @package com.yida.lucene.spring.util
 * @date 2024-09-13 11:35
 * @description Type your description over here.
 */
public class BeanScanUtils {
	private static final Logger logger = LoggerFactory.getLogger(BeanScanUtils.class);
	private static final String RESOURCE_PATTERN = "/**/*.class";

	public static <T> Map<String, Class<T>> scanBean(Set<String> scanPackages, Class<?> annotationClass) {
		Map<String, Class<T>> finalHandlerMap = new HashMap<>();
		for (String scanPackage : scanPackages) {
			Map<String, Class<T>> handlerMap = scanBean(scanPackage, annotationClass);
			if (null == handlerMap || handlerMap.isEmpty()) {
				continue;
			}
			finalHandlerMap.putAll(handlerMap);
		}
		return finalHandlerMap;
	}

	public static <T> Map<String, Class<T>> scanBean(String scanPackage, Class annotationClass) {
		Map<String, Class<T>> handlerMap = new HashMap<>();
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		try {
			String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath(scanPackage) + RESOURCE_PATTERN;
			Resource[] resources = resourcePatternResolver.getResources(pattern);
			MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
			for (Resource resource : resources) {
				MetadataReader reader = readerFactory.getMetadataReader(resource);
				//扫描到的class
				String className = reader.getClassMetadata().getClassName();
				Class<T> clazz = (Class<T>) Class.forName(className);
				Annotation anno = clazz.getAnnotation(annotationClass);
				if (null != anno) {
					handlerMap.put(className, clazz);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			logger.error("As Scaning the bean with specified annotation:[{}] in the package:[{}] occur exception:\n{}.",
					annotationClass.getName(), scanPackage, e.getMessage());
		} finally {
			return handlerMap;
		}
	}
}
