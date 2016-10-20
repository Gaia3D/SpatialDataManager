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
 * altibase spatial db
 *
 * @author hangum
 */
public class AltibaseSQLSpatialDB extends AbstractSpatialDB {
	private static final Logger logger = Logger.getLogger(AltibaseSQLSpatialDB.class);
	
	/**
	 *	https://technet.tmaxsoft.com/upload/download/online/tibero/pver-20150504-000001/tibero_spatial/ch_03.html
	 */
	protected static final String ALTIBASE_GEOJSON_COLUMN_SQL = "ASTEXT(TADPOLESUB.%s) as " + PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN + "%s";
	
	/** 
	 * @param userDB
	 */
	public AltibaseSQLSpatialDB(UserDBDAO userDB) {
		super(userDB);
	}

	/* (non-Javadoc)
	 * @see com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB#isSpatialDBImage()
	 */
	@Override
	public Image isSpatialDBImage() {
		if( isQueryExist("SELECT F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID FROM STO.STO_GEOMETRY_COLUMNS") ) return SpatialUtils.getMapMakerIcon();
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
			rs = stmt.executeQuery("SELECT F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID FROM STO.STO_GEOMETRY_COLUMNS");
			
			while(rs.next()) {
				// 현재 altibase는 오브젝트 탐색기에 보여줄때 스키마명.테이블 명으로 보여주고 있어서 아래와 같이 구성했습니다. 
				// mysql, oralce 처럼 스키마 명을 상단으로 뺀다면 코드 수정을 해야합니다.  - hangum
				String tableName = rs.getString("F_TABLE_SCHEMA") + "." + rs.getString("F_TABLE_NAME");
				
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
		dao.setSpatialQuery(ALTIBASE_GEOJSON_COLUMN_SQL);
		try {
			return sqlCostume(dao);
		} catch (Exception e) {
			logger.error("spatial query maker", e);
		}
		
		return dao;
	}

}
