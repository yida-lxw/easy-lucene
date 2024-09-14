package com.yida.lucene.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yida
 * @date 2024/9/13 1:19
 */
@Data
@AllArgsConstructor(staticName = "of")
public class LatLon {
	private double latitude;
	private double longitude;
}
