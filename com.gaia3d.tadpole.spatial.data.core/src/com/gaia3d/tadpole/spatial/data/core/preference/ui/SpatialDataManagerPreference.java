package com.gaia3d.tadpole.spatial.data.core.preference.ui;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.gaia3d.tadpole.spatial.data.core.preference.data.SpatialGetPreferenceData;
import com.gaia3d.tadpole.spatial.data.core.preference.data.SpatialPreferenceDefine;
import com.hangum.tadpole.commons.util.ColorUtils;
import com.hangum.tadpole.session.manager.SessionManager;
import com.swtdesigner.SWTResourceManager;

/**
 * Spatial Data manager preference
 * 
 * @author hangum
 *
 */
public class SpatialDataManagerPreference extends PreferencePage implements IWorkbenchPreferencePage {
	private static final Logger logger = Logger.getLogger(SpatialDataManagerPreference.class);
	private Text textSendMapDataCount;
	private Label lblColor;

	/**
	 * @wbp.parser.constructor
	 */
	public SpatialDataManagerPreference() {
		// TODO Auto-generated constructor stub
	}

//	public SpatialDataManagerPreference(String title) {
//		super(title);
//		// TODO Auto-generated constructor stub
//	}
//
//	public SpatialDataManagerPreference(String title, ImageDescriptor image) {
//		super(title, image);
//		// TODO Auto-generated constructor stub
//	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Send map data count");
		
		textSendMapDataCount = new Text(container, SWT.BORDER);
		textSendMapDataCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label lblMapBoundColor = new Label(container, SWT.NONE);
		lblMapBoundColor.setText("Map bound color");
		
		lblColor = new Label(container, SWT.NONE);
		GridData gd_lblNewLabel_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_1.widthHint = 20;
		gd_lblNewLabel_1.minimumWidth = 20;
		lblColor.setLayoutData(gd_lblNewLabel_1);
		
		Button btnSelectColor = new Button(container, SWT.NONE);
		btnSelectColor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog dialog = new ColorDialog(getShell());
				RGB rgbColor = dialog.open();
				if(rgbColor != null) {
					lblColor.setBackground(SWTResourceManager.getColor(rgbColor));
				}
			}
		});
		btnSelectColor.setText("Select Color");
		
		initDefaultValue();
		
		return container;
	}
	
	@Override
	public boolean performOk() {
		String txtSendMapDataCount = textSendMapDataCount.getText();
		Color rgbColor = lblColor.getBackground();//ColorUtils.hexaToRGB(SpatialGetPreferenceData.getUserClickedColor());
		String txtHexColor = ColorUtils.rgbToHexa(rgbColor.getRGB());
		
		if(!NumberUtils.isNumber(txtSendMapDataCount)) {
			MessageDialog.openError(getShell(), "Confirm", "Send map data count is must be number.");			 //$NON-NLS-1$
			textSendMapDataCount.setFocus();
			return false;
		}
		
		
		// 테이블에 저장 
		try {
			SpatialGetPreferenceData.updatePreferenceData(txtSendMapDataCount, txtHexColor);
			
			// session 데이터를 수정한다.
			SessionManager.setUserInfo(SpatialPreferenceDefine.SEND_MAP_DATA_COUNT, txtSendMapDataCount);
			SessionManager.setUserInfo(SpatialPreferenceDefine.USER_CLICKED_COLOR, txtHexColor);
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
		
		RGB rgbColor = ColorUtils.hexaToRGB(SpatialGetPreferenceData.getUserClickedColor());
		lblColor.setBackground(SWTResourceManager.getColor(rgbColor));
	}

}
