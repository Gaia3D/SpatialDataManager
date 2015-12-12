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
package com.gaia3d.tadpole.spatial.data.core.ui.editor.extension;

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

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.utils.GEOJSONUtils;
import com.gaia3d.tadpole.spatial.data.core.ui.preference.data.SpatialGetPreferenceData;
import com.hangum.tadpole.ace.editor.core.utils.TadpoleEditorUtils;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.engine.sql.util.resultset.QueryExecuteResultDTO;
import com.hangum.tadpole.rdb.core.Activator;

/**
 * Tadpole extension to spatial data manager 
 * 
 * @author hangum
 *
 */
public class SpatialDataManagerMainEditor extends SpatialDataManagerDataHandler {
	private static final Logger logger = Logger.getLogger(SpatialDataManagerMainEditor.class);
	
	private QueryExecuteResultDTO rsDAO;
	
	/** 결과 중에 geojson column index */
	protected List<Integer> listGisColumnIndex = new ArrayList<>();

	/** 결과 중에 gis column 이 아닌 index */
	protected List<Integer> listNonGisColumnIndex = new ArrayList<>();

	/** mouse click job */
	private Job jobMouseClick = null;
	
	/** 쿼리의 컬럼 정보 */
	protected Map<Integer, String> mapColumnNames = null;
	
	@Override
	public void resultSetClick(final int selectIndex, final Map<Integer, Object> mapColumns) {
		if(jobMouseClick != null) {
			if(Job.RUNNING == jobMouseClick.getState()) {
				return;
			}
		}
		
		jobMouseClick = new Job("Spatial UserClick") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Click map data", IProgressMonitor.UNKNOWN);

				browserMap.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						clearClickedLayersMap();
						onSelecteddData(mapColumns);
					}
				});
				
				monitor.done();
				return Status.OK_STATUS;
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
			private void onSelecteddData(final Map<Integer, Object> mapColumns) {
				List<Map<Integer, Object>> listMapColumns = new ArrayList<>();
				listMapColumns.add(mapColumns);
				
				browserMap.evaluate(String.format(
										"onClickPoint('%s', '%s');", GEOJSONUtils.makeFeatureCollection(getEditorUserDB(), rsDAO, listGisColumnIndex, listMapColumns, false), getTooltip()
									)	// end String.format
						);
			}
			
			/**
			 * tooltip Text
			 * @return
			 */
			private String getTooltip() {
				
				StringBuffer sbPropertiValue = new StringBuffer();
				for(int i=1; i<listNonGisColumnIndex.size(); i++) {
					Integer index = listNonGisColumnIndex.get(i);
					
					if(listRealGisColumnIndex.contains(index)) continue;
					String strProperty = String.format("<b>%s</b>: %s",  mapColumnNames.get(index+1), mapColumns.get(index+1));
					if(i == (listNonGisColumnIndex.size()-1)) sbPropertiValue.append(strProperty);
					else sbPropertiValue.append(strProperty).append("<br>");
				}
				
				return TadpoleEditorUtils.getGrantText(sbPropertiValue.toString());
			}

		};
	
		jobMouseClick.setPriority(Job.INTERACTIVE);
		jobMouseClick.setName("Clikc map data");
		jobMouseClick.schedule();
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
	public void queryEndedExecute(final QueryExecuteResultDTO rsDAO) {
		if(rsDAO == null) return;
		if(rsDAO.getDataList() == null) return;
		
		this.rsDAO = rsDAO;
		listGisColumnIndex.clear();
		listNonGisColumnIndex.clear();
		
		mapColumnNames = rsDAO.getColumnName();
		final List<Map<Integer, Object>> resultData = rsDAO.getDataList().getData();

		for(int i=0; i<mapColumnNames.size(); i++) {
			String strSearchColumn = mapColumnNames.get(i);
			
			// geojson 으로 만들기위해 커럼을 분석합니다. 
			if(StringUtils.startsWithIgnoreCase(strSearchColumn, PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN)) {
				listGisColumnIndex.add(i);
			} else if(!listRealGisColumnIndex.contains(i)) {
				// -1은 사용자 ui를 구성하기 위해 순번 컬럼이 포함되는데 그것을 뺀것이다.
				listNonGisColumnIndex.add(i-1);
			}
		}
		
		/** 지도를 초기화 합니다 */
		clearAllLayersMap();
		
		/** 사용자 멤 데이터 */
		final int INT_SEND_COUNT = SpatialGetPreferenceData.getSendMapDataCount();
		/** 사용자 환경설정 */
		final String USER_OPTIONS = TadpoleEditorUtils.getGrantText(SpatialGetPreferenceData.getUserOptions());
		
		if(!listGisColumnIndex.isEmpty()) {
			Job job = new Job("Drawing map") {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					
					final int intRowSize = resultData.size();//toArray().length;
					final int intTotalDrawMapCount = intRowSize/INT_SEND_COUNT+1;
					monitor.beginTask("Drawing a map", intTotalDrawMapCount);
					
					try {
						int intStartIndex = 0;
						for(int i=0; i<intTotalDrawMapCount; i++) {
							monitor.setTaskName(String.format("Drawing a map ( %s / %s )", (i+1), intTotalDrawMapCount));
							monitor.worked(1);
							
							List<Map<Integer, Object>> listPartData = new ArrayList<>();
							if(intRowSize < INT_SEND_COUNT+intStartIndex) {
								listPartData = resultData.subList(intStartIndex, intRowSize);
							} else {
								listPartData = resultData.subList(intStartIndex, INT_SEND_COUNT+intStartIndex);
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
						logger.error("Drawing map rise exception.", e);
						
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
				private void drawMapInit(final List<Map<Integer, Object>> listGJson, final String strUserOptions) {
					browserMap.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							 
							browserMap.evaluate(String.format("drawingMapInit('%s', '%s');", 
									GEOJSONUtils.makeFeatureCollection(getEditorUserDB(), rsDAO, listGisColumnIndex, listGJson, false), strUserOptions)
												);
						}
					});
				}
				
				/**
				 * 지도에 데이터를 표시합니다.
				 * 
				 * @param strGeoJson
				 */
				private void drawMapAddData(final List<Map<Integer, Object>> listGJson) {
					browserMap.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							browserMap.evaluate(String.format("drawMapAddData('%s');", 
									GEOJSONUtils.makeFeatureCollection(getEditorUserDB(), rsDAO, listGisColumnIndex, listGJson, false))
												);
						}
					});
				}
				
			};
			
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
	 * 지도의 모든 layer를 삭제한다. 
	 */
	private void clearAllLayersMap() {
		browserMap.evaluate("clearAllLayersMap();");
	}
}
