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
package com.gaia3d.tadpole.spatial.data.core.ui.connection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import com.gaia3d.tadpole.spatial.data.core.ui.utils.SpatialUtils;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.rdb.core.extensionpoint.definition.IConnectionDecoration;
import com.hangum.tadpole.sql.dao.system.UserDBDAO;

/**
 * connection viewer decorator
 * 
 * @author hangum
 *
 */
public class ConnectionViewerDecorator implements IConnectionDecoration {
	private static final Logger logger = Logger.getLogger(ConnectionViewerDecorator.class);
	
	@Override
	public Image getImage(UserDBDAO userDB) {
		if(userDB == null) return null;
		
		if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			
			try {
				conn = TadpoleSQLManager.getInstance(userDB).getDataSource().getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM geometry_columns");
				
				return SpatialUtils.getMapMakerIcon();
			} catch (Exception e1) {
				logger.error("connection viewer decoration extension" + e1);
			} finally {
				if(rs != null) try {rs.close(); } catch(Exception e) {}
				if(stmt != null) try { stmt.close(); } catch(Exception e) {}
				if(conn != null) try { conn.close(); } catch(Exception e) {}
			}
		} else if(userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT) {
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			
			try {
				conn = TadpoleSQLManager.getInstance(userDB).getDataSource().getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_CATALOG = '" + userDB.getDb() + "' AND DATA_TYPE like 'geo%'");
				
				return SpatialUtils.getMapMakerIcon();
			} catch (Exception e1) {
				logger.error("connection viewer decoration extension" + e1);
			} finally {
				if(rs != null) try {rs.close(); } catch(Exception e) {}
				if(stmt != null) try { stmt.close(); } catch(Exception e) {}
				if(conn != null) try { conn.close(); } catch(Exception e) {}
			}
		}	// end postgredb
		
		return null;
	}
}
