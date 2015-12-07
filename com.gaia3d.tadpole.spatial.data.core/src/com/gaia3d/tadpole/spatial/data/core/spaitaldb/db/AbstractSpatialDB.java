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
package com.gaia3d.tadpole.spatial.data.core.spaitaldb.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.SpatiaDBFactory;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.dao.RequestSpatialQueryDAO;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.utils.GEOJSONUtils;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.utils.SDMUtiils;
import com.gaia3d.tadpole.spatial.data.core.ui.editor.extension.SpatialDataManagerDataHandler;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.engine.sql.util.QueryUtils;
import com.hangum.tadpole.engine.sql.util.resultset.QueryExecuteResultDTO;
import com.hangum.tadpole.engine.sql.util.resultset.ResultSetUtils;

/**
 * abstract spatial db
 *
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 27.
 *
 */
public abstract class AbstractSpatialDB implements SpatialDB {
	private static final Logger logger = Logger.getLogger(AbstractSpatialDB.class);

	protected UserDBDAO userDB;

	/**
	 * 
	 */
	public AbstractSpatialDB(UserDBDAO userDB) {
		this.userDB = userDB;
	}

	/**
	 * query
	 * 
	 * @param strQuery
	 * @return
	 * @throws Exception
	 */
	protected boolean isQueryExist(final String strQuery) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = TadpoleSQLManager.getInstance(userDB).getDataSource().getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strQuery);
			
			return true;
		} catch (Exception e1) {
			logger.error("connection viewer decoration extension " + e1);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}
		
		return false;
	}
	
	/**
	 * spatial column이 있다면 원하는 형태로 조작한다.
	 * 
	 * 현재는 {@code SpatialDataManagerMainEditor#mapGisColumnData} 에 테이블과 컬럼이 있는지 보고 해당하면 서브 쿼리를 만들어 조작하도록 합니다.
	 */
	protected RequestSpatialQueryDAO sqlCostume(RequestSpatialQueryDAO dao) throws Exception {
		List<String> addCostumeColumn = new ArrayList<String>();
		List<Integer> listRealGisColumnIndex = new ArrayList<Integer>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = TadpoleSQLManager.getInstance(getUserDB()).getDataSource().getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(dao.getOrigianlQuery());
			
			Iterator<Map> iteMap = ResultSetUtils.getColumnTableColumnName(getUserDB(), rs.getMetaData()).values().iterator();
			int intIndex = 0;
			while(iteMap.hasNext()) {
				Map mapOriginal = (Map)iteMap.next();
				// 0 번째 컬럼은 순번 컬럼이므로 타입이 없다. 
				if(mapOriginal.isEmpty()) continue;
				
				String strSearchTable 	= (String)mapOriginal.get("table");
				String strSearchColumn 	= (String)mapOriginal.get("column");
				String strSearchType 	= (String)mapOriginal.get("type");
				String strSearchTypeName = (String)mapOriginal.get("typeName");
				
//				if(logger.isDebugEnabled()) {
//					logger.debug("==> [strSearchColumn]" + strSearchColumn + "\t [strSearchType]" + strSearchType + "\t[strSearchTypeName]" + strSearchTypeName);
//				}
				
				if(SDMUtiils.isSpitailColumn(userDB, strSearchType)) {
					addCostumeColumn.add(strSearchColumn);
					listRealGisColumnIndex.add(intIndex);
				}
				
				intIndex++;
			}	// end while
			
			// geo 컬럼이 있는 것이다.
			boolean isColumnNull = false;
			if(!addCostumeColumn.isEmpty()) {
				
				String strAddCustomeColumn = "";
				for(int i=0; i<addCostumeColumn.size(); i++) {
					String strColumn = StringUtils.trim(addCostumeColumn.get(i));
					// 
					if("".equals(strColumn)) isColumnNull = true;
					strAddCustomeColumn += String.format(dao.getSpatialQuery(), strColumn, strColumn);
					if(addCostumeColumn.size()-1 != i) strAddCustomeColumn += ", ";
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("Add Column is " + strAddCustomeColumn);
					logger.debug("full SQL is " + String.format(SpatialDataManagerDataHandler.GEOJSON_FULLY_SQL_FORMAT, strAddCustomeColumn, dao.getOrigianlQuery()));
				}

				//
				// 컬럼 중에 어느 하나라도 컬럼명을 알아오지 못하면 query 를 변형하지 않는다.
				// 이럴 경우는 geo 타입을 다른 타입으로 캐스팅했을 경우 원래 타입이 geo 타입이라 들어오게 되는데... 인가?
				//
				if(!isColumnNull) {
					dao.setTadpoleFullyQuery( String.format(SpatialDataManagerDataHandler.GEOJSON_FULLY_SQL_FORMAT, strAddCustomeColumn, dao.getOrigianlQuery()) );
				}
			}
			// 컬럼 중에 어느 하나라도 컬럼명을 알아오지 못하면 query 를 변형하지 않는다.
			if(!isColumnNull) {
				dao.setListRealGisColumnIndex(listRealGisColumnIndex);
			}
			
		} catch (Exception e) {
			logger.error("SpatialDataManager extension" + e);
			
			throw e;
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}

		return dao;
	}
	
	@Override
	public String makeGeojsonFeature(String query) throws Exception {
		
		SpatiaDBFactory factory = new SpatiaDBFactory();
		SpatialDB spatialDB = factory.getSpatialDB(userDB);
		
		RequestSpatialQueryDAO dao = new RequestSpatialQueryDAO(query);
		spatialDB.makeSpatialQuery(dao);
		List<Integer> listRealGisColumnIndex = dao.getListRealGisColumnIndex();
		if(listRealGisColumnIndex.isEmpty()) {
			throw new Exception("Do not find geo column. Please check your geo columns");
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("================================================================================");
			logger.debug(dao.getTadpoleFullyQuery());
			logger.debug("================================================================================");
		}
		
		QueryExecuteResultDTO queryResultDto = QueryUtils.executeQuery(userDB, dao.getTadpoleFullyQuery(), 0, 1000);
		Map<Integer, String> mapColumnNames = queryResultDto.getColumnName();
		List<Integer> listGisColumnIndex = new ArrayList<>();
		List<Integer> listNonGisColumnIndex = new ArrayList<>();
		
		if(logger.isDebugEnabled()) logger.debug("result is "+ queryResultDto.getDataList().getData().size());

		for(int i=0; i<mapColumnNames.size(); i++) {
			String strSearchColumn = mapColumnNames.get(i);
			
			// geojson 으로 만들기위해 커럼을 분석합니다. 
			if(StringUtils.startsWithIgnoreCase(strSearchColumn, PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN)) {
				listGisColumnIndex.add(i);
			} else if(!listRealGisColumnIndex.contains(i)) {
				// -1은 사용자 ui를 구성하기 위해 순번 컬럼이 포함되는데 그것을 뺀것이다.
				listNonGisColumnIndex.add(i-1);
			}
		}
		
		String strFeatureCollection = GEOJSONUtils.makeFeatureCollection(userDB, queryResultDto, listGisColumnIndex, queryResultDto.getDataList().getData(), true);
		return strFeatureCollection;
	}
	
	/**
	 * @return the userDB
	 */
	public UserDBDAO getUserDB() {
		return userDB;
	}
}
