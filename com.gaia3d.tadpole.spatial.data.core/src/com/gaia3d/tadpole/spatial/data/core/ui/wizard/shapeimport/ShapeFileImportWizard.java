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

import org.eclipse.jface.wizard.Wizard;

/**
 * Shape file import wizard
 * 
 * http://docs.geotools.org/stable/userguide/library/data/shape.html
 * https://en.wikipedia.org/wiki/Shapefile
 * 
 * 	1.1 Shapefile shape format (.shp)
 * 	1.2 Shapefile shape index format (.shx)
 * 	1.3 Shapefile attribute format (.dbf)
 * 	1.4 Shapefile spatial index format (.sbn)
 * 
 * @author hangum
 *
 */
public class ShapeFileImportWizard extends Wizard {

	private ShapeFileImportWizardPage uploadWizardPage;
	
	public ShapeFileImportWizard() {
		setWindowTitle("Shape file import wizard");
	}

	@Override
	public void addPages() {
		uploadWizardPage = new ShapeFileImportWizardPage();
		addPage(uploadWizardPage);
		
	}

	@Override
	public boolean performFinish() {
		return false;
	}

}
