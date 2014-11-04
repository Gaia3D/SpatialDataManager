package com.gaia3d.tadpole.spatial.data.core.spatical.connection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import com.gaia3d.tadpole.spatial.data.core.Activator;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.rdb.core.extensionpoint.definition.IConnectionDecoration;
import com.hangum.tadpole.sql.dao.system.UserDBDAO;
import com.swtdesigner.ResourceManager;

/**
 * connection viewer decorator
 * 
 * @author hangum
 *
 */
public class ConnectionViewerDecorator implements IConnectionDecoration {
	private static final Logger logger = Logger.getLogger(ConnectionViewerDecorator.class);
	
	@Override
	public Image getImage(UserDBDAO userDB) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
				conn = TadpoleSQLManager.getInstance(userDB).getDataSource().getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM geometry_columns");
				
				return ResourceManager.getPluginImage(Activator.PLUGIN_ID, "resources/images/map-marker-16.png"); //$NON-NLS-1$
			}
		} catch (Exception e1) {
			logger.error("connection viewer decoration extension" + e1.getMessage());
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}
		
		return null;
	}
}
