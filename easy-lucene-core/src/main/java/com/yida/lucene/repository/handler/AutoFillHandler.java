package com.yida.lucene.repository.handler;

import com.yida.lucene.annotation.AutoFill;
import com.yida.lucene.constant.FieldType;
import com.yida.lucene.core.DocFactory;
import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.plugin.Invocation;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author yida
 * @date 2024/9/1 09:59
 */
@RequiredArgsConstructor
public class AutoFillHandler implements RepositoryHandler {

	private final AutoFillValProvider provider;

	@Override
	public int order() {
		return 0;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void beforeInsert(Invocation invocation) {
		DocFactory docFactory = RepositoryHandler.getReposity(invocation).getSource().getDocFactory();
		Map<Field, AutoFill> autoFillMap = docFactory.getAutoFillMap();
		Object entity = invocation.getArgs()[0];
		if (entity instanceof Collection) {
			for (Object data : ((Collection) entity)) {
				handleFill(data, autoFillMap, docFactory);
			}
		} else {
			handleFill(entity, autoFillMap, docFactory);
		}
	}

	@SuppressWarnings({"rawtypes"})
	private void handleFill(Object entity, Map<Field, AutoFill> autoFillMap, DocFactory docFactory) {
		for (Field field : autoFillMap.keySet()) {
			AutoFill autoFill = autoFillMap.get(field);
			String name = field.getName();
			FieldType fieldType = docFactory.getFieldType(name);
			Class<?> type = field.getType();
			try {
				if (autoFill.cover() || Objects.isNull(field.get(entity))) {
					field.set(entity, provider.getVal(name, type, fieldType));
				}
			} catch (IllegalAccessException e) {
				throw EmbeddedLuceneException.of(e);
			}
		}
	}
}
