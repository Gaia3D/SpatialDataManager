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
package com.gaia3d.tadpole.spatial.data.core.ui.table;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.SpatiaDBFactory;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.gaia3d.tadpole.spatial.data.core.ui.utils.SpatialUtils;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.extensionpoint.definition.ITableDecorationExtension;


/**
 * ObjectViewer의 Table decoration 구현합니다. 
 * 
 * @author hangum
 *
 */
public class ObjectViewerTableDecorator implements ITableDecorationExtension {
	private static final Logger logger = Logger.getLogger(ObjectViewerTableDecorator.class);

	@Override
	public boolean initExtension(UserDBDAO userDB) {
		if(userDB == null) return false;
		
		mapColumnDescList.clear();
		
		SpatiaDBFactory factory = new SpatiaDBFactory();
		SpatialDB spatialDB = factory.getSpatialDB(userDB);
		if(spatialDB == null) return false;
		
		mapColumnDescList.putAll( spatialDB.getSpatialTableColumn() );
		if(mapColumnDescList.isEmpty()) return false;
		else return true;
	}

	@Override
	public Image getTableImage(String tableName) {
		if(mapColumnDescList.containsKey(tableName)) return SpatialUtils.getMapMakerIcon();
		return null;
	}

	@Override
	public Image getColumnImage(String tableName, String columnName) {
		if(mapColumnDescList.containsKey(tableName)) {
			List<String> listColumn = mapColumnDescList.get(tableName);
			if(listColumn.contains(columnName)) return SpatialUtils.getMapMakerIcon();
		}
		return null;
	}

}
