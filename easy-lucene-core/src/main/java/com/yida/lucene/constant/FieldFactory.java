package com.yida.lucene.constant;

import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.util.ByteUtil;
import lombok.experimental.UtilityClass;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

/**
 * todo 支持更多类型
 *
 * @author yida
 * @date 2024/8/31 4:52
 */
@UtilityClass
public class FieldFactory {

	public Field create(Class<? extends IndexableField> fieldClass, String name, Object data) {
		if (SortedDocValuesField.class.equals(fieldClass)) {
			byte[] bytes;
			if (data instanceof String) {
				bytes = data.toString().getBytes();
			} else {
				bytes = ByteUtil.numberToBytes((Number) data);
			}
			return new SortedDocValuesField(name, new BytesRef(bytes));
		} else {
			throw EmbeddedLuceneException.of("unsupported field class : " + fieldClass.getName());
		}
	}
}
