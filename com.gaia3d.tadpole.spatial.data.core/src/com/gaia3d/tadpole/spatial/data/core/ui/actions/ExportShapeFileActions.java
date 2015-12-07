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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.gaia3d.tadpole.spatial.data.core.Activator;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.SpatiaDBFactory;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.gaia3d.tadpole.spatial.data.core.ui.editor.shape.SQLToShapeEditor;
import com.gaia3d.tadpole.spatial.data.core.ui.editor.shape.SQLToShapeEditorInput;
import com.hangum.tadpole.commons.exception.dialog.ExceptionDetailsErrorDialog;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * Export shape file actionss
 * 
 * @author hangum
 *
 */
public class ExportShapeFileActions implements IViewActionDelegate {
	private static final Logger logger = Logger.getLogger(ExportShapeFileActions.class);
	protected IStructuredSelection sel;
	
	public ExportShapeFileActions() {
		super();
	}

	@Override
	public void run(IAction action) {
		final UserDBDAO userDB = (UserDBDAO)sel.getFirstElement();
		
		SpatiaDBFactory factory = new SpatiaDBFactory();
		SpatialDB spatialDB = factory.getSpatialDB(userDB);
		if(spatialDB == null || spatialDB.isSpatialDBImage() == null) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Shape file export", "This DB is not Spatial DB. So cat't export shape file.");
			return;
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();		
		try {
			SQLToShapeEditorInput input = new SQLToShapeEditorInput(userDB);
			page.openEditor(input, SQLToShapeEditor.ID, false);				
		} catch (PartInitException e) {
			logger.error("shape export opend", e);
			Status errStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); //$NON-NLS-1$
			ExceptionDetailsErrorDialog.openError(null, "Error", "Shape export", errStatus); //$NON-NLS-1$
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
