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
package com.gaia3d.tadpole.spatial.data.core.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.SpatiaDBFactory;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.gaia3d.tadpole.spatial.data.core.ui.dialogs.shapes.MssqlShapeFileImportDialog;
import com.gaia3d.tadpole.spatial.data.core.ui.dialogs.shapes.OracleShapeFileImportDialog;
import com.gaia3d.tadpole.spatial.data.core.ui.dialogs.shapes.PgsqlShapeFileImportDialog;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * Import shape file actionss
 * 
 * @author hangum
 *
 */
public class ImportShapeFileActions implements IViewActionDelegate {
	private static final Logger logger = Logger.getLogger(ImportShapeFileActions.class);
	protected IStructuredSelection sel;
	
	public ImportShapeFileActions() {
		super();
	}

	@Override
	public void run(IAction action) {
		final UserDBDAO userDB = (UserDBDAO)sel.getFirstElement();
		
		SpatiaDBFactory factory = new SpatiaDBFactory();
		SpatialDB spatialDB = factory.getSpatialDB(userDB);
		if(spatialDB == null || spatialDB.isSpatialDBImage() == null) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Shape file import", "This DB does not Spatial DB. so cat't import Shape file.");
			return;
		}
		
		if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT) {
			PgsqlShapeFileImportDialog dialog = new PgsqlShapeFileImportDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), userDB);
			dialog.open();
		} else if(userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT) {
			MssqlShapeFileImportDialog dialog = new MssqlShapeFileImportDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), userDB);
			dialog.open();
		} else if(userDB.getDBDefine() == DBDefine.ORACLE_DEFAULT) {
			OracleShapeFileImportDialog dialog = new OracleShapeFileImportDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), userDB);
			dialog.open();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		sel = (IStructuredSelection)selection;
	}

	@Override
	public void init(IViewPart view) {
	}


}
