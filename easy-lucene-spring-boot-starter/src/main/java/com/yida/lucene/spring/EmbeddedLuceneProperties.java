package com.yida.lucene.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yida
 * @date 2024/8/25 2:03
 */
@Data
@ConfigurationProperties(prefix = EmbeddedLuceneProperties.PREFIX)
public class EmbeddedLuceneProperties {

	static final String PREFIX = "embedded-lucene";

	static final Set<String> DEFAULT_ENTITY_PACKAGES = new HashSet<>();

	static {
		DEFAULT_ENTITY_PACKAGES.add("");
	}

	static final String DEFAULT_INDEX_PATH = "./embedded-lucene-data";

	/**
	 * 要扫描的ElEntity所在的包
	 */
	private Set<String> entityPackages = DEFAULT_ENTITY_PACKAGES;

	/**
	 * 索引数据根目录
	 */
	private String indexPath = DEFAULT_INDEX_PATH;

	/**
	 * 工作线程数
	 */
	private int workerThreadNum = Runtime.getRuntime().availableProcessors();

	/**
	 * IK分词器是否开启Smart智能分词模式,默认不开启
	 */
	private boolean ikUseSmart;
}
