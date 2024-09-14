package com.yida.lucene.spring;

import com.yida.lucene.constant.FieldType;
import com.yida.lucene.core.EmbeddedLucene;
import com.yida.lucene.core.EmbeddedLuceneConfig;
import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.repository.handler.RepositoryHandler;
import com.yida.lucene.spring.annotation.ElEntity;
import com.yida.lucene.spring.util.AnnotationScanner;
import org.apache.lucene.analysis.Analyzer;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yida
 * @date 2024/8/26 12:18
 */
public class SpringEmbeddedLucene extends EmbeddedLucene implements DisposableBean {

	public SpringEmbeddedLucene(EmbeddedLuceneConfig config) {
		super(config);
		this.start();
	}

	static EmbeddedLuceneConfig buildConfig(
			EmbeddedLuceneProperties embeddedLuceneProperties,
			List<RepositoryHandler> handlers,
			FieldTypeAnalyzerMapper fieldTypeAnalyzerMapper,
			Map<String, Analyzer> analyzers
	) {
		boolean ikUseSmart = embeddedLuceneProperties.isIkUseSmart();
		Map<Class<?>, ElEntity> classElEntityMap = AnnotationScanner.scanAnno(embeddedLuceneProperties.getEntityPackages(), ElEntity.class);
		fieldTypeAnalyzerMapper = fieldTypeAnalyzerMapper == null ? new DefaultFieldTypeAnalyzerMapper(ikUseSmart) : fieldTypeAnalyzerMapper;
		EmbeddedLuceneConfig.Builder configBuilder = EmbeddedLuceneConfig.builder()
				.workerThreadNum(embeddedLuceneProperties.getWorkerThreadNum())
				.indexPath(embeddedLuceneProperties.getIndexPath())
				.repositoryHandler(handlers);
		Map<FieldType, Analyzer> fieldTypeAnalyzerMap = fieldTypeAnalyzerMapper.get();
		if (null != fieldTypeAnalyzerMap) {
			//fieldTypeAnalyzerMap.forEach(configBuilder::fieldTypeAnalyzer);
			fieldTypeAnalyzerMap.forEach((FieldType fieldType, Analyzer analyzer) -> {
				configBuilder.fieldTypeAnalyzer(fieldType, analyzer);
			});
		}

		for (Class<?> entity : classElEntityMap.keySet()) {
			ElEntity elEntity = classElEntityMap.get(entity);
			String entityClassName = entity.getName();
			String docName = elEntity.value();
			if (null != docName && !docName.isEmpty()) {
				docName = entity.getName();
			}
			String analyzerBeanName = elEntity.analyzerBeanName();
			if (null == analyzerBeanName || "".equals(analyzerBeanName)) {
				analyzerBeanName = entityClassName + "Analyzer";
			}
			if (null != docName && !analyzerBeanName.isEmpty()) {
				try {
					Analyzer analyzer = analyzers.get(analyzerBeanName);
					if (Objects.nonNull(analyzer)) {
						configBuilder.registerSource(entity, docName, analyzer);
					} else {
						throw EmbeddedLuceneException.of("You don't setting Analyzer for the field required analyzed of Entity:[" + entityClassName + "].");
					}
				} catch (Exception e) {
					throw EmbeddedLuceneException.of(e);
				}
			} else {
				configBuilder.registerSource(entity, docName);
			}
		}
		return configBuilder.build();
	}

	@Override
	public void destroy() {
		this.close();
	}

}
