package com.yida.lucene.spring.enums;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yida
 * @package com.yida.lucene.spring.enums
 * @date 2024-09-13 13:59
 * @description 分词器类型枚举
 */
public enum AnalyzerType {
	IK(AnalyzerType.IK_ANALYZER),
	WHITESPACE(AnalyzerType.WHITESPACE_ANALYZER),
	SIMPLE(AnalyzerType.SIMPLE_ANALYZER),
	STANDARD(AnalyzerType.STANDARD_ANALYZER),
	STOP(AnalyzerType.STOP_ANALYZER),
	KEYWORD(AnalyzerType.KEYWORD_ANALYZER);

	private String analyzerName;

	AnalyzerType(String analyzerName) {
		this.analyzerName = analyzerName;
	}


	public static final String IK_ANALYZER = "ik";
	public static final String WHITESPACE_ANALYZER = "whitespace";
	public static final String SIMPLE_ANALYZER = "simple";
	public static final String STANDARD_ANALYZER = "standard";
	public static final String STOP_ANALYZER = "stop";
	public static final String KEYWORD_ANALYZER = "keyword";

	public static final Set<String> analyzerNameSet = new HashSet<>();

	static {
		analyzerNameSet.add(IK_ANALYZER);
		analyzerNameSet.add(WHITESPACE_ANALYZER);
		analyzerNameSet.add(SIMPLE_ANALYZER);
		analyzerNameSet.add(STANDARD_ANALYZER);
		analyzerNameSet.add(STOP_ANALYZER);
		analyzerNameSet.add(KEYWORD_ANALYZER);
	}

	public static AnalyzerType of(String analyzerName) {
		if (null == analyzerName || "".equals(analyzerName)) {
			throw new IllegalArgumentException("The paramenter analyzerName MUST NOT be NULL or empty.");
		}
		if (!analyzerNameSet.contains(analyzerName)) {
			throw new IllegalArgumentException("The paramenter analyzerName MUST be in the set of analyzerNameSet.");
		}
		if (IK_ANALYZER.equals(analyzerName)) {
			return IK;
		}
		if (WHITESPACE_ANALYZER.equals(analyzerName)) {
			return WHITESPACE;
		}
		if (SIMPLE_ANALYZER.equals(analyzerName)) {
			return SIMPLE;
		}
		if (STANDARD_ANALYZER.equals(analyzerName)) {
			return STANDARD;
		}
		if (STOP_ANALYZER.equals(analyzerName)) {
			return STOP;
		}
		return KEYWORD;
	}

	public static boolean containsOfThis(String analyzerName) {
		if (null == analyzerName || "".equals(analyzerName)) {
			return false;
		}
		return analyzerNameSet.contains(analyzerName);
	}

	public String getAnalyzerName() {
		return analyzerName;
	}

	public void setAnalyzerName(String analyzerName) {
		this.analyzerName = analyzerName;
	}
}
