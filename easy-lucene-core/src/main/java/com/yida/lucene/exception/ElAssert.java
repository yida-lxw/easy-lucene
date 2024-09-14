package com.yida.lucene.exception;

import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * @author yida
 * @date 2024/8/9 10:11
 */
@UtilityClass
public class ElAssert {

	public void isNull(Object target, String message) {
		if (Objects.nonNull(target)) {
			throw EmbeddedLuceneException.of(message);
		}
	}

	public void nonNull(Object target, String message) {
		if (Objects.isNull(target)) {
			throw EmbeddedLuceneException.of(message);
		}
	}

	public void isTrue(boolean target, String message) {
		if (!target) {
			throw EmbeddedLuceneException.of(message);
		}
	}

	public void isFalse(boolean target, String message) {
		if (target) {
			throw EmbeddedLuceneException.of(message);
		}
	}

}
