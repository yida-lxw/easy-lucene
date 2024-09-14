package com.yida.lucene.constant;

import com.yida.lucene.bean.LatLon;
import com.yida.lucene.exception.EmbeddedLuceneException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yida
 * @date 2024/8/25 9:58
 */
@FunctionalInterface
interface IndexFieldGetter {

	/**
	 * /**
	 * 类型转换
	 *
	 * @param value       stringVal
	 * @param targetClass 目标类
	 * @return targetVal
	 */
	Object get(String value, Class<?> targetClass);

	/**
	 * get setter
	 *
	 * @param fieldType 字段类型
	 * @return 字段setter
	 */
	static IndexFieldGetter getGetter(FieldType fieldType) {
		return Facotry.HOLDER.get(fieldType);
	}

	@SuppressWarnings("all")
	class Facotry {
		private static final Map<FieldType, IndexFieldGetter> HOLDER = new HashMap<>();

		static {
			HOLDER.put(
					FieldType.BOOL,
					(value, targetClass) -> Boolean.parseBoolean(value)
			);
			HOLDER.put(
					FieldType.TEXT,
					(value, targetClass) -> value
			);
			HOLDER.put(
					FieldType.STRING,
					(value, targetClass) -> value
			);
			HOLDER.put(
					FieldType.DATE,
					(value, targetClass) -> {
						long timestamp = Long.parseLong(value);
						if (targetClass.isAssignableFrom(Date.class)) {
							return new Date(timestamp);
						} else if (targetClass.isAssignableFrom(LocalDateTime.class)) {
							return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
						} else if (targetClass.isAssignableFrom(LocalDate.class)) {
							return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalDate();
						} else {
							throw EmbeddedLuceneException.of("can not deserialize");
						}
					}
			);
			HOLDER.put(
					FieldType.LONG,
					(value, targetClass) -> Long.parseLong(value)
			);
			HOLDER.put(
					FieldType.INT,
					(value, targetClass) -> Integer.parseInt(value)
			);
			HOLDER.put(
					FieldType.LATLON,
					(value, targetClass) -> {
						String[] split = value.split(",");
						return LatLon.of(Double.valueOf(split[0]), Double.valueOf(split[1]));
					}
			);
		}
	}
}
