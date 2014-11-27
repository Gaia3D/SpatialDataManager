/*******************************************************************************
 * Copyright 2014 hangum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.ui.preference.data;

/**
 * Spatial Preference define
 * 
 * @author hangum
 *
 */
public class SpatialPreferenceDefine {
	
	/** 올챙이에서 맵으로 데이터 보낼 때 */
	public static final String SPATIAL_SEND_MAP_DATA_COUNT		 = "SPATIAL_SEND_MAP_DATA_COUNT";
	public static final String SPATIAL_SEND_MAP_DATA_COUNT_VALUE = "1000";
	
	public static final String SPATIAL_USER_OPTIONS			= "SPATIAL_USER_OPTIONS";
	public static final String SPATIAL_USER_OPTIONS_VALUE 	= "{\"autoZoom\":true,\"displayType\":\"normal\",\"canvasOptions\":{\"BasePointRadius\":3,\"BaseLineColor\":\"rgba(0, 0, 255, 0.2)\",\"BaseLineWidth\":2,\"BaseFillColor\":\"rgba(0, 0, 255, 0.2)\"},\"heatmapOptions\":{\"radius\":0.01,\"maxOpacity\":0.8,\"scaleRadius\":true,\"useLocalExtrema\":true},\"selectedOptions\":{\"color\":\"#ff7800\",\"weight\":5,\"opacity\":0.65}}";
}
