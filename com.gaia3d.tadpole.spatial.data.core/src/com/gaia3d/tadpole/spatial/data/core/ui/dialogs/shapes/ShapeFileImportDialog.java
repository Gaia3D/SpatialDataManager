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
package com.gaia3d.tadpole.spatial.data.core.ui.dialogs.shapes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gaia3d.tadpole.spatial.data.core.ui.utils.ZIPUtil;
import com.gaia3d.tadpole.spatial.geotools.code.utils.GeoSpatialUtils;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.util.Utils;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.engine.sql.util.SQLUtil;
import com.hangum.tadpole.rdb.core.dialog.msg.TDBErroDialog;

/**
 * 
 * Shape file import dialog
 * 
 * @author hangum
 *
 */
public abstract class ShapeFileImportDialog extends Dialog {
	private static final Logger logger = Logger.getLogger(ShapeFileImportDialog.class);
	
	protected UserDBDAO userDB;
	protected static int intCommitCount = 200;
	
	private static final String INITIAL_TEXT = "No files uploaded."; //$NON-NLS-1$
	
	// file upload
	protected FileUpload fileUpload;
	protected DiskFileUploadReceiver receiver;
	protected ServerPushSession pushSession;
	
	protected Text fileNameLabel;
	protected Text textFileList;
	protected Text textTableName;
	protected Text textSRID;
	protected Text textSQL;
	protected Text textCharacterSet;
	
	/** list shape file */
	protected List<Map<String, Object>> listShape = new ArrayList<>();

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ShapeFileImportDialog(Shell parentShell, UserDBDAO userDB) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.RESIZE | SWT.TITLE);
		
		this.userDB = userDB;
	}
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Shape to DB Import Dialog"); //$NON-NLS-1$
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite containerMain = (Composite) super.createDialogArea(parent);
		
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
		textSRID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label lblCharacterSet = new Label(compositeTail, SWT.NONE);
		lblCharacterSet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCharacterSet.setText("Character Set");
		
		textCharacterSet = new Text(compositeTail, SWT.BORDER);
		textCharacterSet.setText("euc-kr");
		textCharacterSet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		return containerMain;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "OK", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "CANCEL", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 600);
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
				listShape.clear();
				
//				for( FileDetails file : event.getFileDetails() ) {
				FileDetails file = event.getFileDetails()[0];
				// 업로드 된 파일 목록을 나열한다.
				addToLog( "uploaded : " + file.getFileName() ); //$NON-NLS-1$
				
				// 업로드 된 shape 파일(zip)에 sph, dbf 파일이 있는지 검사한다.
				// 데이터베이스 용 디렉토리가 없으면 생성합니다.
				File[] arryFiles = receiver.getTargetFiles();
				if(arryFiles.length != 0) {
					final File userDBFile = arryFiles[arryFiles.length-1];
					
					final String strExtractionDir = userDBFile.getParent() + PublicTadpoleDefine.DIR_SEPARATOR + Utils.getUniqueID();
					
					if(logger.isDebugEnabled()) {
						logger.debug("\t source is " + userDBFile.getAbsolutePath());
						logger.debug("\t target is " + strExtractionDir);
					}

					
					getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
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
								
								String strShapeFile = "";
								String strDBFFile  = "";
								for (String strFile : strFiles) {
									if(logger.isDebugEnabled()) logger.debug("/t file is "+ strFile);
									sbFileList.append(strFile + PublicTadpoleDefine.LINE_SEPARATOR);
									if(StringUtils.endsWith(strFile, ".shp")) strShapeFile = strFile;
									else if(StringUtils.endsWith(strFile, ".dbf")) strDBFFile = strFile;
								}

								if(strShapeFile.equals("")) {
									MessageDialog.openError(getShell(), "Error", "Does not exist shape file.");
									return;
								} else if(strDBFFile.equals("")) {
									MessageDialog.openError(getShell(), "Error", "Does not exist Dbase file.");
									return;
								}
								
								textFileList.setText(sbFileList.toString());
								textTableName.setText(StringUtils.removeEnd(strShapeFile, ".shp"));
								
								try {
									listShape = GeoSpatialUtils.getShapeToList(strExtractionDir + "/" + strShapeFile);
									if(logger.isDebugEnabled()) logger.debug("\tShape file total line is " + listShape.size());
									String strCreateStatement = generateCreateStatement(textTableName.getText(), textSRID.getText());
									
									textSQL.setText(strCreateStatement);
								} catch (Exception e) {
									logger.error("Parse shape", e);
//									MessageDialog.openError(getShell(), "Error", "Shape file parse exception.\n" + e.getMessage());
									TDBErroDialog dialog = new TDBErroDialog(getShell(), "Error", "Shape file parse exception.\n" + e.getMessage());
									dialog.open();
								}
							} catch(final Exception e) {
								logger.error("upzip exception", e);
								
								getShell().getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										textFileList.setText("");
										textTableName.setText("");
										
//										MessageDialog.openError(getShell(), "Error", e.getMessage());
										TDBErroDialog dialog = new TDBErroDialog(getShell(), "Error", "Shape file parse exception.\n" + e.getMessage());
										dialog.open();
									}
								});
							}
						}
					});
				}	// if(arryFiles.length != 0) {
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
	protected abstract String generateCreateStatement(String strTableName, String strSRID);
	
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
		dto.setCreate_statement(SQLUtil.sqlExecutable(textSQL.getText()));
		dto.setListShape(listShape);
		
		return dto;
	}
}
