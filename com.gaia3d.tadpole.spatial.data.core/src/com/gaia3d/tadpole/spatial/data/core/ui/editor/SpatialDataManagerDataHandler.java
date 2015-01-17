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
package com.gaia3d.tadpole.spatial.data.core.ui.editor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.gaia3d.tadpole.spatial.data.core.ui.editor.browserHandler.SpatialEditorFunction;
import com.gaia3d.tadpole.spatial.data.core.ui.editor.browserHandler.SpatialFunctionService;
import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.rdb.core.editors.main.MainEditor;
import com.hangum.tadpole.rdb.core.extensionpoint.definition.AMainEditorExtension;
import com.hangum.tadpole.sql.dao.system.UserDBDAO;
import com.hangum.tadpole.sql.util.resultset.ResultSetUtils;

/**
 * 올챙이 지도를 확장하기 위해서 
 * 1. 동작 가능한 환경인지 검사하고
 * 2. UI를 구성하고
 * 3. 확장에서 사용할 테이블 컬럼 정보를 걸러 냅니다. 
 * 
 * @author hangum
 *
 */
public abstract class SpatialDataManagerDataHandler extends AMainEditorExtension {
	private static final Logger logger = Logger.getLogger(SpatialDataManagerDataHandler.class);
	
	/**
	 * 사용자 쿼리에 geometry 컬럼이 있을 경우에 사용하기 위한 전체 쿼리
	 */
	protected static final String GEOJSON_FULLY_SQL_FORMAT = "SELECT *, %s FROM (%s) as TADPOLESUB";
	
	/**
	 * POSTGIS 컬럼을 st_AsGeoJson 으로 변환합니다.
	 * 참조: http://postgis.net/docs/ST_Transform.html
	 */
	protected static final String POSTGIS_GEOJSON_COLUMN_SQL = "st_AsGeoJson(st_transform(TADPOLESUB.%s, 4326)) as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
	
	/**
	 * MSSQL 컬럼을 st_AsGeoJson 으로 변환합니다.
	 * 참조: http://msdn.microsoft.com/en-us/magazine/dd434647.aspx
	 * 		http://msdn.microsoft.com/en-us/library/bb933790.aspx
	 */
	protected static final String MSSQL_GEOJSON_COLUMN_SQL = "TADPOLESUB.%s.STAsText() as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
	
	/**
	 * 쿼리 중에 리얼 gis 컬럼리스트이다.
	 * 리얼은 쿼리를 조작하여 gis 컬럼을 넣는데, 이 컬럼은 빠져있다.
	 */
	protected List<Integer> listRealGisColumnIndex = new ArrayList<Integer>();

	/** 지도가 들어갈 브라우저 */
	protected Browser browserMap;
	/** browser.browserFunction의 서비스 헨들러 */
	protected BrowserFunction editorService;
	
	@Override
	public void createPartControl(Composite parent, MainEditor mainEditor) {
		this.mainEditor = mainEditor;
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.verticalSpacing = 2;
		gl_composite.horizontalSpacing = 2;
		gl_composite.marginHeight = 2;
		gl_composite.marginWidth = 2;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		------------ 화면 타이틀 처리 시작 -------------------------------
//		java 에서 화면 타이틀을 보이지 않도록 수정합니다.(14.11.20)
//		이것은 자바 스크립트에서 처리하도록 합니다. 
//		Composite compositeHead = new Composite(composite, SWT.NONE);
//		compositeHead.setLayout(new GridLayout(1, false));
//		compositeHead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		Label lblMap = new Label(compositeHead, SWT.NONE);
//		lblMap.setText("Leaflet Map");
//		------------ 화면 타이틀 처리 종료 -------------------------------
		
		Composite compositeBody = new Composite(composite, SWT.NONE);
		compositeBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeBody.setLayout(new GridLayout(1, false));
		
		browserMap = new Browser(compositeBody, SWT.BORDER);
		browserMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		initUI();
	}
	
	@Override
	public void initExtension(UserDBDAO userDB) {
		if(userDB == null) {
			super.setEnableExtension(false);
			return;
		}
		
		super.initExtension(userDB);		
	}
	
	/**
	 * UI가 처음 호출될때 초기화 합니다.
	 */
	public void initUI() {
		
		try {
			browserMap.setUrl("resources/map/LeafletMap.html");
			registerBrowserFunctions();
		} catch (Exception e) {
			logger.error("initialize map initialize error", e);
		}
	}
	
	
	/**
	 * spatial column이 있다면 원하는 형태로 조작한다.
	 * 
	 * 현재는 {@code SpatialDataManagerMainEditor#mapGisColumnData} 에 테이블과 컬럼이 있는지 보고 해당하면 서브 쿼리를 만들어 조작하도록 합니다.
	 */
	@Override
	public String sqlCostume(String strSQL) {
		List<String> addCostumeColumn = new ArrayList<String>();
		listRealGisColumnIndex.clear();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = TadpoleSQLManager.getInstance(getEditorUserDB()).getDataSource().getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			
			Iterator<Map> iteMap = ResultSetUtils.getColumnTableColumnName(getEditorUserDB(), rs.getMetaData()).values().iterator();
			int intIndex = 0;
			while(iteMap.hasNext()) {
				Map mapOriginal = (Map)iteMap.next();
				// 0 번째 컬럼은 순번 컬럼이므로 타입이 없다. 
				if(mapOriginal.isEmpty()) continue;
				
				String strSearchTable 	= (String)mapOriginal.get("table");
				String strSearchColumn 	= (String)mapOriginal.get("column");
				String strSearchType 	= (String)mapOriginal.get("type");
				String strSearchTypeName = (String)mapOriginal.get("typeName");
				
				if(logger.isDebugEnabled()) {
					logger.debug("==> " + getEditorUserDB().getDBDefine());
					logger.debug("==> [strSearchColumn]" + strSearchColumn + "\t [strSearchType]" + strSearchType + "\t[strSearchTypeName]" + strSearchTypeName);
				}
				
				if(getEditorUserDB().getDBDefine() == DBDefine.POSTGRE_DEFAULT & strSearchType.equals("1111")) {
					addCostumeColumn.add(strSearchColumn);
					listRealGisColumnIndex.add(intIndex);
				} else if(getEditorUserDB().getDBDefine() == DBDefine.MSSQL_DEFAULT & strSearchType.equals("2004")) {
					addCostumeColumn.add(strSearchColumn);
					listRealGisColumnIndex.add(intIndex);
				}
				
				intIndex++;
			}	// end while
			
			// geo 컬럼이 있는 것이다.
			if(!addCostumeColumn.isEmpty()) {
				
				// 컬럼이 있다면 mainEditor의 화면중에, 지도 부분의 영역을 30%만큼 조절합니다.
				mainEditor.getSashFormExtension().getDisplay().asyncExec(new Runnable() {
					public void run() {
						int []intWidgetSizes = mainEditor.getSashFormExtension().getWeights();
						if(intWidgetSizes[0] != 100) {
							mainEditor.getSashFormExtension().setWeights(new int[] {70, 30});
						}
					}
				});
				// 컬럼이 있다면 mainEditor의 화면중에, 지도 부분의 영역을 30%만큼 조절합니다.
				
				String strAddCustomeColumn = "";
				for(int i=0; i<addCostumeColumn.size(); i++) {
					String strColumn = addCostumeColumn.get(i);
					if(getEditorUserDB().getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
						if(addCostumeColumn.size()-1 == i) strAddCustomeColumn += String.format(POSTGIS_GEOJSON_COLUMN_SQL, strColumn, strColumn);
						else strAddCustomeColumn += String.format(POSTGIS_GEOJSON_COLUMN_SQL, strColumn, strColumn) + ", ";
					} else if(getEditorUserDB().getDBDefine() == DBDefine.MSSQL_8_LE_DEFAULT || 
							getEditorUserDB().getDBDefine() == DBDefine.MSSQL_DEFAULT) {
						if(addCostumeColumn.size()-1 == i) strAddCustomeColumn += String.format(MSSQL_GEOJSON_COLUMN_SQL, strColumn, strColumn);
						else strAddCustomeColumn += String.format(MSSQL_GEOJSON_COLUMN_SQL, strColumn, strColumn) + ", ";
					}
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("Add Column is " + strAddCustomeColumn);
					logger.debug("full SQL is " + String.format(GEOJSON_FULLY_SQL_FORMAT, strAddCustomeColumn, strSQL));
				}
				
				return String.format(GEOJSON_FULLY_SQL_FORMAT, strAddCustomeColumn, strSQL);
			}
			
		} catch (Exception e1) {
			logger.error("SpatialDataManager extension" + e1);
	
			super.setEnableExtension(false);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}

		return strSQL;
	}
	
	/**
	 * register browser function
	 * 
	 */
	protected void registerBrowserFunctions() {
		editorService = new SpatialFunctionService(browserMap, SpatialEditorFunction.LEAFLET_SERVICE_HANDLER);
	}

}
