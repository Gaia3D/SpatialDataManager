//package com.gaia3d.tadpole.spatial.data.core.shape;
//
//import java.io.File;
//
//import org.geotools.data.FileDataStore;
//import org.geotools.data.FileDataStoreFinder;
//import org.geotools.data.Query;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.feature.FeatureCollection;
//import org.geotools.feature.FeatureIterator;
//import org.opengis.feature.Property;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.feature.simple.SimpleFeatureType;
//
///**
// * shape, dbf 파일 보는것은 마지막 예제만 참고할것
// * 	http://docs.geotools.org/stable/userguide/library/data/shape.html
// * 
// * 화면은 다음을 참고할
// * 	http://www.sqlmusings.com/wp-content/uploads/2011/02/shape2sql.png
// * 
// * insert 문 참고 할것.
// * 	http://gis.stackexchange.com/questions/24486/how-to-insert-a-point-into-postgis
// * 
// * 
// * 
//CREATE TABLE buildings( 
//	gid serial
//	,name character varying(50) 
//	,the_geom geometry(MULTIPOLYGON,4326) 
//	,osm_id integer
//	,type character varying(50) 
//	,CONSTRAINT buildings_pkey PRIMARY KEY ( gid) 
//);
//
//INSERT INTO buildings(name, the_geom, osm_id, type)
//VALUES(
//    'buildings.590'
//    ,ST_GeomFromText('MULTIPOLYGON (((-141.3732044 -80.9270179, -141.116904 -80.9155653, -141.1214403 -80.9466686, -141.3618637 -80.9420277, -141.3732044 -80.9270179)))', 4326)
//    ,348698311
//    ,''
//);
//
// * @author hangum
// *
// */
//public class ShapeToSQL {
//
//	public static void main(String[] args) {
//		
//		try {
//		    File file = new File("./buildings/buildings.shp");
//		    FileDataStore myData = FileDataStoreFinder.getDataStore( file );
//		    SimpleFeatureSource source = myData.getFeatureSource();
//		    SimpleFeatureType schema = source.getSchema();
//	
//		    Query query = new Query(schema.getTypeName());
////		    query.setMaxFeatures(1);
//		    
//		    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(query);
//		    try (FeatureIterator<SimpleFeature> features = collection.features()) {
//		        while (features.hasNext()) {
//		            SimpleFeature feature = features.next();
//		            System.out.println("1." + feature.getID() + ": ");
//		            for (Property attribute : feature.getProperties()) {
//		                System.out.println("1-1." + "\t"+attribute.getName()+":"+attribute.getValue() );
//		            }
//		        }
//		    }
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//
//	}
//}
