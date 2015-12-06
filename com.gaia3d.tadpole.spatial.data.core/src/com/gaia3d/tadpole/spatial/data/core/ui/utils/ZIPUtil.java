/*******************************************************************************
 * Copyright (c) 2015 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.ui.utils;

import java.io.File;

import org.apache.log4j.Logger;
import org.zeroturnaround.zip.ZipUtil;

/**
 * zip util
 * 
 * @author hyunjongcho
 *
 */
public class ZIPUtil {
	private static final Logger logger = Logger.getLogger(ZIPUtil.class);
	
	/**
	 * upzip
	 * 
	 * @param strZIPFile
	 * @param strOutputDir
	 */
	public void unzip(String strZIPFile, String strOutputDir) throws Exception {
//		byte[] buffer = new byte[1024];
		
		if(logger.isDebugEnabled()) logger.debug("*** unzip start ***");
		
		ZipUtil.unpack(new File(strZIPFile), new File(strOutputDir));
		
//		try {
//			File fileOutput = new File(strOutputDir);
//			if(!fileOutput.exists()) fileOutput.mkdirs();
//			
//			ZipInputStream zipIs = new ZipInputStream(new FileInputStream(strZIPFile));
//			ZipEntry ze = zipIs.getNextEntry();
//			
//			while(ze != null) {
//				String strFileName = ze.getName();
//				File fileNew = new File(strOutputDir + PublicTadpoleDefine.DIR_SEPARATOR + strFileName);
//				if(logger.isDebugEnabled()) logger.debug("\tOutput File is " + fileNew.getAbsoluteFile());
//				
//				new File(fileNew.getParent()).mkdirs();
//				
//				FileOutputStream fos = new FileOutputStream(fileNew);
//				int len;
//				while((len = zipIs.read(buffer)) > 0) {
//					fos.write(buffer, 0, len);
//				}
//				
//				fos.close();
//				ze = zipIs.getNextEntry();
//			}
//			
//			zipIs.closeEntry();
//			zipIs.close();
//			if(logger.isDebugEnabled()) logger.debug("*** unzip complete ***");
//			
//		} catch(IOException ioe) {
//			logger.error("unzip exception", ioe);
//			
//			throw ioe;
//		}
	}
}
