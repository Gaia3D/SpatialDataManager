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
 * oracle spatial db
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 27.
 *
 */
public class OracleSQLSpatialDB extends AbstractSpatialDB {
	private static final Logger logger = Logger.getLogger(OracleSQLSpatialDB.class);
	
	/**
	 * ORACLE 컬럼을 gis 로 처리하기 위해 작업합니다. (Oracle 11g)
	 * 
	 * 
	 * 참조: SDO_CS Package (Coordinate System Transformation) : http://docs.oracle.com/cd/B28359_01/appdev.111/b28400/sdo_cs_ref.htm#SPATL140
	 * 		http://docs.oracle.com/cd/B12037_01/appdev.101/b10826/sdo_objrelschema.htm
	 *		https://docs.oracle.com/cd/B19306_01/appdev.102/b14255/toc.htm
	 *		sample database : http://www.oracle.com/technetwork/middleware/mapviewer/downloads/navteq-data-download-168399.html
	 */
	protected static final String ORACLE_GEOJSON_COLUMN_SQL = "SDO_CS.TRANSFORM(TADPOLESUB.%s, 4326) as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
//	protected static final String ORACLE_GEOJSON_COLUMN_SQL = "TADPOLESUB.%s as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
	
	/** 
	 * @param userDB
	 */
	public OracleSQLSpatialDB(UserDBDAO userDB) {
		super(userDB);
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB#isSpatialDBImage()
	 */
	@Override
	public Image isSpatialDBImage() {
		if( isQueryExist("SELECT * FROM user_sdo_geom_metadata") ) return SpatialUtils.getMapMakerIcon();
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
			rs = stmt.executeQuery("select * from user_sdo_geom_metadata");
			
			while(rs.next()) {
				String tableName = rs.getString("table_name");
				
				if(!mapColumnDescList.containsKey(tableName)) {
					List<String> listColumns = new ArrayList();
					listColumns.add(rs.getString("column_name"));
					
					mapColumnDescList.put(tableName, listColumns);
				} else {
					List<String> listColumns = mapColumnDescList.get(tableName);
					listColumns.add(rs.getString("column_name"));
					
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
