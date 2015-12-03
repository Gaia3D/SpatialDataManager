/*******************************************************************************
 * Copyright 2014 hangum
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
package com.gaia3d.tadpole.spatial.data.core.ui.editor.extension.browserHandler;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

import com.gaia3d.tadpole.spatial.data.core.ui.preference.data.SpatialGetPreferenceData;
import com.hangum.tadpole.ace.editor.core.texteditor.EditorExtension;

/**
 * Spatial browser function
 * 
 * @author hangum
 *
 */
public class SpatialFunctionService extends BrowserFunction {
	private static final Logger logger = Logger.getLogger(SpatialFunctionService.class);
	protected EditorExtension editor;

	public SpatialFunctionService(Browser browser, String name) {
		super(browser, name);
	}
	
	@Override
	public Object function(Object[] arguments) {
		
		int intActionId =  NumberUtils.toInt(arguments[0].toString());
		if(logger.isDebugEnabled()) logger.debug("\t\t ==========>  SpatialFunctionService called" + arguments[0] +":" + arguments[1]);
		
		switch (intActionId) {
			case SpatialEditorFunction.SAVE_OPTIONS:
				saveOptions((String) arguments[1]);
				break;
				
			default:
				return null;
		}
		
		return null;
	}
	
	/**
	 * save options
	 */
	protected void saveOptions(String userData) {
		try {
			SpatialGetPreferenceData.updateUserOptions(userData);
		} catch (Exception e) {
			logger.error("Spatial save options", e);
		}
	}
}
