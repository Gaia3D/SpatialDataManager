/*
 GeoJSON Layer using Heatmap for leaflet, 
 BJ Jang, November , 2014

 Based on leaflet-heatmap.js at http://www.patrick-wied.at/static/heatmapjs/example-heatmap-leaflet.html
  
 Apache License
*/


L.GeoJSON.Heatmap = HeatmapOverlay.extend({

	initialize: function (geojson, options) {
		this.setOptions(options);

		this._data = [];
		this._bounds = null;
		this._max = 1;

		if (geojson) {
			this.addData(geojson);
		}
	},

	clearLayers: function() {
		this._data = [];
		this._bounds = null;
		this._heatmap._renderer._clear();
	},
	
	setOptions: function (options) {
		this.cfg = options;
		this.cfg.latField = 1;
		this.cfg.lngField = 0;
		this.cfg.valueField = null;

		HeatmapOverlay.prototype.initialize(this.cfg);
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
	
	_registPoint: function(point) {
		this._data.push(point);
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
				
				this._registPoint(coordinates[i]);
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
	
	_update: function() {
		    var bounds, zoom, scale;

		    bounds = this._map.getBounds();
		    zoom = this._map.getZoom();
		    scale = Math.pow(2, zoom);

		    if (this._data.length == 0) {
		      return;
		    }

		    var generatedData = { max: this._max };
		    var latLngPoints = [];
		    var radiusMultiplier = this.cfg.scaleRadius ? scale : 1;
		    var localMax = 0;
		    var valueField = this.cfg.valueField;
		    var len = this._data.length;
		  
		    while (len--) {
		      var entry = this._data[len];
		      var value = entry[valueField];
		      var latlng = new L.LatLng(entry[1], entry[0]);


		      // we don't wanna render points that are not even on the map ;-)
		      if (!bounds.contains(latlng)) {
		        continue;
		      }
		      // local max is the maximum within current bounds
		      if (value > localMax) {
		        localMax = value;
		      }

		      var point = this._map.latLngToContainerPoint(latlng);
		      var latlngPoint = { x: Math.round(point.x), y: Math.round(point.y) };
		      latlngPoint[valueField] = value;

		      var radius;

		      if (entry.radius) {
		        radius = entry.radius * radiusMultiplier;
		      } else {
		        radius = (this.cfg.radius || 2) * radiusMultiplier;
		      }
		      latlngPoint.radius = radius;
		      latLngPoints.push(latlngPoint);
		    }
		    if (this.cfg.useLocalExtrema) {
		      generatedData.max = localMax;
		    }

		    generatedData.data = latLngPoints;

		    this._heatmap.setData(generatedData);
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
