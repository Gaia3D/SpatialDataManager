package com.gaia3d.tadpole.spatial.data.core.ui.preference.ui;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.gaia3d.tadpole.spatial.data.core.ui.preference.data.SpatialGetPreferenceData;
import com.gaia3d.tadpole.spatial.data.core.ui.preference.data.SpatialPreferenceDefine;
import com.hangum.tadpole.session.manager.SessionManager;

/**
 * Spatial Data manager preference
 * 
 * @author hangum
 *
 */
public class SpatialDataManagerPreference extends PreferencePage implements IWorkbenchPreferencePage {
	private static final Logger logger = Logger.getLogger(SpatialDataManagerPreference.class);
	private Text textSendMapDataCount;

	public SpatialDataManagerPreference() {
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Send map data count");
		
		textSendMapDataCount = new Text(container, SWT.BORDER);
		textSendMapDataCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		initDefaultValue();
		
		return container;
	}
	
	@Override
	public boolean performOk() {
		String txtSendMapDataCount = textSendMapDataCount.getText();
		
		if(!NumberUtils.isNumber(txtSendMapDataCount)) {
			MessageDialog.openError(getShell(), "Confirm", "Send map data count is must be number.");			 //$NON-NLS-1$
			textSendMapDataCount.setFocus();
			return false;
		}
		
		// 테이블에 저장 
		try {
			SpatialGetPreferenceData.updateSendMapDataCount(txtSendMapDataCount);
			
			// session 데이터를 수정한다.
			SessionManager.setUserInfo(SpatialPreferenceDefine.SPATIAL_SEND_MAP_DATA_COUNT, txtSendMapDataCount);
		} catch(Exception e) {
			logger.error("Spatial Preference saveing", e);
			
			MessageDialog.openError(getShell(), "Confirm", String.format("An error occure %s.", e.getMessage())); //$NON-NLS-1$
			return false;
		}
		
		return super.performOk();
	}
	
	@Override
	public boolean performCancel() {
		initDefaultValue();
		
		return super.performCancel();
	}
	
	@Override
	protected void performApply() {
		super.performApply();
	}
	
	@Override
	protected void performDefaults() {
		initDefaultValue();

		super.performDefaults();
	}
	
	/**
	 * initial default preference value
	 * 
	 */
	private void initDefaultValue() {
		textSendMapDataCount.setText( "" + SpatialGetPreferenceData.getSendMapDataCount() ); //$NON-NLS-1$
	}

}
