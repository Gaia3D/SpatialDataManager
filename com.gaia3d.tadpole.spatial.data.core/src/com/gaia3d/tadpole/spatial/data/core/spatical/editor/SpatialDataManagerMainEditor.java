package com.gaia3d.tadpole.spatial.data.core.spatical.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.geojson.GeoJsonObject;
import org.geojson.Geometry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaia3d.tadpole.spatial.data.core.spatical.editor.inner.CenterObj;
import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.ace.editor.core.utils.TadpoleEditorUtils;
import com.hangum.tadpole.sql.util.resultset.QueryExecuteResultDTO;

/**
 * Tadpole extension to spatial data manager 
 * 
 * @author hangum
 *
 */
public class SpatialDataManagerMainEditor extends SpatialDataManagerDataHandler {
	private static final Logger logger = Logger.getLogger(SpatialDataManagerMainEditor.class);
	
	/** 
	 * postgis의 쿼리 결과를 leaflet에 주기위해 전체 GEOJSON 
	 */
	private static final String TMPELATE_GROUP_GEO_JSON = "{\"type\": \"FeatureCollection\",\"features\":[ %s ]}";
	
	/**
	 * postgis의 쿼리 결과 leaflet에 주기위해 부분 GEOJSON
	 */
	private static final String TMPELATE_GEO_JSON = "{ \"type\": \"Feature\", \"geometry\": %s }";
	
	/** 사용자 지정 컬러 */
	private static final String USER_CLICK_COLOR = "#ff7800";
	
	
	@Override
	public void resultSetClick(int selectIndex, Map<Integer, Object> mapColumns) {
//		if(logger.isDebugEnabled()) {
//			logger.debug("=============================================================");
//			logger.debug("Clieck column index is " + selectIndex );
//			logger.debug("Clieck column data is " + mapColumns.get(selectIndex));
//		}
		
		List<String> listGJson = new ArrayList<>();
		for(Integer index : listGisColumnIndex) {
			listGJson.add((String)mapColumns.get(index));
		}
		
		clearClickedLayersMap();
		drawingUserColorMap(listGJson);
	}

	/**
	 * 결과 테이블을 더블클릭했을 경우 
	 */
	@Override
	public void resultSetDoubleClick(int selectIndex, Map<Integer, Object> mapColumns) {
	}

	/**
	 * 쿼리가 끝나면 호출되는 메소드.
	 */
	@Override
	public void queryEndedExecute(QueryExecuteResultDTO rsDAO) {
		listGisColumnIndex.clear();
		Map<Integer, String> mapColumnNames = rsDAO.getColumnName();
		List<Map<Integer, Object>> resultData = rsDAO.getDataList().getData();

		for(int i=0; i<mapColumnNames.size(); i++) {
			String strSearchColumn = mapColumnNames.get(i);
			
			if(StringUtils.startsWithIgnoreCase(strSearchColumn, PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN)) {
				listGisColumnIndex.add(i);
			}
		}
		
		/** 지도를 초기화 합니다 */
		clearAllLayersMap();
		
		//
		if(!listGisColumnIndex.isEmpty()) {
			List<String> listGisColumnGjson = new ArrayList<>();
			for(Object objResult : resultData.toArray()) {
				Map<Integer, Object> mapResult = (Map<Integer, Object>)objResult;
				
				for(Integer index : listGisColumnIndex) {
					listGisColumnGjson.add( (String)mapResult.get(index) );
				}
			}
			
			// ---------------------------------------------
			drawMap(listGisColumnGjson, USER_CLICK_COLOR);
		}
	}
	
	/**
	 * 사용자가 클릭한 모든 layer를 삭제한다. 
	 */
	private void clearAllLayersMap() {
		browserMap.evaluate("clearAllLayersMap();");
	}
	
	/**
	 * 사용자가 클릭한 layer를 삭제한다. 
	 */
	private void clearClickedLayersMap() {
		browserMap.evaluate("clearClickedLayersMap();");
	}
	
	/**
	 * 지도에 데이터를 표시합니다.
	 * 
	 * @param strGeoJson
	 */
	private void drawingUserColorMap(List<String> listGJson) {
		String strFullyGeojson = TadpoleEditorUtils.getGrantText(fullyGeoJSON(listGJson));
//		if(logger.isDebugEnabled()) logger.debug(strFullyGeojson);
		browserMap.evaluate(String.format("onClickPoint('%s');", strFullyGeojson));
	}

	/**
	 * 지도에 데이터를 표시합니다.
	 * 
	 * @param strGeoJson
	 * @param strColor 결과를 더블 클릭했을 경우에 나타나는 색
	 */
	private void drawMap(List<String> listGJson, String strColor) {
		String strFullyGeojson = TadpoleEditorUtils.getGrantText(fullyGeoJSON(listGJson));
//		if(logger.isDebugEnabled()) logger.debug(strFullyGeojson);
		
		CenterObj getCenter = getCenter(listGJson.get(0));
		browserMap.evaluate(String.format("drawingMap('%s', '%s', '%s', '%s', '%s' );", 
					strFullyGeojson, 
					strColor, 
					getCenter.getCenterX(), getCenter.getCenterY(), getCenter.getZoom())
				);
	}
	
	/**
	 * 검색된 첫번째 데이터를 지도 중앙에 표시합니다.
	 */
	private CenterObj getCenter(String str) {
		if(null == str || "".equals(str)) return new CenterObj();

		try {
			GeoJsonObject object = new ObjectMapper().readValue(str, GeoJsonObject.class);
			if (object instanceof Geometry) {
				Geometry geometry = (Geometry)object;
				if(logger.isDebugEnabled()) logger.debug("====================== Setting center ");
				if(geometry.getCoordinates().size() >= 2) {
					List listCoordinates = geometry.getCoordinates();
					logger.debug("============> " + listCoordinates.get(0) +":" + listCoordinates.get(1));
					
					if(logger.isDebugEnabled()) logger.debug("center : X is  " + geometry.getCoordinates().get(0) + "\t:Y is " + geometry.getCoordinates().get(1));
					
					return new CenterObj(""+geometry.getCoordinates().get(0), ""+geometry.getCoordinates().get(1), "3");
				}
			}
			
		} catch (Exception e) {
			logger.error("getJson parser exception", e);
		}
		
		return new CenterObj();
	}
	
	/**
	 * leaflet에서 지도에 표시할 수 있도록 데이터를 만듭니
	 * 
	 * @param listPostGisJson
	 * @return
	 */
	private String fullyGeoJSON(List<String> listPostGisJson) {
		
		StringBuffer tmpSBGeoJson = new StringBuffer();
		for(int i=0; i<listPostGisJson.size(); i++) {
			String geoJson = listPostGisJson.get(i);
			
			tmpSBGeoJson.append(String.format(TMPELATE_GEO_JSON, geoJson));
			if(i != (listPostGisJson.size()-1)) tmpSBGeoJson.append(", ");
		}
		
		return String.format(TMPELATE_GROUP_GEO_JSON, tmpSBGeoJson.toString());
	}

}