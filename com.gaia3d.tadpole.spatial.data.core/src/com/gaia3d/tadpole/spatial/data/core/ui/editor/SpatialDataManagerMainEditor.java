package com.gaia3d.tadpole.spatial.data.core.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.gaia3d.tadpole.spatial.data.core.ui.preference.data.SpatialGetPreferenceData;
import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.ace.editor.core.utils.TadpoleEditorUtils;
import com.hangum.tadpole.rdb.core.Activator;
import com.hangum.tadpole.sql.util.resultset.QueryExecuteResultDTO;

/**
 * Tadpole extension to spatial data manager 
 * 
 * @author hangum
 *
 */
public class SpatialDataManagerMainEditor extends SpatialDataManagerDataHandler {
	private static final Logger logger = Logger.getLogger(SpatialDataManagerMainEditor.class);
	
	/** 지도에 넘겨줄 카운트 */
	/** 
	 * postgis의 쿼리 결과를 leaflet에 주기위해 전체 GEOJSON 
	 */
	private static final String TMPELATE_GROUP_GEO_JSON = "{\"type\": \"FeatureCollection\",\"features\":[ %s ]}";
	
	/**
	 * postgis의 쿼리 결과 leaflet에 주기위해 부분 GEOJSON
	 */
	private static final String TMPELATE_GEO_JSON = "{ \"type\": \"Feature\", \"geometry\": %s }";
	
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
		final List<Map<Integer, Object>> resultData = rsDAO.getDataList().getData();

		for(int i=0; i<mapColumnNames.size(); i++) {
			String strSearchColumn = mapColumnNames.get(i);
			
			if(StringUtils.startsWithIgnoreCase(strSearchColumn, PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN)) {
				listGisColumnIndex.add(i);
			}
		}
		
		/** 지도를 초기화 합니다 */
		clearAllLayersMap();
		
		/** 사용자 멤 데이터 */
		final int INT_SEND_COUNT = SpatialGetPreferenceData.getSendMapDataCount();
		/** 사용자 환경설정 */
		final String USER_OPTIONS = SpatialGetPreferenceData.getUserOptions();
		
		//
		if(!listGisColumnIndex.isEmpty()) {
			// ---------------------------------------------
//			if(logger.isDebugEnabled()) logger.debug("## Total Size is ==> " + listGisColumnGjson.size());
			Job job = new Job("Drawing map") {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					final List<String> listGisColumnGjson = new ArrayList<>();
					for(Object objResult : resultData.toArray()) {
						Map<Integer, Object> mapResult = (Map<Integer, Object>)objResult;
						// 행에 몇개의 geojson 컬럼이 있을지 모르므로. 
						for(Integer index : listGisColumnIndex) listGisColumnGjson.add( (String)mapResult.get(index) );
					}
					
					int intTotalDrawMapCount = listGisColumnGjson.size()/INT_SEND_COUNT+1;
					monitor.beginTask("Drawing a map", intTotalDrawMapCount);
					
					try {
						int intStartIndex = 0;
						for(int i=0; i<intTotalDrawMapCount; i++) {
							monitor.setTaskName("Drawing a map ( " + (i+1) + "/" + intTotalDrawMapCount + " )");
							monitor.worked(1);
							
							List<String> listPartData = new ArrayList<>();
							if(listGisColumnGjson.size() < INT_SEND_COUNT+intStartIndex) {
								listPartData = listGisColumnGjson.subList(intStartIndex, listGisColumnGjson.size());
							} else {
								listPartData = listGisColumnGjson.subList(intStartIndex, INT_SEND_COUNT+intStartIndex);
							}
							
							if(i == 0) {
								drawMapInit(listPartData, USER_OPTIONS);
							} else {
								drawMapAddData(listPartData);
							}
							
							if(monitor.isCanceled()) {
								if(logger.isDebugEnabled()) logger.debug("Stop map drawing.");
								break;
							}
							
							intStartIndex += INT_SEND_COUNT;
						}	
					} catch(Exception e) {
						logger.error("Table Referesh", e);
						
						return new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
					} finally {
						monitor.done();
					}
					
					/////////////////////////////////////////////////////////////////////////////////////////
					return Status.OK_STATUS;
				}
				
				/**
				 * 지도에 데이터를 표시합니다.
				 * 
				 * @param strGeoJson 지도데이터 
				 * @param strUserOptions 사용자 옵션 
				 */
				private void drawMapInit(final List<String> listGJson, final String strUserOptions) {
					browserMap.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							browserMap.evaluate(String.format("drawingMapInit('%s', '%s');", 
																TadpoleEditorUtils.getGrantText(fullyGeoJSON(listGJson)), 
																TadpoleEditorUtils.getGrantText(strUserOptions))
												);
						}
					});
				}
				
				/**
				 * 지도에 데이터를 표시합니다.
				 * 
				 * @param strGeoJson
				 * @param strColor 결과를 더블 클릭했을 경우에 나타나는 색
				 */
				private void drawMapAddData(final List<String> listGJson) {
					browserMap.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							browserMap.evaluate(String.format("drawMapAddData('%s');", 
													TadpoleEditorUtils.getGrantText(fullyGeoJSON(listGJson)))
												);
						}
					});
				}
			};
			
			// job의 event를 처리해 줍니다.
			job.addJobChangeListener(new JobChangeAdapter() {
				
				public void done(IJobChangeEvent event) {
					browserMap.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							// 메인 에디터에 포커스를 이동하도록 합니다.
							mainEditor.setOrionTextFocus();
						}
					});
				}	// end done
			});	// end job
			
			job.setName("Drawing map job");
			job.setUser(true);
			job.setPriority(Job.SHORT);
			job.schedule();
			
		}	// end if block
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
		browserMap.evaluate(String.format("onClickPoint('%s');", strFullyGeojson));
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