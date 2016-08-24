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

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * spatical db factory
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 27.
 *
 */
public class SpatiaDBFactory {

	/**
	 * 테드폴에서 지원하는 공간 디비를 리턴합니다.
	 */
	public SpatialDB getSpatialDB(UserDBDAO userDB) {
		if(userDB.getDBDefine() == null) return null;
		
		if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
			return new PostgreSQLSpatialDB(userDB);
		} else if(userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT) {
			return new MSSQLSpatialDB(userDB);
		} else if(userDB.getDBDefine() == DBDefine.ORACLE_DEFAULT) {
			return new OracleSQLSpatialDB(userDB);
		} else if(userDB.getDBDefine() == DBDefine.TIBERO_DEFAULT) {
			return new TiberoSQLSpatialDB(userDB);
		}
		
		return null;
	}

}
