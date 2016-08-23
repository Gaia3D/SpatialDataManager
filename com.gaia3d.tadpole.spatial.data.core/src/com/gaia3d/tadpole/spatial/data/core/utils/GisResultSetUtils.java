package com.gaia3d.tadpole.spatial.data.core.utils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * GIS result set utils
 * 
 * @author hangum
 *
 */
public class GisResultSetUtils {
	private static final Logger logger = Logger.getLogger(GisResultSetUtils.class);
	
	/**
	 * 쿼리결과의 실제 테이블 컬럼 정보를 넘겨 받습니다.
	 * 현재는 pgsql 만 지원합니다.
	 * 
	 * mysql, maria, oracle의 경우는 테이블 alias가 붙은 경우 이름을 처리하지 못합니다.
	 * 다른 디비는 테스트 해봐야합니다.
	 * 2014-11-13 
	 * 
	 * @param rsm
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, Map> getColumnTableColumnName(UserDBDAO userDB, ResultSetMetaData rsm) {
		Map<Integer, Map> mapTableColumn = new HashMap<Integer, Map>();
		
		// 첫번째 컬럼 순번을 위해 삽입.
		mapTableColumn.put(0, new HashMap());
			
		try {
//			if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
//				Jdbc4ResultSetMetaData pgsqlMeta = (Jdbc4ResultSetMetaData)rsm;
//				for(int i=0;i<rsm.getColumnCount(); i++) {
//					int columnSeq = i+1;
//					Map<String, String> metaData = new HashMap<String, String>();
//					metaData.put("schema", pgsqlMeta.getBaseSchemaName(columnSeq));
//					metaData.put("table", pgsqlMeta.getBaseTableName(columnSeq));
//					metaData.put("column", pgsqlMeta.getBaseColumnName(columnSeq));
//					metaData.put("type", 	""+rsm.getColumnType(columnSeq));
//					metaData.put("typeName", 	""+rsm.getColumnTypeName(columnSeq));
//					
//					if(logger.isDebugEnabled()) {
//						logger.debug("\tschema :" + pgsqlMeta.getBaseSchemaName(columnSeq) + "\ttable:" + pgsqlMeta.getBaseTableName(columnSeq) + "\tcolumn:" + pgsqlMeta.getBaseColumnName(columnSeq));
//					}
//					
//					mapTableColumn.put(i+1, metaData);
//				}
//				
//			} else if(userDB.getDBDefine() == DBDefine.MSSQL_8_LE_DEFAULT 
//						|| userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT
//						|| userDB.getDBDefine() == DBDefine.ORACLE_DEFAULT							
//				) {
				for(int i=0;i<rsm.getColumnCount(); i++) {
					int columnSeq = i+1;
					Map<String, String> metaData = new HashMap<String, String>();
					metaData.put("schema", 	rsm.getSchemaName(columnSeq));
					metaData.put("table", 	rsm.getTableName(columnSeq));
					metaData.put("column", 	rsm.getColumnName(columnSeq));
					metaData.put("type", 	""+rsm.getColumnType(columnSeq));
					metaData.put("typeName", 	""+rsm.getColumnTypeName(columnSeq));
					
					if(logger.isDebugEnabled()) {
						logger.debug("\tschema :" + rsm.getSchemaName(columnSeq) + "\ttable:" + rsm.getTableName(columnSeq) + "\tcolumn:" + rsm.getColumnName(columnSeq)
						 + "\ttype : " + rsm.getColumnType(columnSeq) + "\ttypename : " + rsm.getColumnTypeName(columnSeq));
					}
					
					mapTableColumn.put(i+1, metaData);
				}
//			}
		} catch(Exception e) {
			logger.error("resultset metadata exception", e);
		}
		
		return mapTableColumn;
	}
}
