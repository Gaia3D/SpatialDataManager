package com.gaia3d.tadpole.spatial.data.core.ui.editor.browserHandler;

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
		logger.debug("\t\t ==========>  SpatialFunctionService called" + arguments[0] +":" + arguments[1]);
		
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
	 * help popup
	 */
	protected void saveOptions(String userData) {
		try {
			SpatialGetPreferenceData.updateUserOptions(userData);
		} catch (Exception e) {
			logger.error("Spatial save options", e);
		}
	}
}
