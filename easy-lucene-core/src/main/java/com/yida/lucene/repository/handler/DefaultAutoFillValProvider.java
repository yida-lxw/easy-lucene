package com.yida.lucene.repository.handler;

import com.yida.lucene.constant.FieldType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author yida
 * @date 2024/9/6 09:20
 */
public class DefaultAutoFillValProvider implements AutoFillValProvider {

	private final Map<String, Supplier<?>> map = new HashMap<>();

	{
		map.put("deleted", () -> false);
		map.put("time", LocalDateTime::now);
	}

	@Override
	public Object getVal(String fieldName, Class<?> fieldClass, FieldType fieldType) {
		Supplier<?> supplier = map.get(fieldName);
		if (Objects.nonNull(supplier)) {
			return supplier.get();
		}
		return null;
	}

}
