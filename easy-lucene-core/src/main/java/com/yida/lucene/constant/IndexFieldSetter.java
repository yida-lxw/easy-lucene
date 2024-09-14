package com.yida.lucene.constant;

import com.yida.lucene.bean.LatLon;
import com.yida.lucene.exception.ElAssert;
import com.yida.lucene.exception.EmbeddedLuceneException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yida
 * @date 2024/8/9 10:06
 */
@FunctionalInterface
interface IndexFieldSetter {

	/**
	 * 向document添加IndexableField
	 *
	 * @param name            key
	 * @param data            value
	 * @param store           是否存储
	 * @param document        文档
	 * @param extensionFields 扩展域
	 */
	void set(String name, Object data, boolean store, Document document, Class<? extends IndexableField>[] extensionFields);

	/**
	 * get setter
	 *
	 * @param fieldType 字段类型
	 * @return 字段setter
	 */
	static IndexFieldSetter getSetter(FieldType fieldType) {
		return Facotry.HOLDER.get(fieldType);
	}

	@SuppressWarnings("all")
	class Facotry {
		private static final Map<FieldType, IndexFieldSetter> HOLDER = new HashMap<>();

		static {
			HOLDER.put(
					FieldType.BOOL,
					(name, data, store, document, extensionFields) -> {
						ElAssert.isTrue(data instanceof Boolean, "data is not type of Boolean");
						document.add(new StringField(name, data.toString(), store ? Field.Store.YES : Field.Store.NO));
						if (null != extensionFields && extensionFields.length != 0) {
							Arrays.stream(extensionFields)
									.filter(field -> !field.equals(StringField.class))
									.forEach(field -> {
										document.add(FieldFactory.create(field, name, data));
									});
						}
					});
			HOLDER.put(
					FieldType.TEXT,
					(name, data, store, document, extensionFields) -> {
						ElAssert.isTrue(data instanceof String, "data is not type of String");
						document.add(new TextField(name, (String) data, store ? Field.Store.YES : Field.Store.NO));
						if (null != extensionFields && extensionFields.length != 0) {
							Arrays.stream(extensionFields)
									.filter(field -> !field.equals(TextField.class))
									.forEach(field -> {
										document.add(FieldFactory.create(field, name, data));
									});
						}
					});
			HOLDER.put(
					FieldType.STRING,
					(name, data, store, document, extensionFields) -> {
						ElAssert.isTrue(data instanceof String, "data is not type of String");
						document.add(new StringField(name, (String) data, store ? Field.Store.YES : Field.Store.NO));
						if (null != extensionFields && extensionFields.length != 0) {
							Arrays.stream(extensionFields)
									.filter(field -> !field.equals(StringField.class))
									.forEach(field -> {
										document.add(FieldFactory.create(field, name, data));
									});
						}
					});
			HOLDER.put(
					FieldType.DATE,
					(name, data, store, document, extensionFields) -> {
						long milliseconds = 0L;
						if (data instanceof Date) {
							Date time = (Date) data;
							milliseconds = time.getTime();
						} else if (data instanceof LocalDateTime) {
							LocalDateTime time = (LocalDateTime) data;
							milliseconds = time.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
						} else if (data instanceof LocalDate) {
							LocalDate time = (LocalDate) data;
							milliseconds = time.atStartOfDay().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
						} else {
							throw EmbeddedLuceneException.of("data's type:" + data.getClass() + "is not support");
						}
						document.add(new NumericDocValuesField(name, milliseconds));
						if (store) {
							document.add(new StoredField(name, milliseconds));
						}
						if (null != extensionFields && extensionFields.length != 0) {
							Arrays.stream(extensionFields)
									.filter(field -> !field.equals(NumericDocValuesField.class))
									.filter(field -> !field.equals(StoredField.class))
									.forEach(field -> {
										document.add(FieldFactory.create(field, name, data));
									});
						}
					});
			HOLDER.put(
					FieldType.LONG,
					(name, data, store, document, extensionFields) -> {
						ElAssert.isTrue(data instanceof Long, "data is not type of Long");
						Long longValue = (Long) data;
						document.add(new NumericDocValuesField(name, longValue));
						if (store) {
							document.add(new StoredField(name, longValue));
						}
						if (null != extensionFields && extensionFields.length != 0) {
							Arrays.stream(extensionFields)
									.filter(field -> !field.equals(NumericDocValuesField.class))
									.filter(field -> !field.equals(StoredField.class))
									.forEach(field -> {
										document.add(FieldFactory.create(field, name, data));
									});
						}
					});
			HOLDER.put(
					FieldType.INT,
					(name, data, store, document, extensionFields) -> {
						ElAssert.isTrue(data instanceof Integer, "data is not type of Integer");
						Integer intValue = (Integer) data;
						document.add(new NumericDocValuesField(name, intValue));
						if (store) {
							document.add(new StoredField(name, intValue));
						}
						if (null != extensionFields && extensionFields.length != 0) {
							Arrays.stream(extensionFields)
									.filter(field -> !field.equals(NumericDocValuesField.class))
									.filter(field -> !field.equals(StoredField.class))
									.forEach(field -> {
										document.add(FieldFactory.create(field, name, data));
									});
						}
					});

			HOLDER.put(FieldType.LATLON,
					(name, data, store, document, extensionFields) -> {
						ElAssert.isTrue(data instanceof LatLon, "data is not type of LatLon");
						LatLon latLon = (LatLon) data;
						document.add(new LatLonPoint(name, latLon.getLatitude(), latLon.getLongitude()));
						if (store) {
							document.add(new StoredField(name, latLon.getLatitude() + "," + latLon.getLongitude()));
						}
						if (null != extensionFields && extensionFields.length != 0) {
							Arrays.stream(extensionFields)
									.filter(field -> !field.equals(LatLonPoint.class))
									.filter(field -> !field.equals(StoredField.class))
									.forEach(field -> {
										document.add(FieldFactory.create(field, name, data));
									});
						}
					});
		}
	}
}
