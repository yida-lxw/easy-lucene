package com.yida.lucene.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author yida
 * @package com.yida.lucene.spring
 * @date 2024-09-13 11:30
 * @description Spring Bean注入器
 */
@Component
public class BeanInjector implements ApplicationContextAware {
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		BeanInjector.applicationContext = applicationContext;
	}

	public static <T> void injectBean(T bean) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
	}
}
