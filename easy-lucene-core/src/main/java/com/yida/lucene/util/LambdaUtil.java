package com.yida.lucene.util;

import com.yida.lucene.exception.EmbeddedLuceneException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yida
 * @date 2024/09/25 13:35
 */
public class LambdaUtil {

	private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

	/**
	 * 获取lambda表达式Getter或Setter函数（方法）对应的字段名称，规则如下：
	 * <ul>
	 *     <li>getXxxx获取为xxxx，如getName得到name。</li>
	 *     <li>setXxxx获取为xxxx，如setName得到name。</li>
	 *     <li>isXxxx获取为xxxx，如isName得到name。</li>
	 *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
	 * </ul>
	 *
	 * @param <T>  Lambda类型
	 * @param func 函数（无参方法）
	 * @return 函数名称
	 * @throws IllegalArgumentException 非Getter或Setter方法
	 * @since 5.7.10
	 */
	public static <T> String getFieldName(SerializableFunction<T, ?> func) throws IllegalArgumentException {
		final String methodName = getMethodName(func);
		if (methodName.startsWith("get") || methodName.startsWith("set")) {
			return removePreAndLowerFirst(methodName, 3);
		} else if (methodName.startsWith("is")) {
			return removePreAndLowerFirst(methodName, 2);
		} else {
			throw new IllegalArgumentException("Invalid Getter or Setter name: " + methodName);
		}
	}

	/**
	 * 去掉首部指定长度的字符串并将剩余字符串首字母小写<br>
	 * 例如：str=setName, preLength=3 =》 return name
	 *
	 * @param str       被处理的字符串
	 * @param preLength 去掉的长度
	 * @return 处理后的字符串，不符合规范返回null
	 */
	private static String removePreAndLowerFirst(CharSequence str, int preLength) {
		if (str == null) {
			return null;
		}
		if (str.length() > preLength) {
			char first = Character.toLowerCase(str.charAt(preLength));
			if (str.length() > preLength + 1) {
				return first + str.toString().substring(preLength + 1);
			}
			return String.valueOf(first);
		} else {
			return str.toString();
		}
	}

	/**
	 * 获取lambda表达式函数（方法）名称
	 *
	 * @param <T>  Lambda类型
	 * @param func 函数（无参方法）
	 * @return 函数名称
	 */
	private static <T> String getMethodName(SerializableFunction<T, ?> func) {
		String key = func.getClass().getName();
		String val = CACHE.get(key);
		if (null != val) {
			return val;
		}
		val = getImplMethodName(func);
		CACHE.put(key, val);
		return val;
	}

	private static final Field FIELD_MEMBER_NAME;
	private static final Field FIELD_MEMBER_NAME_NAME;

	static {
		try {
			Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
			FIELD_MEMBER_NAME = classDirectMethodHandle.getDeclaredField("member");
			Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
			FIELD_MEMBER_NAME_NAME = classMemberName.getDeclaredField("name");
		} catch (ClassNotFoundException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getImplMethodName(Serializable func) {
		try {
			if (func instanceof Proxy) {
				InvocationHandler handler = Proxy.getInvocationHandler(func);
				Object dmh = handler.getClass().getDeclaredField("val$target").get(handler);
				Object member = FIELD_MEMBER_NAME.get(dmh);
				return (String) FIELD_MEMBER_NAME_NAME.get(member);
			} else {
				Method method = func.getClass().getDeclaredMethod("writeReplace");
				method.setAccessible(true);
				return ((SerializedLambda) method.invoke(func)).getImplMethodName();
			}
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
			throw EmbeddedLuceneException.of(e);
		}
	}

}
