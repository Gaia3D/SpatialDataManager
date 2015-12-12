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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.FeatureId;

/**
 * Write shape file utils
 * 
 * https://github.com/ianturton/geotools-cookbook/blob/master/modules/output/src/main/java/org/ianturton/cookbook/output/WriteShapefile.java
 * 
 * @author hangum
 *
 */
public class WriteShapefile {
	private static final Logger logger = Logger.getLogger(WriteShapefile.class);
	private ShapefileDataStore shpDataStore;

	public WriteShapefile(File f) {
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<String, Serializable>();
		try {
			params.put("url", f.toURI().toURL());
		} catch (MalformedURLException e) {
			logger.error("Malformed url", e);
		}
		params.put("create spatial index", Boolean.TRUE);

		try {
			shpDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		} catch (IOException e) {
			logger.error("io Exception", e);
		}
	}

	/**
	 * 
	 * @param featureCollection
	 * @return
	 */
	public boolean writeFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) throws Exception {
		if (shpDataStore == null) {
			throw new IllegalStateException("Datastore can not be null when writing");
		}
		SimpleFeatureType schema = featureCollection.getSchema();
		GeometryDescriptor geom = schema.getGeometryDescriptor();

		try {
			
			/*
			 * The Shapefile format has a couple limitations: - "the_geom" is always
			 * first, and used for the geometry attribute name - "the_geom" must be of
			 * type Point, MultiPoint, MuiltiLineString, MultiPolygon - Attribute
			 * names are limited in length - Not all data types are supported (example
			 * Timestamp represented as Date)
			 *
			 * Because of this we have to rename the geometry element and then rebuild
			 * the features to make sure that it is the first attribute.
			 */

			List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
			GeometryType geomType = null;
			List<AttributeDescriptor> attribs = new ArrayList<AttributeDescriptor>();
			for (AttributeDescriptor attrib : attributes) {
				AttributeType type = attrib.getType();
				if (type instanceof GeometryType) {
					geomType = (GeometryType) type;

				} else {
					attribs.add(attrib);
				}
			}

			GeometryTypeImpl gt = new GeometryTypeImpl(new NameImpl("the_geom"),
					geomType.getBinding(), geomType.getCoordinateReferenceSystem(),
					geomType.isIdentified(), geomType.isAbstract(),
					geomType.getRestrictions(), geomType.getSuper(),
					geomType.getDescription());

			GeometryDescriptor geomDesc = new GeometryDescriptorImpl(gt,
					new NameImpl("the_geom"), geom.getMinOccurs(), geom.getMaxOccurs(),
					geom.isNillable(), geom.getDefaultValue());

			attribs.add(0, geomDesc);

			SimpleFeatureType shpType = new SimpleFeatureTypeImpl(schema.getName(),
					attribs, geomDesc, schema.isAbstract(), schema.getRestrictions(),
					schema.getSuper(), schema.getDescription());
			shpDataStore.createSchema(shpType);
			
			/*
			 * Write the features to the shapefile
			 */
			Transaction transaction = new DefaultTransaction("create");
			String typeName = shpDataStore.getTypeNames()[0];
			SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
			if (featureSource instanceof SimpleFeatureStore) {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

				List<SimpleFeature> feats = new ArrayList<SimpleFeature>();

				FeatureIterator<SimpleFeature> features2 = featureCollection.features();
				while (features2.hasNext()) {
					SimpleFeature f = features2.next();
					SimpleFeature reType = SimpleFeatureBuilder.build(shpType, f.getAttributes(), "");

					feats.add(reType);
				}
				features2.close();
				SimpleFeatureCollection collection = new ListFeatureCollection(shpType, feats);

				featureStore.setTransaction(transaction);
				try {
					List<FeatureId> ids = featureStore.addFeatures(collection);
					transaction.commit();
				} catch (Exception problem) {
					problem.printStackTrace();
					transaction.rollback();
				} finally {
					transaction.close();
				}
				shpDataStore.dispose();
				return true;
			} else {
				shpDataStore.dispose();
				logger.error("Input file is doesn't  SimpleFeatureStore file. So, ShapefileStore not writable. " + featureSource.getClass());
				throw new Exception("Input file is doesn't  SimpleFeatureStore file. So, ShapefileStore not writable. " + featureSource.getClass());
			}
		} catch (Exception e) {
			logger.error("not writterable shape file", e);
			throw e;
		}
	}
}