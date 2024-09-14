package com.yida.lucene.spring.factory;

/**
 * @author yida
 * @package com.yida.lucene.spring.factory
 * @date 2024-09-13 14:14
 * @description Type your description over here.
 */
public class AnalyzerMapping {
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private String analyzer;
	private boolean useSmart;
	private int maxTokenLength;
	private String stopWordDictPath;

	public AnalyzerMapping(String analyzer, boolean useSmart) {
		this(analyzer, useSmart, DEFAULT_MAX_TOKEN_LENGTH);
	}

	public AnalyzerMapping(String analyzer, boolean useSmart, int maxTokenLength) {
		this(analyzer, useSmart, maxTokenLength, null);
	}

	public AnalyzerMapping(String analyzer, boolean useSmart, int maxTokenLength, String stopWordDictPath) {
		this.analyzer = analyzer;
		this.useSmart = useSmart;
		this.maxTokenLength = maxTokenLength;
		this.stopWordDictPath = stopWordDictPath;
	}

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	public boolean isUseSmart() {
		return useSmart;
	}

	public void setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
	}

	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	public void setMaxTokenLength(int maxTokenLength) {
		this.maxTokenLength = maxTokenLength;
	}

	public String getStopWordDictPath() {
		return stopWordDictPath;
	}

	public void setStopWordDictPath(String stopWordDictPath) {
		this.stopWordDictPath = stopWordDictPath;
	}
}
