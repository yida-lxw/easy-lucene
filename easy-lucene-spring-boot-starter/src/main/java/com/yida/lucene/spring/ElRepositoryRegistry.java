package com.yida.lucene.spring;

import com.yida.lucene.core.EmbeddedLucene;
import com.yida.lucene.repository.ElRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

/**
 * 将扫描到的被@ElEntity标记的类注册到ioc容器中
 *
 * @author yida
 * @date 2024/8/25 12:28
 * @see com.yida.lucene.spring.annotation.ElEntity
 */
@RequiredArgsConstructor
class ElRepositoryRegistry implements BeanDefinitionRegistryPostProcessor {

	private final EmbeddedLucene lucene;

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		for (Class<?> entity : lucene.getLoadedDocClass()) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(
					ElRepository.class,
					() -> ElRepository.get(lucene.getSource(entity))
			);
			beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(ElRepository.class, entity));
			registry.registerBeanDefinition(entity.getName() + ":" + ElRepository.class.getName(), beanDefinition);
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

}
