package com.gaia3d.tadpole.spatial.data.core.spatical.editor;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;

import com.gaia3d.tadpole.spatial.data.core.Activator;
import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
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
	 * 컬럼을 st_AsGeoJson 으로 변환합니다.
	 * 참조: http://postgis.net/docs/ST_Transform.html
	 */
	protected static final String GEOJSON_COLUMN_SQL = "st_AsGeoJson(st_transform(TADPOLESUB.%s, 4326)) as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
	
	/** 결과 중에 geojson column index */
	protected List<Integer> listGisColumnIndex = new ArrayList<>();
	

	/** 
	 * <pre>
	 * 	geometry 테이블과 컬럼 정보를 가지고있습니다.
	 * 
	 * 	테이블명, 컬럼명
	 * </pre> 
	 */
	protected Map<String, List<String>> mapGisColumnData = new HashMap<>();
	
	/** 지도가 들어갈 브라우저 */
	protected Browser browserMap;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.verticalSpacing = 2;
		gl_composite.horizontalSpacing = 2;
		gl_composite.marginHeight = 2;
		gl_composite.marginWidth = 2;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeHead = new Composite(composite, SWT.NONE);
		compositeHead.setLayout(new GridLayout(1, false));
		compositeHead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMap = new Label(compositeHead, SWT.NONE);
		lblMap.setText("Leaflet Map");
		
		Composite compositeBody = new Composite(composite, SWT.NONE);
		compositeBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeBody.setLayout(new GridLayout(1, false));
		
		browserMap = new Browser(compositeBody, SWT.BORDER);
		browserMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		initUI();
	}
	
	@Override
	public void initExtension(UserDBDAO userDB) {
		super.initExtension(userDB);
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if(getEditorUserDB().getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
				conn = TadpoleSQLManager.getInstance(getEditorUserDB()).getDataSource().getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM geometry_columns");
				
				while(rs.next()) {
					String tableName = rs.getString("f_table_name");
					
					if(!mapGisColumnData.containsKey(tableName)) {
						List<String> listColumns = new ArrayList();
						listColumns.add(rs.getString("f_geometry_column"));
						
						mapGisColumnData.put(tableName, listColumns);
					} else {
						List<String> listColumns = mapGisColumnData.get(tableName);
						listColumns.add(rs.getString("f_geometry_column"));
						
						mapGisColumnData.put(tableName, listColumns);
					}
				}
				
				super.setEnableExtension(true);
			} else {
				super.setEnableExtension(false);	
			}
			
		} catch (Exception e1) {
			logger.error("GoogleMap extension" + e1);
	
			super.setEnableExtension(false);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}

	}
	
	/**
	 * UI가 처음 호출될때 초기화 합니다.
	 */
	public void initUI() {
		
		try {
			Bundle bundle = Activator.getDefault().getBundle();
			URL resource = bundle.getResource("/resources/map/LeafletMap.html");
			String strHtml = IOUtils.toString(resource.openStream());
			
			browserMap.setText(strHtml);
			
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
//		if(logger.isDebugEnabled()) logger.debug("orginal sql is " + strSQL);
		List<String> addCostumeColumn = new ArrayList<String>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
				conn = TadpoleSQLManager.getInstance(getEditorUserDB()).getDataSource().getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery(strSQL);
				
				Iterator<Map> iteMap = ResultSetUtils.getColumnTableColumnName(getEditorUserDB(), rs.getMetaData()).values().iterator();
				while(iteMap.hasNext()) {
					Map mapOriginal = (Map)iteMap.next();
					
					String strSearchTable 	= (String)mapOriginal.get("table");
					String strSearchColumn 	= (String)mapOriginal.get("column");
					
					if(mapGisColumnData.containsKey(strSearchTable)) {
						List<String> listColumn = mapGisColumnData.get(strSearchTable);
						
						if(listColumn.contains(strSearchColumn)) {
							addCostumeColumn.add(strSearchColumn);
						}
					}
				}	// end while
				
				if(!addCostumeColumn.isEmpty()) {
					
					String strAddCustomeColumn = "";
					for(int i=0; i<addCostumeColumn.size(); i++) {
						String strColumn = addCostumeColumn.get(i);
						
						if(addCostumeColumn.size()-1 == i) strAddCustomeColumn += String.format(GEOJSON_COLUMN_SQL, strColumn, strColumn);
						else strAddCustomeColumn += String.format(GEOJSON_COLUMN_SQL, strColumn, strColumn) + ", ";
						
					}
					
					if(logger.isDebugEnabled()) {
						logger.debug("Add Column is " + strAddCustomeColumn);
						logger.debug("full SQL is " + String.format(GEOJSON_FULLY_SQL_FORMAT, strAddCustomeColumn, strSQL));
					}
					
					return String.format(GEOJSON_FULLY_SQL_FORMAT, strAddCustomeColumn, strSQL);
				}
			
		} catch (Exception e1) {
			logger.error("GoogleMap extension" + e1);
	
			super.setEnableExtension(false);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}

		return strSQL;
	}
}
