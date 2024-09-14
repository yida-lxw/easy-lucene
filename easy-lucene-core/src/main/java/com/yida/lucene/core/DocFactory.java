package com.yida.lucene.core;

import com.yida.lucene.annotation.AutoFill;
import com.yida.lucene.annotation.DocField;
import com.yida.lucene.annotation.DocId;
import com.yida.lucene.annotation.LogicDel;
import com.yida.lucene.bean.Pair;
import com.yida.lucene.constant.FieldType;
import com.yida.lucene.exception.ElAssert;
import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.repository.hightlight.HighlightRender;
import com.yida.lucene.repository.query.Querys;
import com.yida.lucene.util.BeanDesc;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 映射转换 文档 <=> JavaBean 的工厂
 *
 * @author yida
 * @date 2024/8/4 14:33
 */
@Slf4j
@Getter
public class DocFactory<T> {

	/**
	 * 除id外的字段
	 */
	private static final Map<Class<?>, Set<Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();

	/**
	 * 除id外的getter
	 */
	private static final Map<Field, Method> FIELD_GETTER_CACHE = new ConcurrentHashMap<>();

	/**
	 * 除id外的setter
	 */
	private static final Map<Field, Method> FIELD_SETTER_CACHE = new ConcurrentHashMap<>();

	/**
	 * 字段 => DocField
	 */
	private static final Map<Field, DocField> DOC_FIELD_CACHE = new ConcurrentHashMap<>();

	/**
	 * 字段 => AutoFill
	 */
	private static final Map<Class<?>, Map<Field, AutoFill>> AUTO_FILL_CACHE = new ConcurrentHashMap<>();

	/**
	 * 字段 => LogicDel
	 */
	private static final Map<Class<?>, Pair<Field, LogicDel>> LOGIC_DEL_CACHE = new ConcurrentHashMap<>();

	/**
	 * 类 => DocId
	 */
	private static final Map<Class<?>, DocId> CLASS_DOC_ID_CACHE = new ConcurrentHashMap<>();

	/**
	 * 类 => id字段
	 */
	private static final Map<Class<?>, Field> CLASS_ID_CACHE = new ConcurrentHashMap<>();

	/**
	 * id字段 => getter
	 */
	private static final Map<Field, Method> ID_GETTER_CACHE = new ConcurrentHashMap<>();

	/**
	 * id字段 => setter
	 */
	private static final Map<Field, Method> ID_SETTER_CACHE = new ConcurrentHashMap<>();

	public static <T> DocFactory<T> getDocFactory(Class<T> docClass, Constructor<T> constructor) {
		Field id = CLASS_ID_CACHE.get(docClass);
		if (Objects.isNull(id)) {
			initDocData(docClass);
		}
		return new DocFactory<>(docClass, constructor);
	}

	private static <T> void initDocData(Class<T> docClass) {
		Set<Field> fields = Arrays.stream(docClass.getDeclaredFields())
				.peek(field -> field.setAccessible(true))
				.filter(field -> {
					if (field.isAnnotationPresent(DocField.class)) {
						return true;
					} else if (field.isAnnotationPresent(DocId.class)) {
						// put docId cache
						if (Objects.nonNull(CLASS_DOC_ID_CACHE.get(docClass))) {
							throw EmbeddedLuceneException.of("docClass can only have one docId");
						} else {
							DocId docId = field.getAnnotation(DocId.class);
							CLASS_DOC_ID_CACHE.put(docClass, docId);
							CLASS_ID_CACHE.put(docClass, field);
						}
					}
					return false;
				})
				.collect(Collectors.toSet());

		// check docId cache
		if (Objects.isNull(CLASS_DOC_ID_CACHE.get(docClass))) {
			throw EmbeddedLuceneException.of("docClass must have one docId!");
		}

		CLASS_FIELD_CACHE.put(docClass, fields);
		BeanDesc beanDesc = new BeanDesc(docClass);

		Map<Field, AutoFill> autoFillMap = new HashMap<>();
		AUTO_FILL_CACHE.put(docClass, autoFillMap);

		fields.forEach(field -> {
			AutoFill autoFill = field.getAnnotation(AutoFill.class);
			if (Objects.nonNull(autoFill)) {
				autoFillMap.put(field, autoFill);
			}
			LogicDel logicDel = field.getAnnotation(LogicDel.class);
			if (Objects.nonNull(logicDel)) {
				if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
					LOGIC_DEL_CACHE.put(docClass, Pair.of(field, logicDel));
				} else {
					throw EmbeddedLuceneException.of("annotation LogicDel only support boolean type!");
				}
			}

			DOC_FIELD_CACHE.put(field, field.getAnnotation(DocField.class));
			Method getter = beanDesc.getGetter(field.getName());
			Method setter = beanDesc.getSetter(field.getName());
			FIELD_GETTER_CACHE.put(field, getter);
			FIELD_SETTER_CACHE.put(field, setter);
		});

		Field idField = CLASS_ID_CACHE.get(docClass);
		ID_GETTER_CACHE.put(idField, beanDesc.getGetter(idField.getName()));
		ID_SETTER_CACHE.put(idField, beanDesc.getSetter(idField.getName()));
	}

	public DocFactory(Class<T> docClass, Constructor<T> constructor) {
		log.info("Creating DocFactory bind from class : {}", docClass.getName());

		this.docClass = docClass;

		constructor.setAccessible(true);
		this.constructor = constructor;

		this.idField = CLASS_ID_CACHE.get(docClass);
		this.idFieldName = idField.getName();

		this.fields = CLASS_FIELD_CACHE.get(docClass);
		this.nameFieldMap = this.fields.stream().collect(Collectors.toMap(Field::getName, Function.identity()));
	}

	private final Class<T> docClass;

	/**
	 * 无参构造
	 */
	private final Constructor<T> constructor;

	private final Field idField;
	private final String idFieldName;

	private final Set<Field> fields;
	private final Map<String, Field> nameFieldMap;

	/**
	 * 获取字段的FieldType
	 *
	 * @param fieldName 字段名称
	 * @return FieldType
	 */
	public FieldType getFieldType(String fieldName) {
		boolean isIdField = fieldName.equals(idFieldName);
		if (isIdField) {
			return CLASS_DOC_ID_CACHE.get(docClass).type();
		} else {
			DocField docField = DOC_FIELD_CACHE.get(getField(fieldName));
			ElAssert.nonNull(docField, "filedName : " + fieldName + " , is not exist");
			return docField.type();
		}
	}

	/**
	 * 获取字段
	 *
	 * @param fieldName 字段名称
	 * @return Field
	 */
	public Field getField(String fieldName) {
		return nameFieldMap.get(fieldName);
	}

	/**
	 * 创建文档
	 *
	 * @param data javabean
	 * @return Document
	 */
	public Document createDoc(T data) {
		Document doc = new Document();
		try {
			Method idGetter = ID_GETTER_CACHE.get(idField);
			DocId docId = CLASS_DOC_ID_CACHE.get(docClass);
			docId.type().set(idFieldName, idGetter.invoke(data), true, doc, docId.extensionFields());
			for (Field field : fields) {
				Method getter = FIELD_GETTER_CACHE.get(field);
				DocField docField = DOC_FIELD_CACHE.get(field);
				docField.type().set(field.getName(), getter.invoke(data), docField.store(), doc, docField.extensionFields());
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw EmbeddedLuceneException.of(e);
		}
		return doc;
	}


	/**
	 * 文档 => javabean
	 *
	 * @param doc             文档
	 * @param highlightRender 高亮
	 * @return javabean
	 */
	public T toJavaBean(Document doc, HighlightRender highlightRender) {
		return toJavaBean(toMap(renderHighlight(doc, highlightRender)));
	}

	public void setVal(T t, String fieldName, Object val) {
		try {
			if (fieldName.equals(idFieldName)) {
				ID_SETTER_CACHE.get(idField).invoke(t, val);
			} else {
				Field field = nameFieldMap.get(fieldName);
				FIELD_SETTER_CACHE.get(field).invoke(t, val);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw EmbeddedLuceneException.of(e);
		}
	}

	public T toJavaBean(Map<String, String> map) {
		T res;
		boolean idSetFlag = false;
		try {
			res = constructor.newInstance();
			for (String key : map.keySet()) {
				if (!idSetFlag && key.equals(idFieldName)) {
					Object value = CLASS_DOC_ID_CACHE.get(docClass).type().get(map.get(key), idField.getType());
					ID_SETTER_CACHE.get(idField).invoke(res, value);
					idSetFlag = true;
				} else {
					Field field = nameFieldMap.get(key);
					FieldType type = DOC_FIELD_CACHE.get(field).type();
					FIELD_SETTER_CACHE.get(field).invoke(res, type.get(map.get(key), field.getType()));
				}
			}
			return res;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw EmbeddedLuceneException.of(e);
		}
	}

	/**
	 * 文档 => javabean
	 *
	 * @param doc 文档
	 * @return javabean
	 */
	public Map<String, String> toMap(Document doc) {
		return doc.getFields().stream()
				.collect(Collectors.toMap(IndexableField::name, IndexableField::stringValue));
	}

	/**
	 * 高亮填充
	 *
	 * @param doc             文档
	 * @param highlightRender 高亮
	 * @return 文档
	 */
	public Document renderHighlight(Document doc, HighlightRender highlightRender) {
		if (Objects.nonNull(highlightRender)) {
			List<IndexableField> fields = doc.getFields();
			for (IndexableField indexableField : fields) {
				String name = indexableField.name();
				Field columnField = nameFieldMap.get(name);
				String value = indexableField.stringValue();
				if (null != columnField && null != value) {
					FieldType type = DOC_FIELD_CACHE.get(columnField).type();
					boolean render = type == FieldType.TEXT || type == FieldType.STRING;
					if (render) {
						org.apache.lucene.document.Field field = (org.apache.lucene.document.Field) indexableField;
						String renderStr = highlightRender.render(name, value);
						if (null != renderStr) {
							field.setStringValue(renderStr);
						}
					}
				}
			}
		}
		return doc;
	}

	/**
	 * 获取id
	 *
	 * @param entity entity
	 * @return id
	 */
	public Serializable getId(T entity) {
		Field field = CLASS_ID_CACHE.get(docClass);
		Method getter = ID_GETTER_CACHE.get(field);
		try {
			return (Serializable) getter.invoke(entity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw EmbeddedLuceneException.of(e);
		}
	}

	/**
	 * 获取id的query
	 *
	 * @param entity entity
	 * @return Query
	 */
	public Query getIdQuery(T entity) {
		return getIdQuery(getId(entity));
	}

	/**
	 * 获取id的query
	 *
	 * @param id id
	 * @return Query
	 */
	public Query getIdQuery(Serializable id) {
		return Querys.eq(CLASS_DOC_ID_CACHE.get(docClass).type(), CLASS_ID_CACHE.get(docClass).getName(), id);
	}

	static final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer();

	/**
	 * 组装当前映射类的 Analyzer
	 *
	 * @param map map
	 * @return Analyzer
	 */
	Analyzer getAnalyzer(Map<FieldType, Analyzer> map) {
		DocId docId = CLASS_DOC_ID_CACHE.get(docClass);
		Map<String, Analyzer> fieldAnalyzers = new HashMap<>(fields.size() + 1);
		fieldAnalyzers.put(idFieldName, map.get(docId.type()));
		for (Field field : fields) {
			DocField docField = DOC_FIELD_CACHE.get(field);
			fieldAnalyzers.put(field.getName(), map.get(docField.type()));
		}
		return new PerFieldAnalyzerWrapper(DEFAULT_ANALYZER, fieldAnalyzers);
	}

	/**
	 * 获取逻辑删除字段
	 *
	 * @return Pair
	 */
	public Pair<Field, LogicDel> getLogicDel() {
		return LOGIC_DEL_CACHE.get(docClass);
	}

	/**
	 * 获取自动填充字段
	 *
	 * @return Map
	 */
	public Map<Field, AutoFill> getAutoFillMap() {
		return AUTO_FILL_CACHE.get(docClass);
	}

	public ElDocument<T> getElDocument(int docId, Source<T> source, HighlightRender highlightRender, Set<String> selectFieldSet) {
		return new ElDocument<>(docId, source, highlightRender, selectFieldSet);
	}

}
