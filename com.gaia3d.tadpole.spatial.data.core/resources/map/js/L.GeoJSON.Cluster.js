/*
 GeoJSON Layer using PointCluster for leaflet, 
 BJ Jang, November , 2014

 Based on leaflet.markercluster.js at https://github.com/Leaflet/Leaflet.markercluster
  
 Apache License
*/


L.GeoJSON.Cluster = L.MarkerClusterGroup.extend({

	initialize: function (geojson, options) {
		L.MarkerClusterGroup.prototype.initialize(options);

		this._bounds = null;

		if (geojson) {
			this.addData(geojson);
		}
	},

	clearLayers: function() {
		L.MarkerClusterGroup.prototype.clearLayers();
		// for bug fix
		this._generateInitialClusters();
		this._bounds = null;
	},
	
	setOptions: function (options) {
		L.Util.setOptions(this, options);
	},

	setData: function (geojson) {
		this.clearLayers();
		this._addData(geojson);
	},

	addData: function (geojson) {
		this._addData(geojson);
	},
	_addData: function (geojson) {
		var features = L.Util.isArray(geojson) ? geojson : geojson.features,
		    i, len, feature;

		if (features) {
			for (i = 0, len = features.length; i < len; i++) {
				// Only add this if geometry or geometries are set and not null
				feature = features[i];
				if (feature.geometry) {
					this._addData(features[i]);
				}
			}
			return this;
		}

		var mbr = this._registDataAndCalcMbr(geojson.geometry.coordinates);
		if (!this._bounds) 
			this._bounds = mbr;
		else {
			this._bounds.extend(mbr);
		}
		
		return this;
	},
	
	_registPoint: function(point) {
		var marker = L.marker(new L.LatLng(point[1], point[0]));
		this.addLayer(marker);
	},
	
	_registDataAndCalcMbr: function (coordinates) {
		// case of point
		if (typeof(coordinates[0])=="number") {
			var p1 = L.latLng(coordinates[1], coordinates[0]),
		    p2 = L.latLng(coordinates[1], coordinates[0]),
		    retBounds = L.latLngBounds(p1, p2);
			
			this._registPoint(coordinates);
			
		    return retBounds;
		}
		
		// case of point array
		if (typeof(coordinates[0][0])=="number") {
			var retBounds;
			var fstPnt = coordinates[0];
			var p1 = L.latLng(fstPnt[1], fstPnt[0]),
		    p2 = L.latLng(fstPnt[1], fstPnt[0]),
		    retBounds = L.latLngBounds(p1, p2);
			for (var i=1; i<coordinates.length; i++) {
				retBounds.extend(L.latLng(coordinates[i][1], coordinates[i][0]));
			}
			
			this._registPoint([(retBounds.getWest()+retBounds.getEast())/2.0, (retBounds.getSouth()+retBounds.getNorth())/2.0]);
			return retBounds;
		}
		
		// case of array array
		var totBounds = this._registDataAndCalcMbr(coordinates[0]);
		for (var i=1; i<coordinates.length; i++) {
			var subBounds = this._registDataAndCalcMbr(coordinates[i]);
			totBounds.extend(subBounds.min);
			totBounds.extend(subBounds.max);
		}
		return totBounds;
	},
	
	addTo: function (map) {
		map.addLayer(this);
		// for bug fix
		L.MarkerClusterGroup.prototype.onAdd(map);
		return this;
	},
    getBounds : function() {
		return this._bounds;
	}
});

L.geoJson.cluster = function (geojson, options) {
	return new L.GeoJSON.Cluster(geojson, options);
};
