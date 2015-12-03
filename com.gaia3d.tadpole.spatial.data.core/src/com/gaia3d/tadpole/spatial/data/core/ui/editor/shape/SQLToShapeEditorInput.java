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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

/**
 * Shape export input
 * 
 * @author hangum
 *
 */
public class SQLToShapeEditorInput implements IEditorInput {
	private UserDBDAO userDB;
	
	public SQLToShapeEditorInput(UserDBDAO userDB) {
		this.userDB = userDB;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( !(obj instanceof SQLToShapeEditorInput) ) return false;
		
		return ((SQLToShapeEditorInput)obj).getName().equals(getName());
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}
	public UserDBDAO getUserDB() {
		return userDB;
	}

	@Override
	public String getName() {
		return getUserDB() + " shape exporter";
	}

	@Override
	public String getToolTipText() {
		return getUserDB() + " shape exporter";
	}
}
