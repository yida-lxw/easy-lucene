package com.yida.lucene.repository.query;

import com.yida.lucene.constant.FieldType;
import com.yida.lucene.exception.EmbeddedLuceneException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yida
 * @date 2024/8/24 1:55
 */
@SuppressWarnings("all")
public interface QueryProvider<T> {

	Query eq(String name, T value);

	default Query ne(String name, T value) {
		return negative(eq(name, value));
	}

	Query ge(String name, T value);

	Query le(String name, T value);

	Query gt(String name, T value);

	Query lt(String name, T value);

	Query in(String name, Collection<T> value);

	default Query notIn(String name, Collection<T> value) {
		return negative(in(name, value));
	}

	Query like(Analyzer analyzer, String name, T value);

	default Query notLike(Analyzer analyzer, String name, T value) {
		return negative(like(analyzer, name, value));
	}

	default Query negative(Query query) {
		return new BooleanQuery.Builder()
				.add(query, BooleanClause.Occur.MUST_NOT)
				.build();
	}

	/**
	 * get
	 *
	 * @param fieldType fieldType
	 * @return TypeChecker
	 */
	@SuppressWarnings("rawtypes")
	static QueryProvider getProvider(FieldType fieldType) {
		QueryProvider<?> queryProvider = Factory.HOLDER.get(fieldType);
		if (Objects.isNull(queryProvider)) {
			throw EmbeddedLuceneException.of("nosuch fieldType: {" + fieldType + "} queryProvider");
		}
		return queryProvider;
	}

	class Factory {
		private static final Map<FieldType, QueryProvider<?>> HOLDER = new HashMap<>();

		static {
			QueryProvider<String> stringQueryProvider = new QueryProvider<String>() {
				@Override
				public Query eq(String name, String value) {
					return new TermQuery(new Term(name, value));
				}

				@Override
				public Query ge(String name, String value) {
					throw EmbeddedLuceneException.of("string type not support ge query");
				}

				@Override
				public Query le(String name, String value) {
					throw EmbeddedLuceneException.of("string type not support le query");
				}

				@Override
				public Query gt(String name, String value) {
					throw EmbeddedLuceneException.of("string type not support gt query");
				}

				@Override
				public Query lt(String name, String value) {
					throw EmbeddedLuceneException.of("string type not support lt query");
				}

				@Override
				public Query in(String name, Collection<String> value) {
					BooleanQuery.Builder builder = new BooleanQuery.Builder();
					for (String s : value) {
						builder.add(eq(name, s), BooleanClause.Occur.SHOULD);
					}
					return builder.build();
				}

				@Override
				public Query like(Analyzer analyzer, String name, String value) {
					return new SimpleQueryParser(analyzer, name).parse(value);
				}
			};

			HOLDER.put(
					FieldType.TEXT,
					stringQueryProvider
			);
			HOLDER.put(
					FieldType.STRING,
					stringQueryProvider
			);
			HOLDER.put(
					FieldType.BOOL,
					new QueryProvider<Boolean>() {
						@Override
						public Query eq(String name, Boolean value) {
							return new TermQuery(new Term(name, value.toString()));
						}

						@Override
						public Query ge(String name, Boolean value) {
							throw EmbeddedLuceneException.of("boolean type not support ge query");
						}

						@Override
						public Query le(String name, Boolean value) {
							throw EmbeddedLuceneException.of("boolean type not support le query");
						}

						@Override
						public Query gt(String name, Boolean value) {
							throw EmbeddedLuceneException.of("boolean type not support gt query");
						}

						@Override
						public Query lt(String name, Boolean value) {
							throw EmbeddedLuceneException.of("boolean type not support lt query");
						}

						@Override
						public Query in(String name, Collection<Boolean> value) {
							throw EmbeddedLuceneException.of("boolean type not support in query");
						}

						@Override
						public Query notIn(String name, Collection<Boolean> value) {
							throw EmbeddedLuceneException.of("boolean type not support notIn query");
						}

						@Override
						public Query like(Analyzer analyzer, String name, Boolean value) {
							throw EmbeddedLuceneException.of("boolean type not support like query");
						}

						@Override
						public Query notLike(Analyzer analyzer, String name, Boolean value) {
							throw EmbeddedLuceneException.of("boolean type not support notLike query");
						}
					}
			);

			QueryProvider<Long> longQueryProvider = new QueryProvider<Long>() {
				@Override
				public Query eq(String name, Long value) {
					return NumericDocValuesField.newSlowExactQuery(name, value);
				}

				@Override
				public Query ge(String name, Long value) {
					return NumericDocValuesField.newSlowRangeQuery(name, value, Long.MAX_VALUE);
				}

				@Override
				public Query le(String name, Long value) {
					return NumericDocValuesField.newSlowRangeQuery(name, Long.MIN_VALUE, value);
				}

				@Override
				public Query gt(String name, Long value) {
					return NumericDocValuesField.newSlowRangeQuery(name, Math.addExact(value, 1), Long.MAX_VALUE);
				}

				@Override
				public Query lt(String name, Long value) {
					return NumericDocValuesField.newSlowRangeQuery(name, Long.MIN_VALUE, Math.addExact(value, -1));
				}

				@Override
				public Query in(String name, Collection<Long> value) {
					BooleanQuery.Builder builder = new BooleanQuery.Builder();
					for (Long s : value) {
						builder.add(eq(name, s), BooleanClause.Occur.SHOULD);
					}
					return builder.build();
				}

				@Override
				public Query like(Analyzer analyzer, String name, Long value) {
					throw EmbeddedLuceneException.of("long type not support like query");
				}

				@Override
				public Query notLike(Analyzer analyzer, String name, Long value) {
					throw EmbeddedLuceneException.of("long type not support notLike query");
				}
			};
			HOLDER.put(
					FieldType.LONG,
					longQueryProvider
			);
			HOLDER.put(
					FieldType.DATE,
					new QueryProvider<Object>() {
						private Long toLong(Object value) {
							long milliseconds = 0L;
							if (value instanceof Date) {
								Date time = (Date) value;
								milliseconds = time.getTime();
							} else if (value instanceof LocalDateTime) {
								LocalDateTime time = (LocalDateTime) value;
								milliseconds = time.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
							} else if (value instanceof LocalDate) {
								LocalDate time = (LocalDate) value;
								milliseconds = time.atStartOfDay().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
							} else {
								throw EmbeddedLuceneException.of("data's type:" + value.getClass() + "is not support");
							}
							return milliseconds;
						}

						@Override
						public Query eq(String name, Object value) {
							return longQueryProvider.eq(name, toLong(value));
						}

						@Override
						public Query ge(String name, Object value) {
							return longQueryProvider.ge(name, toLong(value));
						}

						@Override
						public Query le(String name, Object value) {
							return longQueryProvider.le(name, toLong(value));
						}

						@Override
						public Query gt(String name, Object value) {
							return longQueryProvider.gt(name, toLong(value));
						}

						@Override
						public Query lt(String name, Object value) {
							return longQueryProvider.lt(name, toLong(value));
						}

						@Override
						public Query in(String name, Collection<Object> value) {
							return longQueryProvider.in(name, value.stream().map(this::toLong).collect(Collectors.toList()));
						}

						@Override
						public Query like(Analyzer analyzer, String name, Object value) {
							throw EmbeddedLuceneException.of("date type not support like query");
						}

						@Override
						public Query notLike(Analyzer analyzer, String name, Object value) {
							throw EmbeddedLuceneException.of("date type not support notLike query");
						}
					}
			);
			HOLDER.put(
					FieldType.INT,
					new QueryProvider<Integer>() {
						@Override
						public Query eq(String name, Integer value) {
							return NumericDocValuesField.newSlowExactQuery(name, value);
						}

						@Override
						public Query ge(String name, Integer value) {
							return NumericDocValuesField.newSlowRangeQuery(name, value, Integer.MAX_VALUE);
						}

						@Override
						public Query le(String name, Integer value) {
							return NumericDocValuesField.newSlowRangeQuery(name, Integer.MIN_VALUE, value);
						}

						@Override
						public Query gt(String name, Integer value) {
							return NumericDocValuesField.newSlowRangeQuery(name, Math.addExact(value, 1), Integer.MAX_VALUE);
						}

						@Override
						public Query lt(String name, Integer value) {
							return NumericDocValuesField.newSlowRangeQuery(name, Integer.MIN_VALUE, Math.addExact(value, -1));
						}

						@Override
						public Query in(String name, Collection<Integer> value) {
							BooleanQuery.Builder builder = new BooleanQuery.Builder();
							for (Integer s : value) {
								builder.add(eq(name, s), BooleanClause.Occur.SHOULD);
							}
							return builder.build();
						}

						@Override
						public Query like(Analyzer analyzer, String name, Integer value) {
							throw EmbeddedLuceneException.of("int type not support like query");
						}

						@Override
						public Query notLike(Analyzer analyzer, String name, Integer value) {
							throw EmbeddedLuceneException.of("int type not support notLike query");
						}
					}
			);
		}
	}

}
