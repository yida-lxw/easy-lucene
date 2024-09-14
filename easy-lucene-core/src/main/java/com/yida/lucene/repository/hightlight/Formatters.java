package com.yida.lucene.repository.hightlight;

import lombok.experimental.UtilityClass;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

/**
 * @author yida
 * @date 2024/8/31 3:53
 */
@UtilityClass
public class Formatters {

	private final String COLOR_RED = "red";
	private final String B_PRE = "<B style=\"color: %s\">";
	private final String B_POST = "</B>";

	public final Formatter DEFAULT = bColor(COLOR_RED);

	public Formatter bColor(String color) {
		return new SimpleHTMLFormatter(String.format(B_PRE, color), B_POST);
	}

}
