package com.yida.lucene.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yida
 * @date 2024/09/15 3:12
 */
@Data
@AllArgsConstructor(staticName = "of")
public class Pair<K, V> implements Serializable {
	private static final long serialVersionUID = 1L;
	protected K key;
	protected V value;
}
