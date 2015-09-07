/*******************************************************************************
 * Copyright (c) 2012 - 2015 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.ui.wizard.shapeimport;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Shape file import wizard
 * 
 * http://docs.geotools.org/stable/userguide/library/data/shape.html
 * https://en.wikipedia.org/wiki/Shapefile
 * 
 * 	1.1 Shapefile shape format (.shp)
 * 	1.2 Shapefile shape index format (.shx)
 * 	1.3 Shapefile attribute format (.dbf)
 * 	1.4 Shapefile spatial index format (.sbn)
 * 
 * @author hangum
 *
 */
public class ShapeFileImportWizard extends Wizard {
	private static final Logger logger = Logger.getLogger(ShapeFileImportWizard.class);
	
	private UserDBDAO userDB;
	private int intCommitCount = 1000;
	
	/** define insert into statement */
	private String INSERT_STATEMENT = "INSERT INTO %s(%s)\n VALUES(%s);\n";
	
	private String INSERT_VALUE_GEOM = ",ST_GeomFromText('%s', %s)";
	private String INSERT_VALUE_NONE = ",'%s'";

	private ShapeFileImportWizardPage uploadWizardPage;
	
	public ShapeFileImportWizard(UserDBDAO userDB) {
		setWindowTitle("Shape to DB import wizard");
		
		this.userDB = userDB;
	}

	@Override
	public void addPages() {
		uploadWizardPage = new ShapeFileImportWizardPage();
		addPage(uploadWizardPage);
	}
	
	@Override
	public boolean performFinish() {
		ShapeImportDTO shapeDto = uploadWizardPage.getDTO();
		
		java.sql.Connection javaConn = null;
		Statement statement = null;
		
		try {
			SqlMapClient client = TadpoleSQLManager.getInstance(userDB);
			javaConn = client.getDataSource().getConnection();
			statement = javaConn.createStatement();
			
			// create 문을 인서트하고.
			if(logger.isDebugEnabled()) logger.debug("create 문을 생성을 시작 했습니다.");
			statement.execute(shapeDto.getCreate_statement());
			if(logger.isDebugEnabled()) logger.debug("create 문을 생성했습니다.");
			
			// insert into 문을 인서트합니다잉.
			int count = 0;
			List<Map<String, Object>> listShape = shapeDto.listShape;
			for (Map<String, Object> mapShape : listShape) {
				
				StringBuffer columnName = new StringBuffer();
				StringBuffer values = new StringBuffer();
				for(String strKey : mapShape.keySet()) {
					columnName.append( String.format("%s,", strKey) );
					Object obj = mapShape.get(strKey);
					
					String strTmpValue = StringEscapeUtils.escapeSql(obj.toString());
					if(StringUtils.startsWith(obj.getClass().getName(), "com.vividsolutions.jts.geom")) {
						strTmpValue = String.format(INSERT_VALUE_GEOM, strTmpValue, shapeDto.getSrid());
					} else {
						strTmpValue = String.format(INSERT_VALUE_NONE, strTmpValue);
					}
					
//					strTmpValue = StringHelper.escapeSQL(strTmpValue);
					
					values.append(strTmpValue);
				}
				
				String strQuery = String.format(INSERT_STATEMENT, shapeDto.getTableName(), 
						StringUtils.removeEnd(columnName.toString(), ","), 
						StringUtils.removeStart(values.toString(), ","));
				statement.addBatch(strQuery);
				
				if(++count % intCommitCount == 0) {
					if(logger.isDebugEnabled()) logger.debug("executeBatch complement.");
					statement.executeBatch();
					count = 0;
				}
			}
			statement.executeBatch();
		} catch(Exception e) {
			logger.error("Rise excepiton", e);
			
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		
			return false;
		} finally {
			try { statement.close();} catch(Exception e) {}
			try { javaConn.close(); } catch(Exception e){}
		}
		
		MessageDialog.openInformation(getShell(), "Confirm", "Successful data upload");
		
		return true;
	}

}
