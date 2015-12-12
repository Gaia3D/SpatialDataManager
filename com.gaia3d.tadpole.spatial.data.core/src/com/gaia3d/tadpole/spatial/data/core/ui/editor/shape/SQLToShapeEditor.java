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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.gaia3d.tadpole.spatial.data.core.Activator;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.SpatiaDBFactory;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.gaia3d.tadpole.spatial.geotools.code.utils.GeoSpatialUtils;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.util.download.DownloadServiceHandler;
import com.hangum.tadpole.commons.util.download.DownloadUtils;
import com.hangum.tadpole.commons.utils.zip.util.ZipUtils;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.engine.sql.util.SQLUtil;

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
	private DownloadServiceHandler downloadServiceHandler;
	
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
		
		registerServiceHandler();
	}
	
	/** 
	 * export shape 
	 */
	private void exportShape() {
		final String strQuery =  SQLUtil.sqlExecutable(textSQL.getText());
		if("".equals(StringUtils.trimToEmpty(strQuery))) {
			MessageDialog.openError(null, "Error", "Please input query.");
			return;
		}
		
		if(!MessageDialog.openConfirm(getSite().getShell(), "Confirm", "Do you want to shapefile export?")) return;
		
		final String root 	= PublicTadpoleDefine.TEMP_DIR + System.currentTimeMillis() + PublicTadpoleDefine.DIR_SEPARATOR;
		final String geojsonFileName = "SDMShapeFileJson";
		
		final Display display = getSite().getShell().getDisplay();
		Job job = new Job("Export shapefile") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Export shapefile", IProgressMonitor.UNKNOWN);
				
				try {

					monitor.setTaskName("Initialize Shape factory");
					// gem 컬럼이 있는지 검사합니다. 
					SpatiaDBFactory factory = new SpatiaDBFactory();
					SpatialDB spatialDB = factory.getSpatialDB(userDB);
					monitor.setTaskName("Execute user query");
					String strGeojsonFeature = StringEscapeUtils.unescapeJava(spatialDB.makeGeojsonFeature(strQuery));
					monitor.setTaskName("Make query result");
					
					new File(root).mkdirs();
					
//					FileWriter fw = new FileWriter(root+geojsonFileName);
//					fw.write(strGeojsonFeature);
//					fw.flush();
//					strGeojsonFeature = null;
//					if(logger.isDebugEnabled()) logger.debug("geojson localtion : " + root + geojsonFileName);
					monitor.setTaskName("Make shapefile");
					boolean boolExport = GeoSpatialUtils.toShp(strGeojsonFeature, root+geojsonFileName+".shp");
					strGeojsonFeature = null;
					if(boolExport) logger.debug("======[export success]=====================================================");
					else {
						logger.info("======[export fail]=====================================================");
						throw new Exception("Shape file does not export");
					}
					logger.debug("shape localtion : " + root+geojsonFileName+".shp");
					monitor.setTaskName("Make zipfile and download");
					downloadFile(root);
					
//					FileUtils.deleteDirectory(new File(root));

				} catch(Exception e) {
					logger.error("Shape exporter", e); //$NON-NLS-1$
					
					return new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		
		// job의 event를 처리해 줍니다.
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				final IJobChangeEvent jobEvent = event; 
				
				final Display display = getSite().getShell().getDisplay();
				display.asyncExec(new Runnable() {
					public void run() {
						if(jobEvent.getResult().isOK()) {
							if(logger.isDebugEnabled()) logger.debug("Success export shape file.  Checked your directory.");
							MessageDialog.openInformation(display.getActiveShell(), "OK", "Success export shape file.  Checked your directory.");
						} else {
							logger.error(String.format("File export shape file.  reason [%s].", jobEvent.getResult().getMessage()));
							MessageDialog.openError(display.getActiveShell(), "Fail", jobEvent.getResult().getMessage());
						}
					}
				});	// end display.asyncExec
			}	// end done
			
		});	// end job
		
		job.setName("Database compare");
		job.setUser(true);
		job.schedule();
	}
	
	/**
	 * download file
	 * @param strFileLocation
	 * @throws Exception
	 */
	private void downloadFile(String strFileLocation) throws Exception {
		String strZipFile = ZipUtils.pack(strFileLocation);
		byte[] bytesZip = FileUtils.readFileToByteArray(new File(strZipFile));
		
		downloadExtFile("SDMShape.zip", bytesZip); //$NON-NLS-1$
	}
	
	/** registery service handler */
	private void registerServiceHandler() {
		downloadServiceHandler = new DownloadServiceHandler();
		RWT.getServiceManager().registerServiceHandler(downloadServiceHandler.getId(), downloadServiceHandler);
	}
	
	/** download service handler call */
	private void unregisterServiceHandler() {
		RWT.getServiceManager().unregisterServiceHandler(downloadServiceHandler.getId());
		downloadServiceHandler = null;
	}
	
	/**
	 * download external file
	 * 
	 * @param fileName
	 * @param newContents
	 */
	private void downloadExtFile(String fileName, byte[] newContents) {
		downloadServiceHandler.setName(fileName);
		downloadServiceHandler.setByteContent(newContents);
		
		final Display display = getSite().getShell().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				DownloadUtils.provideDownload(getSite().getShell(), downloadServiceHandler.getId());
			}
		});
	}
	
	@Override
	public void dispose() {
		unregisterServiceHandler();
		super.dispose();
	}
	

	@Override
	public void setFocus() {
		textSQL.setFocus();
	}

}
