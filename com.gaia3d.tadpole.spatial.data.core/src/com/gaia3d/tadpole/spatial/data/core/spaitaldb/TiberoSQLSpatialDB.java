/*******************************************************************************
 * Copyright (c) 2012 - 2016 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.spaitaldb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.dao.RequestSpatialQueryDAO;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.AbstractSpatialDB;
import com.gaia3d.tadpole.spatial.data.core.ui.utils.SpatialUtils;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * tibero spatial db
 *
 * @author hangum
 */
public class TiberoSQLSpatialDB extends AbstractSpatialDB {
	private static final Logger logger = Logger.getLogger(TiberoSQLSpatialDB.class);
	
	/**
	 *	https://technet.tmaxsoft.com/upload/download/online/tibero/pver-20150504-000001/index.html
	 */
	protected static final String ORACLE_GEOJSON_COLUMN_SQL = "ST_ASTEXT(TADPOLESUB.%s) as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
	
	/** 
	 * @param userDB
	 */
	public TiberoSQLSpatialDB(UserDBDAO userDB) {
		super(userDB);
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB#isSpatialDBImage()
	 */
	@Override
	public Image isSpatialDBImage() {
		if( isQueryExist("SELECT * FROM SYSGIS.GEOMETRY_COLUMNS_BASE") ) return SpatialUtils.getMapMakerIcon();
		else return null;
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB#getSpatialTableColumn()
	 */
	@Override
	public Map<String, List<String>> getSpatialTableColumn() {
		Map<String, List<String>> mapColumnDescList = new HashMap<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = TadpoleSQLManager.getInstance(userDB).getDataSource().getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM SYSGIS.GEOMETRY_COLUMNS_BASE");
			
			while(rs.next()) {
				String tableName = rs.getString("F_TABLE_NAME");
				
				if(!mapColumnDescList.containsKey(tableName)) {
					List<String> listColumns = new ArrayList();
					listColumns.add(rs.getString("F_GEOMETRY_COLUMN"));
					
					mapColumnDescList.put(tableName, listColumns);
				} else {
					List<String> listColumns = mapColumnDescList.get(tableName);
					listColumns.add(rs.getString("F_GEOMETRY_COLUMN"));
					
					mapColumnDescList.put(tableName, listColumns);
				}
			}
		} catch (Exception e1) {
			logger.error("connection viewer decoration extension" + e1);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}
		
		return mapColumnDescList;
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB#makeSpatialQuery(com.gaia3d.tadpole.spatial.data.core.spaitaldb.dao.RequestSpatialQueryDAO)
	 */
	@Override
	public RequestSpatialQueryDAO makeSpatialQuery(RequestSpatialQueryDAO dao) {
		dao.setSpatialQuery(ORACLE_GEOJSON_COLUMN_SQL);
		try {
			return sqlCostume(dao);
		} catch (Exception e) {
			logger.error("spatial query maker", e);
		}
		
		return dao;
	}

}
