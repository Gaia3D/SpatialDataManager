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

import org.apache.log4j.Logger;
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
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.util.Utils;

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
	
	private static final String INITIAL_TEXT = "No files uploaded."; //$NON-NLS-1$
	
	// file upload
	private FileUpload fileUpload;
	private DiskFileUploadReceiver receiver;
	private ServerPushSession pushSession;
	
	private Text fileNameLabel;
	private Text textFileList;

	/**
	 * Create the wizard.
	 */
	public ShapeFileImportWizardPage() {
		super("Import shape file");
		setTitle("Upload shape file wizard page");
		setDescription("Must include shape file(*.shp, *.shx, *.dbf)");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite containerMain = new Composite(parent, SWT.NULL);

		setControl(containerMain);
		containerMain.setLayout(new GridLayout(1, false));
		
		Composite compositeHead = new Composite(containerMain, SWT.NONE);
		compositeHead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeHead.setLayout(new GridLayout(3, false));
		
		Label lblFileName = new Label(compositeHead, SWT.NONE);
		lblFileName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFileName.setText("file name");
		
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
		grpFileInformation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpFileInformation.setText("File Information");
		
		textFileList = new Text(grpFileInformation, SWT.BORDER | SWT.H_SCROLL | SWT.CANCEL | SWT.MULTI);
		textFileList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
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
						
						String strExtractionDir = userDBFile.getParent() + PublicTadpoleDefine.DIR_SEPARATOR + Utils.getUniqueID();
						
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
							String[] strFiles = new File(strWorkingDir).list();
							final StringBuffer sbFileList = new StringBuffer();
							for (String string : strFiles) {
								sbFileList.append(string + PublicTadpoleDefine.LINE_SEPARATOR);
							}
							
							getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									textFileList.setText(sbFileList.toString());		
								}
							});
							
							
						} catch(Exception e) {
							logger.error("upzip exception", e);
						}
					}
					
				}
			}			
		});
		
		return uploadHandler.getUploadUrl();
	}
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
		
}
