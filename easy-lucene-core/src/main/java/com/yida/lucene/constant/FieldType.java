package com.yida.lucene.constant;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * @author yida
 * @date 2024/8/4 3:31
 */
@SuppressWarnings("all")
@RequiredArgsConstructor
public enum FieldType implements IndexFieldSetter, IndexFieldGetter {

	/**
	 * @see org.apache.lucene.document.TextField
	 */
	TEXT,

	/**
	 * @see org.apache.lucene.document.StringField
	 */
	STRING,

	/**
	 * 转换成 {@link FieldType.LONG} 存储
	 */
	DATE,

	/**
	 * @see org.apache.lucene.document.NumericDocValuesField
	 */
	INT,

	/**
	 * @see org.apache.lucene.document.NumericDocValuesField
	 */
	LONG,

	/**
	 * @see org.apache.lucene.document.StringField
	 */
	BOOL,

	/**
	 * @see org.apache.lucene.document.LatLonPoint
	 */
	LATLON;

	@Override
	public void set(String name, Object data, boolean store, Document container, Class<? extends IndexableField>[] extensionFields) {
		if (null == data) {
			return;
		}
		IndexFieldSetter.getSetter(this).set(name, data, store, container, extensionFields);
	}

	@Override
	public Object get(String value, Class<?> targetClass) {
		return IndexFieldGetter.getGetter(this).get(value, targetClass);
	}

}
