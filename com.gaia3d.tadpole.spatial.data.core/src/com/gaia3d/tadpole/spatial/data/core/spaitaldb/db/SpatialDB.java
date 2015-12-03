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
package com.gaia3d.tadpole.spatial.data.core.spaitaldb.db;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.dao.RequestSpatialQueryDAO;

/**
 * spatial db
 *
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 27.
 *
 */
public interface SpatialDB {
	
	/**
	 * is spatial db
	 * if spatialdb return spatial icon
	 * 
	 * @return
	 */
	public Image isSpatialDBImage();

	/**
	 * this method is spatial table, column information.
	 *  
	 *  table name, columns
	 *  Map<String, List<String>>
	 *  
	 * 
	 * @return
	 */
	public Map<String, List<String>> getSpatialTableColumn();
	
	/**
	 * user query to change tadpole custom query
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	public RequestSpatialQueryDAO makeSpatialQuery(RequestSpatialQueryDAO dao);
		

	/**
	 * Result query to geojsonfeature
	 *
	 * @param query
	 * @return
	 */
	public String makeGeojsonFeature(String query) throws Exception;
}
