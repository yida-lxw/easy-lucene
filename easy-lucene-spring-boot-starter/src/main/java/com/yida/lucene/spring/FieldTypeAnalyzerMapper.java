package com.yida.lucene.spring;

import com.yida.lucene.constant.FieldType;
import org.apache.lucene.analysis.Analyzer;

import java.util.Map;

/**
 * @author yida
 * @date 2024/09/05 13:12
 */
public interface FieldTypeAnalyzerMapper {

	/**
	 * 获取字段类型和分词器的映射
	 *
	 * @return map
	 */
	Map<FieldType, Analyzer> get();
}
