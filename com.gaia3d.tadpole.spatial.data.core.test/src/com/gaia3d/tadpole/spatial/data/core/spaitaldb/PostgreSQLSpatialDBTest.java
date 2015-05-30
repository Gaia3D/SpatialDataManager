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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.dao.RequestSpatialQueryDAO;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * postgresql spatial db test
 *
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 30.
 *
 */
public class PostgreSQLSpatialDBTest {
	SpatialDB spatialDB = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		UserDBDAO userDB = new UserDBDAO();
		
		final String dbUrl = String.format(
				DBDefine.POSTGRE_DEFAULT.getDB_URL_INFO(), 
				"127.0.0.1", "5432", "tadpole");
		
		
		userDB.setDbms_type(DBDefine.POSTGRE_DEFAULT.getDBToString());
		userDB.setUrl(dbUrl);
		userDB.setDb("tadpole");
		userDB.setDisplay_name("junit test");
		userDB.setHost("127.0.0.1");
		userDB.setPort("5432");
		userDB.setUsers("postgres");
		userDB.setPasswd("tadpole");
		
		userDB.setLocale("");
		
		SpatiaDBFactory factory = new SpatiaDBFactory();
		spatialDB = factory.getSpatialDB(userDB);
	}

	/**
	 * Test method for {@link com.gaia3d.tadpole.spatial.data.core.spaitaldb.PostgreSQLSpatialDB#PostgreSQLSpatialDB(com.hangum.tadpole.engine.query.dao.system.UserDBDAO)}.
	 */
	@Test
	public void testPostgreSQLSpatialDB() {
		if(spatialDB == null) fail("do not support spatial database");
		
		assertTrue(true);
	}

//	/**
//	 * image는 이클립스가 동작해야해서 막아놓습니다.
//	 *
//	 * Test method for {@link com.gaia3d.tadpole.spatial.data.core.spaitaldb.PostgreSQLSpatialDB#isSpatialDBImage()}.
//	 */
//	@Test
//	public void testIsSpatialDBImage() {
//		Image img = spatialDB.isSpatialDBImage();
////		if(img == null) fail("Do not support spatial database");
//		
//		assertTrue(true);
//	}

	/**
	 * Test method for {@link com.gaia3d.tadpole.spatial.data.core.spaitaldb.PostgreSQLSpatialDB#getSpatialTableColumn()}.
	 */
	@Test
	public void testGetSpatialTableColumn() {
		Map<String, List<String>> mapSpatialTableColumn = spatialDB.getSpatialTableColumn();
		if(mapSpatialTableColumn.isEmpty()) fail("Do not support spaatial database");
		
		boolean isRiseError = false;
		Set<String> tableColumn = mapSpatialTableColumn.keySet();
		for (String strTable : tableColumn) {
			List<String> listSpatialColumn = mapSpatialTableColumn.get(strTable);
			
			if(listSpatialColumn.isEmpty()) {
				isRiseError = true;
				fail(strTable + " is not found spatial columns.");
			}
		}
	}

	/**
	 * Test method for {@link com.gaia3d.tadpole.spatial.data.core.spaitaldb.PostgreSQLSpatialDB#makeSpatialQuery(com.gaia3d.tadpole.spatial.data.core.spaitaldb.dao.RequestSpatialQueryDAO)}.
	 */
	@Test
	public void testMakeSpatialQuery() {
		String strUserQuery = "SELECT gid, city, latitude, country, rank, population, longitude, geom  FROM world_cities";
		
		RequestSpatialQueryDAO dao = new RequestSpatialQueryDAO(strUserQuery);
		dao = spatialDB.makeSpatialQuery(dao);

		// count geo column
		List<Integer> listSpatialColumn = dao.getListRealGisColumnIndex();
		// fully get query
		String strFullyQuery = dao.getTadpoleFullyQuery();
		if(strFullyQuery.equals("")) fail("This query not is spatial");
		else {
			System.out.println("count of get column is " + listSpatialColumn.size());
			System.out.println("fully get query is \n" + strFullyQuery);				
		}

	}

}
