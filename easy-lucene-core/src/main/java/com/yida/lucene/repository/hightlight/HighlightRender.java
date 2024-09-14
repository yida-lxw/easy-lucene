package com.yida.lucene.repository.hightlight;

import com.yida.lucene.exception.EmbeddedLuceneException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author yida
 * @date 2024/8/31 11:20
 */
public class HighlightRender {


	public static HighlightRender getDefault() {
		return new HighlightRender();
	}

	private Analyzer analyzer;
	private Highlighter highlighter;
	private final Formatter formatter;
	private boolean initialed = false;
	private final Set<String> renderFieldSet = new HashSet<>();

	public HighlightRender() {
		this(Formatters.DEFAULT);
	}

	public HighlightRender(Formatter formatter) {
		this.formatter = formatter;
	}

	public static void init(HighlightRender render, Query query, Analyzer analyzer) {
		if (Objects.isNull(render)) {
			return;
		}
		render.highlighter = new Highlighter(render.formatter, new QueryScorer(query));
		render.analyzer = analyzer;
		render.initialed = true;
	}

	@SuppressWarnings("all")
	public HighlightRender addRenderField(String field) {
		renderFieldSet.add(field);
		return this;
	}

	public String render(String fieldName, String text) {
		if (initialed && renderFieldSet.contains(fieldName)) {
			try {
				return highlighter.getBestFragment(analyzer, fieldName, text);
			} catch (IOException | InvalidTokenOffsetsException e) {
				throw EmbeddedLuceneException.of(e);
			}
		}
		return text;
	}

}
