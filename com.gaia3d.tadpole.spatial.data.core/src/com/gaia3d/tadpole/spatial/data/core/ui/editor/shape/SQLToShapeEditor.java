/*******************************************************************************
 * Copyright 2015 hangum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.ui.editor.shape;

import java.io.FileWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.SpatiaDBFactory;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.gaia3d.tadpole.spatial.geotools.code.utils.GeoSpatialUtils;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * SQL to shape exporter
 * 
 * @author hangum
 *
 */
public class SQLToShapeEditor extends EditorPart {
	private static final Logger logger = Logger.getLogger(SQLToShapeEditor.class);
	public static final String ID = "com.gaia3d.tadpole.spatial.data.core.editor.shapeExport";
	private UserDBDAO userDB;
	private Text textSQL;

	public SQLToShapeEditor() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		SQLToShapeEditorInput qei = (SQLToShapeEditorInput)input;
		userDB = qei.getUserDB();

		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite compositeHead = new Composite(parent, SWT.NONE);
		compositeHead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeHead.setLayout(new GridLayout(2, false));
		
		Label lblDatabaseName = new Label(compositeHead, SWT.NONE);
		lblDatabaseName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatabaseName.setText("Database Name");
		
		Text textDBName = new Text(compositeHead, SWT.BORDER);
		textDBName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textDBName.setText(userDB.getDb());
		
		Group grpSql = new Group(parent, SWT.NONE);
		grpSql.setLayout(new GridLayout(1, false));
		grpSql.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpSql.setText("SQL");
		
		textSQL = new Text(grpSql, SWT.BORDER | SWT.MULTI);
		textSQL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeTail = new Composite(parent, SWT.NONE);
		compositeTail.setLayout(new GridLayout(1, false));
		compositeTail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnExportShape = new Button(compositeTail, SWT.NONE);
		btnExportShape.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportShape();
			}
		});
		btnExportShape.setText("Export Shape");
	}
	
	/** 
	 * export shape 
	 */
	private void exportShape() {
		String strQuery = textSQL.getText();
		
		// gem 컬럼이 있는지 검사합니다. 
		SpatiaDBFactory factory = new SpatiaDBFactory();
		SpatialDB spatialDB = factory.getSpatialDB(userDB);
		try {
			String strGeojsonFeature = StringEscapeUtils.unescapeJava(spatialDB.makeGeojsonFeature(strQuery));
			String root = "/Users/hangum/Downloads/example_shape_file/test/";
			String fileName = "FeatureCollection.json";
			FileWriter fw = new FileWriter(root+fileName);
			fw.write(strGeojsonFeature);
			fw.flush();
			
			GeoSpatialUtils.toShp(root+fileName, root+fileName+".shp");
			
			logger.debug("==[start]=======================================================================================================================");
			logger.debug(strGeojsonFeature);
			logger.debug("==[end]=======================================================================================================================");
			GeoSpatialUtils.getShapeToList(root+fileName+".shp");
			logger.debug("==[end]=======================================================================================================================");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus() {
		textSQL.setFocus();
	}

}
