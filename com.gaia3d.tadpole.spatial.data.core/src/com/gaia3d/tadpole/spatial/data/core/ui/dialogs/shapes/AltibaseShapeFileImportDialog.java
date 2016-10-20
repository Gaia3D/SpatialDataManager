/*******************************************************************************
 * Copyright 2016 hangum
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
package com.gaia3d.tadpole.spatial.data.core.ui.dialogs.shapes;

import java.sql.Statement;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.dialog.msg.TDBErroDialog;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * altibase shape file import
 * 
 * 기본적인 메뉴얼은 다음을 참조합니다.(HDB_6.5.1_SpatialSQL.pdf)
 * 공간 테이블 생성 후에 공간 컬럼을 등록해야하는데 위의 메뉴얼 155(Gemotry 테이블 참조) 부분을 읽어보세요.
 * 
<pre>
 위의 데이터를 임포트하고 다음의 작업을 해줘야합니다.
 
-- 0) 테이블 생성
CREATE TABLE TB2 (F1 INTEGER PRIMARY KEY, F2 GEOMETRY);

-- 1) 공간 인덱스 생성하기
CREATE INDEX RT_IDX_TB2 ON TB2(F2);

-- 2) 프라이머리 키 생성하기

-- 3) 공간컬럼 등록하기
exec AddGeometryColumns( 'SYS', 'TB2', 'F2', 100 );

</pre>
 * 
 * @author hangum
 *
 */
public class AltibaseShapeFileImportDialog extends ShapeFileImportDialog {
	private static final Logger logger = Logger.getLogger(AltibaseShapeFileImportDialog.class);
	
	/** crate table statement head */
	public static String CREATE_TABLE_HEAD 	= " CREATE TABLE %s(			\n" +  
			"	sdm_gid NUMBER \n";
		
	public static String CREATE_TABLE_GEO = "	,%s GEOMETRY \n";
	
	public static String CREATE_TABLE_NORMAL = "	,%s VARCHAR(4000)		\n";
					
	public static String CREATE_TABLE_KEY = ");";
		
	/** define insert into statement */
	public static String INSERT_STATEMENT = "INSERT INTO %s(%s)\n VALUES(%s);\n";
	public static String INSERT_VALUE_GEOM = ",GEOMFROMTEXT('%s')";
	public static String INSERT_VALUE_NONE = ",'%s'";
	
	public AltibaseShapeFileImportDialog(Shell parentShell, UserDBDAO userDB) {
		super(parentShell, userDB);
	}

	@Override
	protected void okPressed() {

		if(!MessageDialog.openConfirm(getShell(), "Confirm", "Do you want to upload?")) return ;
		final ShapeImportDTO shapeDto = getDTO();
		java.sql.Connection javaConn = null;
		Statement statement = null;
		
		String strCharSet = textCharacterSet.getText();
		
		try {
			SqlMapClient client = TadpoleSQLManager.getInstance(userDB);
			javaConn = client.getDataSource().getConnection();
			statement = javaConn.createStatement();
			
			// create 문을 인서트하고.
			if(logger.isDebugEnabled()) logger.debug(shapeDto.getCreate_statement());
			statement.execute(shapeDto.getCreate_statement());
			
			// insert into 문을 인서트합니다잉.
			int count = 0;
			int totCount = 0;
			for (Map<String, Object> mapShape : shapeDto.getListShape()) {
				
				StringBuffer columnName = new StringBuffer();
				StringBuffer values = new StringBuffer();
				for(String strKey : mapShape.keySet()) {
					columnName.append( String.format("%s,", strKey) );
					Object obj = mapShape.get(strKey);
					
					String strTmpValue = StringEscapeUtils.escapeSql(obj==null?"":obj.toString());
					if(obj != null && StringUtils.startsWith(obj.getClass().getName(), "com.vividsolutions.jts.geom")) {
						strTmpValue = String.format(INSERT_VALUE_GEOM, strTmpValue);
					} else {
						strTmpValue = String.format(INSERT_VALUE_NONE, strTmpValue);
						if(!"".equals(strCharSet)) strTmpValue = new String(strTmpValue.getBytes("8859_1"), strCharSet);//"euc-kr");
					}
					
					values.append(strTmpValue);
				}
				
				String strQuery = String.format(INSERT_STATEMENT, shapeDto.getTableName(), 
						StringUtils.removeEnd(columnName.toString(), ","), 
						StringUtils.removeStart(values.toString(), ","));
				
				if(logger.isDebugEnabled()) logger.debug(strQuery);
				
				statement.addBatch(strQuery);
				totCount += 1;
				if(++count % intCommitCount == 0) {
					if(logger.isDebugEnabled()) logger.debug("executeBatch complement.");
					statement.executeBatch();
					count = 0;
				}
			}
			statement.executeBatch();
			
			MessageDialog.openInformation(null, "Confirm", "Successful data upload");
		} catch(Exception e) {
			logger.error("Rise excepiton", e);
			
			TDBErroDialog dialog = new TDBErroDialog(getShell(), "Error", "Shape file parse exception.\n" + e.getMessage());
			dialog.open();
		} finally {
			try { statement.close();} catch(Exception e) {}
			try { javaConn.close(); } catch(Exception e){}
		}
	}
	
	/**
	 * generate create statement
	 * 
	 * @param strTableName
	 * @param strSRID
	 * @return
	 */
	protected String generateCreateStatement(String strTableName, String strSRID) {
		if(listShape.isEmpty()) return "";
	
		StringBuffer sbSQL = new StringBuffer(String.format(CREATE_TABLE_HEAD, strTableName));
		
		Map<String, Object> mapShape = listShape.get(0);
		for(String strKey : mapShape.keySet()) {
			Object obj = mapShape.get(strKey);
			if(logger.isDebugEnabled()) logger.debug(" column type is " + obj.getClass().getName() + ": " + obj);
			
			if(obj == null) {
				sbSQL.append(String.format(CREATE_TABLE_NORMAL, strKey));
			} else if(StringUtils.startsWith(obj.getClass().getName(), "com.vividsolutions.jts.geom")) {
				String strColumnData =""+ mapShape.get(strKey);
				String strGeoType = StringUtils.substringBefore(strColumnData, "(");
				
				sbSQL.append(String.format(CREATE_TABLE_GEO, strKey));//, strGeoType, strSRID));
			} else {
				sbSQL.append(String.format(CREATE_TABLE_NORMAL, strKey));
			}
		}
		
		sbSQL.append(String.format(CREATE_TABLE_KEY, strTableName));
		
		return sbSQL.toString();
	}
}
