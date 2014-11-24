/*
 GeoJSON Layer using Canvas for leaflet, 
 BJ Jang, November , 2014

 Based on leaflet-heatmap.js at http://www.patrick-wied.at/static/heatmapjs/example-heatmap-leaflet.html
  
 Apache License
*/


L.GeoJSON.Heatmap = HeatmapOverlay.extend({

	initialize: function (geojson, options) {
		this.cfg = options;
		
		HeatmapOverlay.prototype.initialize(options);

		this._data = [];
		this._bounds = null;
		this._max = 1;

		if (geojson) {
			this.addData(geojson);
		}
	},

	setData: function (geojson) {
		this.clearLayers();
		this._addData(geojson);
		this._draw();
	},

	addData: function (geojson) {
		this._addData(geojson);
		this._draw();
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
			this._bounds.extend(mbr.getSouthWest());
			this._bounds.extend(mbr.getNorthEast());
		}
		
	    geojson.geometry.mbr = mbr;
		
		return this;
	},
	
	_registPoint: function(latlng, value) {
		var dataObj = { latlng: latlng, value: value };
		this._data.push(dataObj);
	},
	
	clearLayers: function() {
		this._data = [];
		this._bounds = null;
	},
	
	_registDataAndCalcMbr: function (coordinates) {
		// case of point
		if (typeof(coordinates[0])=="number") {
			var p1 = L.latLng(coordinates[1], coordinates[0]),
		    p2 = L.latLng(coordinates[1], coordinates[0]),
		    retBounds = L.latLngBounds(p1, p2);
			
			this._registPoint(p1, 1);
			
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
				
				var p1 = new L.LatLng(coordinates[i][1], coordinates[i][0]);
				this._registPoint(p1, 1);
			}
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
        return this;
    },

    getBounds : function() {
		return this._bounds;
	}
});

L.geoJson.heatmap = function (geojson, options) {
	return new L.GeoJSON.Heatmap(geojson, options);
};
