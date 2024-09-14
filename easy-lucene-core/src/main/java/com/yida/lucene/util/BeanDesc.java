package com.yida.lucene.util;

import lombok.AllArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yida
 * @date 2024/09/25 14:54
 */
public class BeanDesc {

	private final Map<String, PropDesc> propDescMap;

	public BeanDesc(Class<?> beanClass) {
		Field[] declaredFields = beanClass.getDeclaredFields();
		propDescMap = Arrays.stream(declaredFields)
				.peek(field -> field.setAccessible(true))
				.filter(field ->
						!Modifier.isStatic(field.getModifiers())
								|| !Modifier.isFinal(field.getModifiers())
				)
				.map(field -> {
					String name = field.getName();
					name = name.substring(0, 1).toUpperCase() + name.substring(1);
					Class<?> type = field.getType();
					Method getter = null;
					Method setter = null;
					try {
						if (Boolean.class.equals(type) || boolean.class.equals(type)) {
							getter = beanClass.getDeclaredMethod("is" + name);
						} else {
							getter = beanClass.getDeclaredMethod("get" + name);
						}
						setter = beanClass.getDeclaredMethod("set" + name, type);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
					return new PropDesc(field.getName(), getter, setter);
				}).collect(Collectors.toMap(item -> item.fieldName, Function.identity()));
	}

	public Method getGetter(String fieldName) {
		PropDesc propDesc = propDescMap.get(fieldName);
		if (propDesc != null) {
			return propDesc.getter;
		}
		return null;
	}

	public Method getSetter(String fieldName) {
		PropDesc propDesc = propDescMap.get(fieldName);
		if (propDesc != null) {
			return propDesc.setter;
		}
		return null;
	}

	@AllArgsConstructor
	private static class PropDesc {
		private String fieldName;
		private Method getter;
		private Method setter;
	}

}
