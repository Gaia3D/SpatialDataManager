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
 * mssql shape file import
 * 
 * @author hangum
 *
 */
public class MssqlShapeFileImportDialog extends ShapeFileImportDialog {
	private static final Logger logger = Logger.getLogger(MssqlShapeFileImportDialog.class);
	
	/** crate table statement head */
	public static String CREATE_TABLE_HEAD = 
			" CREATE TABLE %s(			\n" +  
			"	sdm_gid int NOT NULL IDENTITY(1,1) PRIMARY KEY			\n";
		
	public static String CREATE_TABLE_GEO = 
		"	,%s geometry \n";
	
	public static String CREATE_TABLE_NORMAL =
		"	,%s text		\n";
					
	public static String CREATE_TABLE_KEY =
		");";
		
	/** define insert into statement */
	public static String INSERT_STATEMENT = "INSERT INTO %s(%s)\n VALUES(%s);\n";
	public static String INSERT_VALUE_GEOM = ",geometry::STGeomFromText('%s', %s)";
	public static String INSERT_VALUE_NONE = ",'%s'";
	
	public MssqlShapeFileImportDialog(Shell parentShell, UserDBDAO userDB) {
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
						strTmpValue = String.format(INSERT_VALUE_GEOM, strTmpValue, shapeDto.getSrid());
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
