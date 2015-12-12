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
package com.gaia3d.tadpole.spatial.geotools.code.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * geo spatial utils
 * 
 * shape file merge : http://www.helptouser.com/gis/44874-merging-shapefiles-with-geotools.html
 * 
 * @author hangum
 *
 */
public class GeoSpatialUtils {
	private static final Logger logger = Logger.getLogger(GeoSpatialUtils.class);
	
	/**
	 * featuredjson to shape
	 * 
	 * @param geojsonStr 
	 * @param shapeLocation
	 * @return
	 * @throws Exception
	 */
	public static boolean toShp(String geojsonStr, String shapeLocation) throws Exception {
		if(logger.isDebugEnabled()) logger.debug("##### start ========>make shape file =================");
		GeometryJSON gjson = new GeometryJSON(150);
		FeatureJSON fjson = new FeatureJSON(gjson);
		
		FeatureCollection<SimpleFeatureType, SimpleFeature> fc 
			= fjson.readFeatureCollection(new ByteArrayInputStream(geojsonStr.getBytes(StandardCharsets.UTF_8)));
		fc.getSchema();
		
		WriteShapefile writer = new WriteShapefile(new File(shapeLocation));
		return writer.writeFeatures(fc);
	}
	
	/**
	 * shape to List<Map<String, Object>
	 * 
	 * @param strShapeFile
	 * @return
	 * @throws IOException
	 */
	public static List<Map<String, Object>> getShapeToList(String strShapeFile) throws Exception {
//		if(logger.isDebugEnabled()) logger.debug("==========> shape to object");
		List<Map<String, Object>> listReturn = new LinkedList<>();
	    FileDataStore myData = FileDataStoreFinder.getDataStore(new File(strShapeFile));

		SimpleFeatureSource source = myData.getFeatureSource();
	    SimpleFeatureType schema = source.getSchema();
	    Query query = new Query(schema.getTypeName());
//		query.setMaxFeatures(1);
	    
	    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(query);
	    try (FeatureIterator<SimpleFeature> features = collection.features()) {
	        while (features.hasNext()) {
	            SimpleFeature feature = features.next();
	            
	            Map<String, Object> mapTemp = new HashMap<>();
	        	mapTemp.put("id", feature.getID());
	            
//	            if(logger.isDebugEnabled()) logger.debug("1." + feature.getID() + ": ");
//	            System.out.println("1." + feature.getID() + ": ");
	            for (Property attribute : feature.getProperties()) {
//	            	logger.debug("1-1." + "\t"+attribute.getName()+":"+attribute.getValue() );
//	            	System.out.println("1-1." + "\t"+attribute.getName()+":"+attribute.getValue() );
	                mapTemp.put(attribute.getName().toString(), attribute.getValue());
	            }
	            
	            listReturn.add(mapTemp);
	        }
	    }

		return listReturn;
	}

}
