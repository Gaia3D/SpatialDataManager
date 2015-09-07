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
package com.gaia3d.tadpole.spatial.geotools.code.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * utils
 * 
 * @author hangum
 *
 */
public class GeoSpatialUtils {
//	private static final Logger logger = Logger.getLogger(GeoSpatialUtils.class);
	
	/**
	 * shape to List<Map<String, Object>
	 * 
	 * @param strShapeFile
	 * @return
	 * @throws IOException
	 */
	public static List<Map<String, Object>> getShapeToList(String strShapeFile) throws Exception {
		List<Map<String, Object>> listReturn = new LinkedList<>();
//		logger.debug("----0");
		
//		try {
//			logger.debug(strShapeFile);
		    File file = new File(strShapeFile);//"./buildings/buildings.shp");
//		    logger.debug("File is " + (Boolean.valueOf(file.exists())?"Exist":"does not exist.") );
		    
		    FileDataStore myData = FileDataStoreFinder.getDataStore( file );

			SimpleFeatureSource source = myData.getFeatureSource();
		    SimpleFeatureType schema = source.getSchema();
		    Query query = new Query(schema.getTypeName());
//			query.setMaxFeatures(1);
	    
	    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(query);
	    try (FeatureIterator<SimpleFeature> features = collection.features()) {
	        while (features.hasNext()) {
	            SimpleFeature feature = features.next();
	            
	            Map<String, Object> mapTemp = new HashMap<>();
	        	mapTemp.put("id", feature.getID());
	            
//	            System.out.println("1." + feature.getID() + ": ");
	            for (Property attribute : feature.getProperties()) {
//	            	System.out.println("1-1." + "\t"+attribute.getName()+":"+attribute.getValue() );
	                mapTemp.put(attribute.getName().toString(), attribute.getValue());
	            }
	            
	            listReturn.add(mapTemp);
	        }
	    }
//		} catch(Throwable e) {
////			logger.error(e);
//			e.printStackTrace();
//		}

		return listReturn;
	}

}
