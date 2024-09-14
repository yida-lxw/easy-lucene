package com.yida.lucene.core;

import com.yida.lucene.repository.hightlight.HighlightRender;
import lombok.SneakyThrows;
import org.apache.lucene.document.Document;

import java.util.Map;
import java.util.Set;

/**
 * 懒加载模式
 *
 * @author yida
 * @date 2024/09/18 4:32
 */
public class ElDocument<T> {

	private final int docId;
	private final Source<T> source;
	private final DocFactory<T> docFactory;
	private final HighlightRender highlightRender;
	private final Set<String> selectFieldSet;

	private Document document;
	private Map<String, String> map;
	private T bean;

	public ElDocument(int docId, Source<T> source, HighlightRender highlightRender, Set<String> selectFieldSet) {
		this.docId = docId;
		this.source = source;
		this.docFactory = source.getDocFactory();
		this.highlightRender = highlightRender;
		this.selectFieldSet = selectFieldSet;
	}

	@SneakyThrows
	public Document getDocument() {
		if (null == document) {
			if (null == selectFieldSet || selectFieldSet.isEmpty()) {
				document = docFactory.renderHighlight(source.getSearchHolder().getIndexSearcher().doc(docId), highlightRender);
			} else {
				document = docFactory.renderHighlight(source.getSearchHolder().getIndexSearcher().doc(docId, selectFieldSet), highlightRender);
			}
		}
		return document;
	}

	public Map<String, String> getMap() {
		if (null == bean) {
			map = docFactory.toMap(getDocument());
		}
		return map;
	}

	public T getJavaBean() {
		if (null == bean) {
			bean = docFactory.toJavaBean(getMap());
		}
		return bean;
	}

}
