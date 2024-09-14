package com.yida.lucene.repository.query;

import com.yida.lucene.bean.LatLon;
import com.yida.lucene.constant.FieldType;
import com.yida.lucene.constant.TypeChecker;
import com.yida.lucene.util.LambdaUtil;
import com.yida.lucene.util.SerializableFunction;
import lombok.experimental.UtilityClass;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;

import java.util.Collection;

/**
 * 组装Query
 *
 * @author yida
 * @date 2024/8/9 11:14
 */
@UtilityClass
@SuppressWarnings({"unchecked", "rawtypes"})
public class Querys {

	public static final Query MATCH_ALL_QUERY = new MatchAllDocsQuery();

	public Query eq(FieldType type, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).eq(name, data);
	}

	public Query ne(FieldType type, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).ne(name, data);
	}

	public Query ge(FieldType type, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).ge(name, data);
	}

	public Query le(FieldType type, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).le(name, data);
	}

	public Query gt(FieldType type, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).gt(name, data);
	}

	public Query lt(FieldType type, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).lt(name, data);
	}

	public Query like(FieldType type, Analyzer analyzer, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).like(analyzer, name, data);
	}

	public Query notLike(FieldType type, Analyzer analyzer, String name, Object data) {
		TypeChecker.getChecker(type).checkThenThrowIfNeed(data);
		return QueryProvider.getProvider(type).notLike(analyzer, name, data);
	}

	public Query in(FieldType type, String name, Collection data) {
		if (data != null && !data.isEmpty()) {
			TypeChecker.getChecker(type).checkThenThrowIfNeed(data.iterator().next());
		}
		return QueryProvider.getProvider(type).in(name, data);
	}

	public Query notIn(FieldType type, String name, Collection data) {
		if (data != null && !data.isEmpty()) {
			TypeChecker.getChecker(type).checkThenThrowIfNeed(data.iterator().next());
		}
		return QueryProvider.getProvider(type).notIn(name, data);
	}

	public TotalHitCountCollector totalHitsCollector() {
		return new TotalHitCountCollector();
	}

	/**
	 * 固定宽高内搜索
	 */
	public <T> Query latLonBox(SerializableFunction<T, ?> name, LatLon topLeft, LatLon bottomRight) {
		return LatLonPoint.newBoxQuery(LambdaUtil.getFieldName(name), topLeft.getLatitude(), bottomRight.getLatitude(), topLeft.getLongitude(), bottomRight.getLongitude());
	}

	/**
	 * 以某个坐标为圆心固定半径搜索
	 */
	public <T> Query latLonRadio(SerializableFunction<T, ?> name, LatLon center, double radiusMeters) {
		return LatLonPoint.newDistanceQuery(LambdaUtil.getFieldName(name), center.getLatitude(), center.getLongitude(), radiusMeters);
	}

	/**
	 * 固定宽高内搜索
	 */
	public Query latLonBox(String name, LatLon topLeft, LatLon bottomRight) {
		return LatLonPoint.newBoxQuery(name, topLeft.getLatitude(), bottomRight.getLatitude(), topLeft.getLongitude(), bottomRight.getLongitude());
	}

	/**
	 * 以某个坐标为圆心固定半径搜索
	 */
	public Query latLonRadio(String name, LatLon center, double radiusMeters) {
		return LatLonPoint.newDistanceQuery(name, center.getLatitude(), center.getLongitude(), radiusMeters);
	}

	public Query compose(Query... queries) {
		if (queries == null || queries.length == 0) {
			return null;
		}
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (Query query : queries) {
			builder.add(query, BooleanClause.Occur.SHOULD);
		}
		return builder.build();
	}
}
