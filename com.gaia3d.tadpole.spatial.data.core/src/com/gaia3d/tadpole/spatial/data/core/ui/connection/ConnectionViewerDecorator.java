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
package com.gaia3d.tadpole.spatial.data.core.ui.connection;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.extensionpoint.definition.IConnectionDecoration;

/**
 * connection viewer decorator
 * 
 * @author hangum
 *
 */
public class ConnectionViewerDecorator implements IConnectionDecoration {
	private static final Logger logger = Logger.getLogger(ConnectionViewerDecorator.class);
	
	@Override
	public Image getImage(UserDBDAO userDB) {
		//
		// 디비를 불러올때 공간 디비를 지원하는 것은 시스템 전반에 속도 저하를 가져와서 주석으로 막았습니다. - 현종(12.13.2015)
		//
//		if(userDB == null) return null;
//		
//		SpatiaDBFactory factory = new SpatiaDBFactory();
//		SpatialDB spatialDB = factory.getSpatialDB(userDB);
//		if(spatialDB == null) return null;
//		return spatialDB.isSpatialDBImage();
		return null;
	}
	
}
