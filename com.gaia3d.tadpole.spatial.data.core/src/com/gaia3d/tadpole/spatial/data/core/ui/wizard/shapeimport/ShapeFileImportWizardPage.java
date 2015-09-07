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
package com.gaia3d.tadpole.spatial.data.core.ui.wizard.shapeimport;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rap.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.fileupload.FileDetails;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.gaia3d.tadpole.spatial.data.core.ui.utils.ZIPUtil;
import com.gaia3d.tadpole.spatial.geotools.code.utils.GeoSpatialUtils;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.util.Utils;
import org.eclipse.swt.widgets.Button;

/**
 * Shape file import wizard
 * 
 * upload shape zip file 
 * 	1.1 Shapefile shape format (.shp)
 * 	1.2 Shapefile shape index format (.shx)
 * 	1.3 Shapefile attribute format (.dbf)
 * 	1.4 Shapefile spatial index format (.sbn)
 * 
 * @author hangum
 *
 */
public class ShapeFileImportWizardPage extends WizardPage {
	private static final Logger logger = Logger.getLogger(ShapeFileImportWizardPage.class);

	private String CREATE_STATEMENT_HEAD = 
		" CREATE TABLE %s(			\n" +  
		"	gid serial			\n";
	
	private String CREATE_STATEMENT_GEO = 
		"	,%s geometry(%s,%s) \n";
	
	private String CREATE_STATEMENT_NORMAL =
		"	,%s text		\n";
					
	private String CREATE_STATEMENT_TAIL =
		"	,CONSTRAINT %s_pkey PRIMARY KEY (gid) \n" +  
		");";
	
	private static final String INITIAL_TEXT = "No files uploaded."; //$NON-NLS-1$
	
	// file upload
	private FileUpload fileUpload;
	private DiskFileUploadReceiver receiver;
	private ServerPushSession pushSession;
	
	private Text fileNameLabel;
	private Text textFileList;
	private Text textTableName;
	private Text textSRID;
	private Text textSQL;

	/** list shape file */
	private List<Map<String, Object>> listShape = null;
	
	/**
	 * Create the wizard.
	 */
	public ShapeFileImportWizardPage() {
		super("Import shape file");
		setTitle("Shape to DB import wizard");
		setDescription("Upload ZIP file.(Must be include *.shp, *.dbf)");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite containerMain = new Composite(parent, SWT.NULL);
		containerMain.setLayout(new GridLayout(1, false));
		
		Composite compositeHead = new Composite(containerMain, SWT.NONE);
		compositeHead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeHead.setLayout(new GridLayout(3, false));
		
		Label lblFileName = new Label(compositeHead, SWT.NONE);
		lblFileName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFileName.setText("File name");
		
		fileNameLabel = new Text(compositeHead, SWT.BORDER);
		fileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		fileNameLabel.setEditable(false);
		
		final String url = startUploadReceiver();
		pushSession = new ServerPushSession();
		
		fileUpload = new FileUpload(compositeHead, SWT.NONE);
		fileUpload.setText("Select File");

		fileUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fileUpload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(false);
				String fileName = fileUpload.getFileName();
				if("".equals(fileName) || null == fileName) return; //$NON-NLS-1$
			
				fileNameLabel.setText(fileName == null ? "" : fileName); //$NON-NLS-1$
				
				pushSession.start();
				fileUpload.submit(url);
			}
		});
		
		Group grpFileInformation = new Group(containerMain, SWT.NONE);
		grpFileInformation.setLayout(new GridLayout(1, false));
		grpFileInformation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFileInformation.setText("File Information");
		
		textFileList = new Text(grpFileInformation, SWT.BORDER | SWT.H_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_textFileList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_textFileList.heightHint = 80;
		gd_textFileList.minimumHeight = 100;
		textFileList.setLayoutData(gd_textFileList);
		
		Group grpSql = new Group(containerMain, SWT.NONE);
		grpSql.setLayout(new GridLayout(1, false));
		grpSql.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpSql.setText("SQL");
		
		textSQL = new Text(grpSql, SWT.BORDER | SWT.MULTI);
		textSQL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeTail = new Composite(containerMain, SWT.NONE);
		compositeTail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeTail.setLayout(new GridLayout(3, false));
		
		Label lblTableName = new Label(compositeTail, SWT.NONE);
		lblTableName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTableName.setText("Table Name");
		
		textTableName = new Text(compositeTail, SWT.BORDER);
		textTableName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnRename = new Button(compositeTail, SWT.NONE);
		btnRename.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String strSQL = generateCreateStatement(textTableName.getText(), textSRID.getText());
				textSQL.setText(strSQL);
			}
		});
		btnRename.setText("Rename");
		
		Label lblSetSrid = new Label(compositeTail, SWT.NONE);
		lblSetSrid.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSetSrid.setText("Set SRID");
		
		textSRID = new Text(compositeTail, SWT.BORDER);
		textSRID.setText("4326");
		textSRID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		setControl(containerMain);
		setPageComplete(false);
	}
	
	/**
	 * 저장 이벤트 
	 * 
	 * @return
	 */
	private String startUploadReceiver() {
		receiver = new DiskFileUploadReceiver();
		final FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
		uploadHandler.addUploadListener(new FileUploadListener() {

			public void uploadProgress(FileUploadEvent event) {
			}

			public void uploadFailed(FileUploadEvent event) {
				addToLog( "upload failed: " + event.getException() ); //$NON-NLS-1$
			}

			public void uploadFinished(FileUploadEvent event) {
				
				for( FileDetails file : event.getFileDetails() ) {
					// 업로드 된 파일 목록을 나열한다.
					addToLog( "uploaded : " + file.getFileName() ); //$NON-NLS-1$
					
					// 업로드 된 shape 파일(zip)에 sph, dbf 파일이 있는지 검사한다.
					// 데이터베이스 용 디렉토리가 없으면 생성합니다.
					File[] arryFiles = receiver.getTargetFiles();
					if(arryFiles.length != 0) {
						File userDBFile = arryFiles[arryFiles.length-1];
						
						final String strExtractionDir = userDBFile.getParent() + PublicTadpoleDefine.DIR_SEPARATOR + Utils.getUniqueID();
						
						if(logger.isDebugEnabled()) {
							logger.debug("\t source is " + userDBFile.getAbsolutePath());
							logger.debug("\t target is " + strExtractionDir);
						}

						try {
							ZIPUtil unzipUtil = new ZIPUtil();
							unzipUtil.unzip(userDBFile.getAbsolutePath(), strExtractionDir);
							
							String[] listFile = new File(strExtractionDir).list();
							// 파일 갯수가 하나이고 디렉토리이면 최상위 루트로 생각하고 하위 디렉토리에서 값을 찾습니다.
							String strWorkingDir = "";
							if(listFile.length == 1) {
								strWorkingDir = strExtractionDir + PublicTadpoleDefine.DIR_SEPARATOR + listFile[0];
							} else {
								strWorkingDir = strExtractionDir;
							}
							
							if(logger.isDebugEnabled()) logger.debug("extension folder : " + strWorkingDir);
							final String[] strFiles = new File(strWorkingDir).list();
							final StringBuffer sbFileList = new StringBuffer();
							
							String strTmpShapeFile = "";
							String strTmpDBFFile  = "";
							for (String strFile : strFiles) {
								if(logger.isDebugEnabled()) logger.debug("/t file is "+ strFile);
								sbFileList.append(strFile + PublicTadpoleDefine.LINE_SEPARATOR);
								if(StringUtils.endsWith(strFile, ".shp")) strTmpShapeFile = strFile;
								else if(StringUtils.endsWith(strFile, ".dbf")) strTmpDBFFile = strFile;
							}
							final String strShapeFile = strTmpShapeFile;
							final String strDBFile = strTmpDBFFile;
							
							getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									if(strShapeFile.equals("")) {
										MessageDialog.openError(getShell(), "Error", "Does not exist shape file.");
										return;
									} else if(strDBFile.equals("")) {
										MessageDialog.openError(getShell(), "Error", "Does not exist Dbase file.");
										return;
									}
									
									textFileList.setText(sbFileList.toString());
									textTableName.setText(StringUtils.removeEnd(strShapeFile, ".shp"));
									
									try {
										listShape = GeoSpatialUtils.getShapeToList(strExtractionDir + "/" + strShapeFile);
										logger.debug("\tShape file total line is " + listShape.size());
										String strCreateStatement = generateCreateStatement(textTableName.getText(), textSRID.getText());
										
										textSQL.setText(strCreateStatement);
										
										setPageComplete(true);
									} catch (Exception e) {
										logger.error("Parse shape", e);
										MessageDialog.openError(getShell(), "Error", "Shape file parse exception.\n" + e.getMessage());
									}
									
								}
							});
							
						} catch(final Exception e) {
							logger.error("upzip exception", e);
							
							getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									textFileList.setText("");
									textTableName.setText("");
									
									MessageDialog.openError(getShell(), "Error", e.getMessage());
								}
							});
						}
					}
					
				}
			}			
		});
		
		return uploadHandler.getUploadUrl();
	}
	
	/**
	 * generate create statement
	 * 
	 * @param strTableName
	 * @param strSRID
	 * @return
	 */
	private String generateCreateStatement(String strTableName, String strSRID) {
		if(listShape.isEmpty()) return "";
	
		StringBuffer sbSQL = new StringBuffer(String.format(CREATE_STATEMENT_HEAD, strTableName));
		
		Map<String, Object> mapShape = listShape.get(0);
		for(String strKey : mapShape.keySet()) {
			Object obj = mapShape.get(strKey);
			
			if(StringUtils.startsWith(obj.getClass().getName(), "com.vividsolutions.jts.geom")) {
				String strColumnData =""+ mapShape.get(strKey);
				String strGeoType = StringUtils.substringBefore(strColumnData, "(");
				
				sbSQL.append(String.format(CREATE_STATEMENT_GEO, strKey, strGeoType, strSRID));
			} else {
				sbSQL.append(String.format(CREATE_STATEMENT_NORMAL, strKey));
			}
		}
		
		sbSQL.append(String.format(CREATE_STATEMENT_TAIL, strTableName));
		
		return sbSQL.toString();
	}
	
	/**
	 * add log file
	 * 
	 * @param message
	 */
	private void addToLog(final String message) {
		if (!fileNameLabel.isDisposed()) {
			fileNameLabel.getDisplay().asyncExec(new Runnable() {
				public void run() {
					String text = fileNameLabel.getText();
					if (INITIAL_TEXT.equals(text)) {
						text = ""; //$NON-NLS-1$
					}
					fileNameLabel.setText(message);

					pushSession.stop();
				}
			});
		}
	}
	
	/**
	 * work dto
	 * 
	 * @return
	 */
	public ShapeImportDTO getDTO() {
		ShapeImportDTO dto = new ShapeImportDTO();
		dto.setTableName(textTableName.getText());
		dto.setSrid(textSRID.getText());
		dto.setCreate_statement(textSQL.getText());
		dto.setListShape(listShape);
		
		return dto;
	}
		
}
