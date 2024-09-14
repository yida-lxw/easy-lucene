package com.yida.lucene.spring;

import com.yida.lucene.core.EmbeddedLucene;
import com.yida.lucene.repository.handler.AutoFillHandler;
import com.yida.lucene.repository.handler.DefaultAutoFillValProvider;
import com.yida.lucene.repository.handler.LogHandler;
import com.yida.lucene.repository.handler.RepositoryHandler;
import com.yida.lucene.spring.annotation.ElEntity;
import com.yida.lucene.spring.annotation.EnableEmbeddedLucene;
import com.yida.lucene.spring.enums.AnalyzerType;
import com.yida.lucene.spring.factory.AnalyzerFactory;
import com.yida.lucene.spring.factory.AnalyzerMapping;
import com.yida.lucene.spring.util.BeanScanUtils;
import com.yida.lucene.spring.util.JacksonUtils;
import com.yida.lucene.spring.util.ResourceFileUtils;
import com.yida.lucene.transaction.ElTransactionManager;
import com.yida.lucene.transaction.ElTransactionTemplate;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yida
 * @date 2024/08/25 2:01
 */
@EnableAspectJAutoProxy
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EmbeddedLucene.class)
public class EmbeddedLuceneAutoConfiguration implements EnvironmentAware {
	private EmbeddedLuceneProperties properties;

	@Bean
	@ConditionalOnClass(value = EnableEmbeddedLucene.class)
	public RepositoryHandler logHandler() {
		return new LogHandler();
	}

	@Bean
	@ConditionalOnClass(value = EnableEmbeddedLucene.class)
	public RepositoryHandler autoFillHandler() {
		return new AutoFillHandler(new DefaultAutoFillValProvider());
	}

	/**
	 * Field对应的默认分词器映射
	 */
	@Bean
	public FieldTypeAnalyzerMapper fieldTypeAnalyzerMapper() {
		return new DefaultFieldTypeAnalyzerMapper(properties.isIkUseSmart());
	}

	@Bean("analyzers")
	@ConditionalOnClass(value = EnableEmbeddedLucene.class)
	public Map<String, Analyzer> analyzers() {
		Map<String, Analyzer> analyzerMap = new HashMap<>();
		Map<String, Class<Object>> classMap = BeanScanUtils.scanBean(properties.getEntityPackages(), ElEntity.class);
		if (null != classMap && classMap.size() > 0) {
			for (Map.Entry<String, Class<Object>> entry : classMap.entrySet()) {
				Class<Object> entityClass = entry.getValue();
				ElEntity elEntity = entityClass.getAnnotation(ElEntity.class);
				String analyzerMappingJSONFilePath = elEntity.analyzerMappingJSONFilePath();
				if (null == analyzerMappingJSONFilePath || "".equals(analyzerMappingJSONFilePath)) {
					continue;
				}
				String analyzerMappingJSONString = ResourceFileUtils.readResourceFile(analyzerMappingJSONFilePath);
				if (null == analyzerMappingJSONString || "".equals(analyzerMappingJSONString)) {
					continue;
				}
				Map<String, Map<String, Object>> analyzerMappingJSONMap = JacksonUtils.json2Map(analyzerMappingJSONString);
				Field[] fields = entityClass.getDeclaredFields();
				Map<String, Analyzer> fieldAnalyzerMap = new HashMap<>(8);
				for (Field field : fields) {
					String fieldName = field.getName();
					int modifiers = field.getModifiers();
					boolean isStatic = Modifier.isStatic(modifiers);
					if (isStatic) {
						continue;
					}
					Map<String, Object> analyzerMapping = analyzerMappingJSONMap.get(fieldName);
					if (null == analyzerMapping) {
						analyzerMapping = new HashMap<>();
						analyzerMapping.put("analyzer", AnalyzerType.KEYWORD_ANALYZER);
						analyzerMapping.put("use_smart", false);
						analyzerMapping.put("max_token_length", AnalyzerMapping.DEFAULT_MAX_TOKEN_LENGTH);
						analyzerMapping.put("stop_word_dict_path", null);
					}
					String analyzer = AnalyzerType.KEYWORD_ANALYZER;
					Object analyzerObj = analyzerMapping.get("analyzer");
					if (null != analyzerObj) {
						analyzer = analyzerObj.toString();
					}
					boolean useSmart = false;
					Object useSmartObj = analyzerMapping.get("use_smart");
					if (null != useSmartObj) {
						useSmart = Boolean.valueOf(useSmartObj.toString());
					}
					int maxTokenLength = AnalyzerMapping.DEFAULT_MAX_TOKEN_LENGTH;
					Object maxTokenLengthObj = analyzerMapping.get("max_token_length");
					if (null != maxTokenLengthObj) {
						maxTokenLength = Integer.valueOf(maxTokenLengthObj.toString());
					}
					String stopWordDictPath = null;
					Object stopWordDictPathObj = analyzerMapping.get("stop_word_dict_path");
					if (null != stopWordDictPathObj) {
						stopWordDictPath = stopWordDictPathObj.toString();
					}
					AnalyzerType analyzerType = AnalyzerType.of(analyzer);
					AnalyzerMapping analyzerMappingBean = new AnalyzerMapping(analyzer, useSmart, maxTokenLength, stopWordDictPath);
					Analyzer analyzer4Field = AnalyzerFactory.build(analyzerType, analyzerMappingBean);
					fieldAnalyzerMap.put(fieldName, analyzer4Field);
				}
				PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), fieldAnalyzerMap);
				String entityClassName = entityClass.getName();
				String analyzerKey = entityClassName + "Analyzer";
				analyzerMap.put(analyzerKey, perFieldAnalyzerWrapper);
			}
		}
		return analyzerMap;
	}

	@Bean("springEmbeddedLucene")
	@DependsOn("analyzers")
	@ConditionalOnMissingBean(SpringEmbeddedLucene.class)
	public SpringEmbeddedLucene springEmbeddedLucene(List<RepositoryHandler> handlers,
													 Map<String, Analyzer> analyzers,
													 @Nullable FieldTypeAnalyzerMapper fieldTypeAnalyzerMapper) {
		return new SpringEmbeddedLucene(
				SpringEmbeddedLucene.buildConfig(
						properties,
						handlers,
						fieldTypeAnalyzerMapper,
						analyzers
				)
		);
	}

	@Bean
	@ConditionalOnBean(SpringEmbeddedLucene.class)
	public ElRepositoryRegistry elRepositoryRegistry(SpringEmbeddedLucene embeddedLucene) {
		return new ElRepositoryRegistry(embeddedLucene);
	}

	@Bean
	@ConditionalOnBean(SpringEmbeddedLucene.class)
	@ConditionalOnMissingBean(ElTransactionManager.class)
	public ElTransactionManager elTransactionManager(SpringEmbeddedLucene embeddedLucene) {
		return embeddedLucene.getTxManager();
	}

	@Bean
	@ConditionalOnBean(ElTransactionManager.class)
	@ConditionalOnMissingBean(ElTransactionTemplate.class)
	public ElTransactionTemplate elTransactionTemplate(ElTransactionManager elTransactionManager) {
		return new ElTransactionTemplate(elTransactionManager);
	}

	@Bean
	@ConditionalOnBean(ElTransactionTemplate.class)
	@ConditionalOnMissingBean(ElTransactionAspect.class)
	public ElTransactionAspect elTransactionAspect(ElTransactionTemplate elTransactionTemplate) {
		return new ElTransactionAspect(elTransactionTemplate);
	}

	@Override
	public void setEnvironment(Environment environment) {
		properties = Binder.get(environment)
				.bindOrCreate(
						EmbeddedLuceneProperties.PREFIX,
						EmbeddedLuceneProperties.class
				);
	}

}
