package com.yida.lucene.constant;

import com.yida.lucene.bean.LatLon;
import com.yida.lucene.exception.EmbeddedLuceneException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yida
 * @date 2024/8/22 1:15
 */
public interface TypeChecker {

	/**
	 * get
	 *
	 * @param fieldType fieldType
	 * @return TypeChecker
	 */
	static TypeChecker getChecker(FieldType fieldType) {
		return Factory.HOLDER.get(fieldType);
	}

	class Factory {
		private static final Map<FieldType, TypeChecker> HOLDER = new HashMap<>();

		static {
			HOLDER.put(
					FieldType.BOOL,
					object -> object instanceof Boolean
			);
			TypeChecker stringTypeChecker = object -> object instanceof String;
			HOLDER.put(
					FieldType.TEXT,
					stringTypeChecker
			);
			HOLDER.put(
					FieldType.STRING,
					stringTypeChecker
			);
			HOLDER.put(
					FieldType.LONG,
					object -> object instanceof Long
			);
			HOLDER.put(
					FieldType.INT,
					object -> object instanceof Integer
			);
			HOLDER.put(
					FieldType.DATE,
					object -> object instanceof Date || object instanceof LocalDate || object instanceof LocalDateTime
			);
			HOLDER.put(
					FieldType.LATLON,
					object -> object instanceof LatLon
			);
		}
	}

	/**
	 * 校验
	 *
	 * @param object o
	 * @return 结果
	 */
	boolean check(Object object);

	/**
	 * 校验失败时抛出异常
	 *
	 * @param object o
	 */
	default void checkThenThrowIfNeed(Object object) {
		if (!check(object)) {
			throw EmbeddedLuceneException.of("type detection failed");
		}
	}


}
