package com.yida.lucene.exception;

/**
 * @author yida
 * @date 2024/8/9 10:11
 */
public class EmbeddedLuceneException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static EmbeddedLuceneException of(String message) {
		return new EmbeddedLuceneException(message);
	}

	public static EmbeddedLuceneException of(Throwable cause) {
		if (cause instanceof EmbeddedLuceneException) {
			return (EmbeddedLuceneException) cause;
		}
		return new EmbeddedLuceneException(cause);
	}

	public static EmbeddedLuceneException of(String message, Throwable cause) {
		return new EmbeddedLuceneException(message, cause);
	}

	private EmbeddedLuceneException(String message) {
		super(message);
	}

	private EmbeddedLuceneException(Throwable cause) {
		super(cause);
	}

	private EmbeddedLuceneException(String message, Throwable cause) {
		super(message, cause);
	}

}
