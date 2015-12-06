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
 * postgresql spatial db
 *
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 27.
 *
 */
public class PostgreSQLSpatialDB extends AbstractSpatialDB {
	private static final Logger logger = Logger.getLogger(PostgreSQLSpatialDB.class);
	/**
	 * POSTGIS 컬럼을 st_AsGeoJson 으로 변환합니다. (Postgres 9.3.5.2) 
	 * 
	 * 참조: http://postgis.net/docs/ST_Transform.html
	 */
	protected static final String POSTGIS_GEOJSON_COLUMN_SQL = "st_AsGeoJson(st_transform(TADPOLESUB.%s, 4326)) as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
	
	/**
	 * @param userDB
	 */
	public PostgreSQLSpatialDB(UserDBDAO userDB) {
		super(userDB);
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.ui.define.SpatialDB#isSpatialDB()
	 */
	@Override
	public Image isSpatialDBImage() {
		if( isQueryExist("SELECT * FROM geometry_columns") ) return SpatialUtils.getMapMakerIcon();
		else return null;
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB#isSpatialTableColumn()
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
			rs = stmt.executeQuery("SELECT * FROM geometry_columns");
			while(rs.next()) {
				String tableName = rs.getString("f_table_name");
				
				if(!mapColumnDescList.containsKey(tableName)) {
					List<String> listColumns = new ArrayList();
					listColumns.add(rs.getString("f_geometry_column"));
					
					mapColumnDescList.put(tableName, listColumns);
				} else {
					List<String> listColumns = mapColumnDescList.get(tableName);
					listColumns.add(rs.getString("f_geometry_column"));
					
					mapColumnDescList.put(tableName, listColumns);
				}
			}
		} catch (Exception e1) {
			logger.error("Find geo tableColumn " + e1);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}
		return mapColumnDescList;
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB#makeSpatialQuery(java.lang.String)
	 */
	@Override
	public RequestSpatialQueryDAO makeSpatialQuery(RequestSpatialQueryDAO dao) {
		dao.setSpatialQuery(POSTGIS_GEOJSON_COLUMN_SQL);
		try {
			return sqlCostume(dao);
		} catch (Exception e) {
			logger.error("spatial query maker", e);
		}
		
		return dao;
	}

}
