package com.yida.lucene.spring;

import com.yida.lucene.constant.FieldType;
import org.apache.lucene.analysis.Analyzer;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yida
 * @date 2024/09/05 13:13
 */
public class DefaultFieldTypeAnalyzerMapper implements FieldTypeAnalyzerMapper {
	private boolean ikUseSamrt;

	public DefaultFieldTypeAnalyzerMapper() {
		this(false);
	}

	public DefaultFieldTypeAnalyzerMapper(boolean ikUseSamrt) {
		this.ikUseSamrt = ikUseSamrt;
	}

	@Override
	public Map<FieldType, Analyzer> get() {
		HashMap<FieldType, Analyzer> res = new HashMap<>(1);
		res.put(FieldType.TEXT, new IKAnalyzer(this.ikUseSamrt));
		return res;
	}
}
