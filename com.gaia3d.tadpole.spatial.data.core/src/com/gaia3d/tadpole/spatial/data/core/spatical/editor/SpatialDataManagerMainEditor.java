package com.gaia3d.tadpole.spatial.data.core.spatical.editor;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;

import com.gaia3d.tadpole.spatial.data.core.Activator;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.rdb.core.extensionpoint.definition.AMainEditorExtension;
import com.hangum.tadpole.sql.dao.system.UserDBDAO;

/**
 * Tadpole extension to spatial data manager 
 * 
 * @author hangum
 *
 */
public class SpatialDataManagerMainEditor extends AMainEditorExtension {
	private static final Logger logger = Logger.getLogger(SpatialDataManagerMainEditor.class);	
	private Browser browserMap;

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.verticalSpacing = 2;
		gl_composite.horizontalSpacing = 2;
		gl_composite.marginHeight = 2;
		gl_composite.marginWidth = 2;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeHead = new Composite(composite, SWT.NONE);
		compositeHead.setLayout(new GridLayout(1, false));
		compositeHead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMap = new Label(compositeHead, SWT.NONE);
		lblMap.setText("Leaflet Map");
		
		Composite compositeBody = new Composite(composite, SWT.NONE);
		compositeBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeBody.setLayout(new GridLayout(1, false));
		
		browserMap = new Browser(compositeBody, SWT.BORDER);
		browserMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		initUI();
	}
	
	@Override
	public void initExtension(UserDBDAO userDB) {
		super.initExtension(userDB);
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if(getEditorUserDB().getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
				conn = TadpoleSQLManager.getInstance(getEditorUserDB()).getDataSource().getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM geometry_columns");
				
				super.setEnableExtension(true);
			} else {
				super.setEnableExtension(false);	
			}
			
		} catch (Exception e1) {
			logger.error("GoogleMap extension" + e1);
	
			super.setEnableExtension(false);
		} finally {
			if(rs != null) try {rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}

	}
	
	public void initUI() {
		
		try {
			Bundle bundle = Activator.getDefault().getBundle();
			URL resource = bundle.getResource("/resources/map/LeafletMap.html");
			String strHtml = IOUtils.toString(resource.openStream());
			
			browserMap.setText(strHtml);
			
		} catch (Exception e) {
			logger.error("initialize map initialize error", e);
		}
	}

	@Override
	public void resultSetDoubleClick(int selectIndex, Map<Integer, Object> mapColumns) {
		if(logger.isDebugEnabled()) {
			logger.debug("=============================================================");
			logger.debug("Clieck column index is " + selectIndex );
			logger.debug("Clieck column data is " + mapColumns.get(selectIndex));
		}
		
		browserMap.evaluate(String.format("testAlertMsg('%s');", mapColumns.get(selectIndex)));
	}


}