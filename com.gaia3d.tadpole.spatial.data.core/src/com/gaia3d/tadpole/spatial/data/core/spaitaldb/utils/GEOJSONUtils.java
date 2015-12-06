/*******************************************************************************
 * Copyright 2015 hangum
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
package com.gaia3d.tadpole.spatial.data.core.spaitaldb.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.gaia3d.tadpole.spatial.data.core.ui.utils.SpatialUtils;
import com.hangum.tadpole.ace.editor.core.utils.TadpoleEditorUtils;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.engine.sql.util.RDBTypeToJavaTypeUtils;
import com.hangum.tadpole.engine.sql.util.resultset.QueryExecuteResultDTO;
import com.vividsolutions.jts.io.ParseException;

/**
 * geojson utils
 * 
 * @author hangum
 *
 */
public class GEOJSONUtils {
	private static final Logger logger = Logger.getLogger(GEOJSONUtils.class);
	
	/** 
	 * postgis의 쿼리 결과를 leaflet에 주기위해 전체 GEOJSON 
	 */
	public static final String GEOJSON_FEATURECOLLECTION = "{\"type\": \"FeatureCollection\",\"features\":[ %s ]}";
	
	/**
	 * postgis의 쿼리 결과 leaflet에 주기위해 부분 GEOJSON
	 * TEMP_GEOJSON 안에 들어가야 합니다.
	 */
	public static final String GEOJSON_FEATURE = "{ \"type\": \"Feature\", \"geometry\": %s }";
	
	/**
	 * geojson 
	 */
	public static final String GEOJSON_FEATURE_PROPERTIES = ", \"properties\": { %s }";
	
	/**
	 * 
	 * @param userDB
	 * @param obj
	 * @param properties
	 * @return
	 */
	private static String _partMakrFeature(UserDBDAO userDB, Object obj, String properties) {
		String geoFeature = "";
		if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
			geoFeature = String.format(GEOJSON_FEATURE, ((String)obj) + properties);
		} else if(userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT) {
			try {
				geoFeature = String.format(GEOJSON_FEATURE, SpatialUtils.wktToGeojson((String)obj) + properties);
			} catch (ParseException e) {
				logger.error("WKT parse exception", e);
			}
		} else if(userDB.getDBDefine() == DBDefine.ORACLE_DEFAULT) {
			try {
				geoFeature = SpatialUtils.wktToGeojson(SpatialUtils.oralceStructToWKT((oracle.sql.STRUCT)obj) + properties);
				geoFeature = String.format(GEOJSON_FEATURE, geoFeature);
			} catch (Exception e) {
				logger.error("WKT parse exception", e);
			}
		}
		
		return geoFeature;
	}
	
	/**
	 * 
	 * @param userDB
	 * @param rsDAO
	 * @param listGisColumnIndex
	 * @param resultData
	 * @param isProperties
	 * @return
	 */
	public static String makeFeature(final UserDBDAO userDB, 
									final QueryExecuteResultDTO rsDAO, 
									final List<Integer> listGisColumnIndex, 
									final List<Map<Integer, Object>> resultData, 
									boolean isProperties) {
		final List<String> listGisColumnGjson = new ArrayList<>();
		final Map<Integer, String> mapColumnName = rsDAO.getColumnName();
		
		
		for(Object objResult : resultData.toArray()) {
			final Map<Integer, Object> mapResult = (Map<Integer, Object>)objResult;

			// 행에 몇개의 geojson 컬럼이 있을지 모르므로. 
			for(Integer index : listGisColumnIndex) {
				String strProperties = "";
				if(isProperties) {
					StringBuffer strBfProp = new StringBuffer();
					for(Integer intKey : mapResult.keySet()) {
						if(index == intKey) continue;
						String strSearchType = ""+rsDAO.getColumnType().get(intKey);
						if(SDMUtiils.isSpitailColumn(userDB, strSearchType)) continue;
						
						if(RDBTypeToJavaTypeUtils.isNumberType(rsDAO.getColumnType().get(intKey))) {
//							System.out.println(mapColumnName.get(intKey) + "=====" + mapResult.get(intKey));
							strBfProp.append(String.format("\"%s\":%s,", mapColumnName.get(intKey), mapResult.get(intKey)));
						} else {
							strBfProp.append(String.format("\"%s\":\"%s\",", mapColumnName.get(intKey), mapResult.get(intKey)));
						}
					}
					
					strProperties = String.format(GEOJSON_FEATURE_PROPERTIES, StringUtils.removeEnd(strBfProp.toString(), ","));
				}
				
				String geoFeature = _partMakrFeature(userDB, mapResult.get(index), strProperties);
				
				listGisColumnGjson.add(geoFeature);
			}
		}
		
		StringBuffer tmpSBGeoJson = new StringBuffer();
		for(int i=0; i<listGisColumnGjson.size(); i++) {
			String geoJson = listGisColumnGjson.get(i);
			
			tmpSBGeoJson.append(geoJson);
			if(i != (listGisColumnGjson.size()-1)) tmpSBGeoJson.append(", ");
		}
		
		return tmpSBGeoJson.toString();
	}
	
	/**
	 * 데이터를 geojson 형태로 만듭니다.
	 * 
	 * @param userDB
	 * @param rsDAO
	 * @param listGisColumnIndex
	 * @param resultData
	 * @param isProperties
	 * @return
	 */
	public static String makeFeatureCollection(final UserDBDAO userDB, 
												final QueryExecuteResultDTO rsDAO, 
												final List<Integer> listGisColumnIndex, 
												final List<Map<Integer, Object>> resultData, 
												boolean isProperties
	) {
//		if(rsDAO == null) return "";
		return TadpoleEditorUtils.getGrantText(String.format(GEOJSON_FEATURECOLLECTION, makeFeature(userDB, rsDAO, listGisColumnIndex, resultData, isProperties)));
	}
}
