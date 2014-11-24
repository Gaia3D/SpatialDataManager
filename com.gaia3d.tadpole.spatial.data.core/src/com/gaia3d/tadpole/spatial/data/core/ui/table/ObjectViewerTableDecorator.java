package com.gaia3d.tadpole.spatial.data.core.ui.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import com.gaia3d.tadpole.spatial.data.core.Activator;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.rdb.core.extensionpoint.definition.ITableDecorationExtension;
import com.hangum.tadpole.sql.dao.system.UserDBDAO;
import com.swtdesigner.ResourceManager;


/**
 * ObjectViewer의 Table decoration구현합니다. 
 * 
 * 
 * @author hangum
 *
 */
public class ObjectViewerTableDecorator implements ITableDecorationExtension {
	private static final Logger logger = Logger.getLogger(ObjectViewerTableDecorator.class);

	@Override
	public boolean initExtension(UserDBDAO userDB) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
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
				
				return true;
			}

		} catch (Exception e1) {
			logger.error("GoogleMap extension" + e1);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}
		
		return false;
	}

	@Override
	public Image getTableImage(String tableName) {
		if(mapColumnDescList.containsKey(tableName)) {
			return ResourceManager.getPluginImage(Activator.PLUGIN_ID, "resources/images/map-marker-16.png"); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public Image getColumnImage(String tableName, String columnName) {
		if(mapColumnDescList.containsKey(tableName)) {
			List<String> listColumn = mapColumnDescList.get(tableName);
			if(listColumn.contains(columnName)) {
				return ResourceManager.getPluginImage(Activator.PLUGIN_ID, "resources/images/map-marker-16.png"); //$NON-NLS-1$
			}
		}
		return null;
	}

}
