package com.gaia3d.tadpole.spatial.data.core.spaitaldb.utils;

import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * sdm utils
 * 
 * @author hangum
 *
 */
public class SDMUtiils {

	/**
	 * is spatial column
	 * 
	 * @param userDB
	 * @param strSearchType
	 * @return
	 */
	public static boolean isSpitailColumn(UserDBDAO userDB, String strSearchType) {
		if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT & strSearchType.equals("1111")) {
			return true;
		} else if(userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT & strSearchType.equals("2004")) {
			return true;
		} else if(userDB.getDBDefine() == DBDefine.ORACLE_DEFAULT & strSearchType.equals("2002")) {
			return true;
		}
		
		return false;
	}
			 
}
