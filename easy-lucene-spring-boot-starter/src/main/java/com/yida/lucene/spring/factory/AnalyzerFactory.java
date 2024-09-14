package com.yida.lucene.spring.factory;

import com.yida.lucene.spring.enums.AnalyzerType;
import com.yida.lucene.spring.util.ResourceFileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.util.Arrays;
import java.util.List;

/**
 * @author yida
 * @package com.yida.lucene.spring.factory
 * @date 2024-09-13 14:13
 * @description Type your description over here.
 */
public class AnalyzerFactory {
	public static Analyzer build(AnalyzerType analyzerType, AnalyzerMapping analyzerMapping) {
		if (AnalyzerType.IK.equals(analyzerType)) {
			boolean useSmart = (null != analyzerMapping) ? analyzerMapping.isUseSmart() : false;
			return new IKAnalyzer(useSmart);
		}
		if (AnalyzerType.WHITESPACE.equals(analyzerType)) {
			int maxTokenLength = (null != analyzerMapping) ? analyzerMapping.getMaxTokenLength() : AnalyzerMapping.DEFAULT_MAX_TOKEN_LENGTH;
			return new WhitespaceAnalyzer(maxTokenLength);
		}
		if (AnalyzerType.SIMPLE.equals(analyzerType)) {
			return new SimpleAnalyzer();
		}
		if (AnalyzerType.STANDARD.equals(analyzerType)) {
			StandardAnalyzer standardAnalyzer = null;
			int maxTokenLength = (null != analyzerMapping) ? analyzerMapping.getMaxTokenLength() : AnalyzerMapping.DEFAULT_MAX_TOKEN_LENGTH;
			String stopWordDictPath = analyzerMapping.getStopWordDictPath();
			if (null != stopWordDictPath && stopWordDictPath.length() > 0) {
				String stopwords = ResourceFileUtils.readResourceFile(stopWordDictPath);
				if (null != stopwords && stopwords.length() > 0) {
					String[] stopwrodArray = stopwords.split("\n");
					List<String> stopwordList = Arrays.asList(stopwrodArray);
					CharArraySet charArraySet = new CharArraySet(stopwordList, true);
					standardAnalyzer = new StandardAnalyzer(charArraySet);
				} else {
					standardAnalyzer = new StandardAnalyzer();
				}
			} else {
				standardAnalyzer = new StandardAnalyzer();
			}
			standardAnalyzer.setMaxTokenLength(maxTokenLength);
			return standardAnalyzer;
		}
		if (AnalyzerType.STOP.equals(analyzerType)) {
			StopAnalyzer stopAnalyzer = null;
			String stopWordDictPath = analyzerMapping.getStopWordDictPath();
			if (null != stopWordDictPath && stopWordDictPath.length() > 0) {
				String stopwords = ResourceFileUtils.readResourceFile(stopWordDictPath);
				if (null != stopwords && stopwords.length() > 0) {
					String[] stopwrodArray = stopwords.split("\n");
					List<String> stopwordList = Arrays.asList(stopwrodArray);
					CharArraySet charArraySet = new CharArraySet(stopwordList, true);
					stopAnalyzer = new StopAnalyzer(charArraySet);
				} else {
					stopAnalyzer = new StopAnalyzer(CharArraySet.EMPTY_SET);
				}
			} else {
				stopAnalyzer = new StopAnalyzer(CharArraySet.EMPTY_SET);
			}
			return stopAnalyzer;
		}
		if (AnalyzerType.KEYWORD.equals(analyzerType)) {
			return new KeywordAnalyzer();
		}
		return new KeywordAnalyzer();
	}
}
