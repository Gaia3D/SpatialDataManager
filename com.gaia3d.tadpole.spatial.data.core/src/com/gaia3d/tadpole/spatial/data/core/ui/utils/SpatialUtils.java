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
package com.gaia3d.tadpole.spatial.data.core.ui.utils;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.gaia3d.tadpole.spatial.data.core.Activator;
import com.gaia3d.tadpole.spatial.data.core.ui.define.SpatialDefine;
import com.swtdesigner.ResourceManager;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;

/**
 * utils
 * 
 * @author hangum
 *
 */
public class SpatialUtils {
	private static final Logger logger = Logger.getLogger(SpatialUtils.class);

	/**
	 * map marker icon
	 * 
	 * @return
	 */
	public static Image getMapMakerIcon() {
		return ResourceManager.getPluginImage(Activator.PLUGIN_ID, SpatialDefine.SPATIAL_ICON);
	}
	
	/**
	 * wkt to geojson
	 * 
	 * @param strWKT
	 * @return
	 * @throws Exception
	 */
	public static String wktToGeojson(String strWKT) throws ParseException {
		GeometryFactory geoFactory = new GeometryFactory();
		WKTReader wktReader = new WKTReader(geoFactory);
		
		Geometry geometry = wktReader.read(strWKT);
		
		GeoJSONWriter geojson = new GeoJSONWriter();
		org.wololo.geojson.Geometry wololoGeojson = geojson.write(geometry);
		return wololoGeojson.toString();
	}
	
	/**
	 * oracle.sql.STRUCT to wkt
	 * 
	 * @param struct
	 * @return
	 * @throws Exception
	 */
	public static String oralceStructToWKT(oracle.sql.STRUCT struct) throws Exception {
		JGeometry j_geom = JGeometry.load(struct);
		
		WKT wkt = new WKT();
		return new String(wkt.fromJGeometry(j_geom));		
	}
}
