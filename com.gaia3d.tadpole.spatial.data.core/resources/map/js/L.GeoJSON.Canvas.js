/*
 GeoJSON Layer using Canvas for leaflet, 
 BJ Jang, November , 2014

 Based on L.CanvasOverlay.js at http://bl.ocks.org/sumbera/11114288
  
 Apache License
*/


L.GeoJSON.Canvas = L.CanvasOverlay.extend({

	initialize: function (geojson, options) {
		L.setOptions(this, options);

		this._data = [];
		this._bounds = null;

		if (geojson) {
			this.addData(geojson);
		}
	},
	
	setOptions: function (options) {
		L.setOptions(this, options);
	},

	setData: function (geojson) {
		this.clearLayers();
		this._addData(geojson);
		this.redraw();
	},

	addData: function (geojson) {
		this._addData(geojson);
		this.redraw();
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

		var mbr = this._calcMbr(geojson.geometry.coordinates);
		if (!this._bounds) 
			this._bounds = new L.LatLngBounds(mbr);
		else {
			this._bounds.extend(mbr.getSouthWest());
			this._bounds.extend(mbr.getNorthEast());
		}
		
		geojson.geometry.mbr = mbr;
		
		return this._data.push(geojson.geometry);
	},
	
	clearLayers: function() {
		this._data = [];
		this._bounds = null;
	},
	
	_calcMbr: function (coordinates) {
		// case of point
		if (typeof(coordinates[0])=="number") {
			var p1 = L.latLng(coordinates[1], coordinates[0]),
		    p2 = L.latLng(coordinates[1], coordinates[0]),
		    retBounds = L.latLngBounds(p1, p2);
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
			return retBounds;
		}
		
		// case of array array
		var totBounds = this._calcMbr(coordinates[0]);
		for (var i=1; i<coordinates.length; i++) {
			var subBounds = this._calcMbr(coordinates[i]);
			totBounds.extend(subBounds);
		}
		return totBounds;
	},
	
	getBounds : function() {
		return this._bounds;
	},
	
	_drawPoint: function(canvasOverlay, ctx, coord) {
		var dot = canvasOverlay._map.latLngToContainerPoint(L.latLng(coord[1], coord[0]));
        ctx.beginPath();
        ctx.arc(dot.x, dot.y, this.options.BasePointRadius, 0, Math.PI * 2);
        ctx.fill();
        ctx.closePath();			
	},
	_drawLineString: function(canvasOverlay, ctx, coordinates) {
        ctx.beginPath();
		for (var i=0; i<coordinates.length; i++) {
			var coord = coordinates[i]
			var dot = canvasOverlay._map.latLngToContainerPoint(L.latLng(coord[1], coord[0]));
	        ctx[i==0?"moveTo":"lineTo"](dot.x, dot.y);
		}
		ctx.stroke();
	},
	_drawPolygon: function(canvasOverlay, ctx, coordinates) {
        ctx.beginPath();
		for (var iPoly=0; iPoly<coordinates.length; iPoly++) {
			var poly = coordinates[iPoly];
			for (var iRing=0; iRing<poly.length; iRing++) {
				var coord = poly[iRing];
				var dot = canvasOverlay._map.latLngToContainerPoint(L.latLng(coord[1], coord[0]));
		        ctx[iRing==0?"moveTo":"lineTo"](dot.x, dot.y);
			}
			ctx.closePath();
		}
		ctx.fill();
		ctx.stroke();
	},
	_drawMbr: function(canvasOverlay, ctx, mbr) {
		var coordinates = [mbr.getSouthWest(), mbr.getSouthEast(), mbr.getNorthEast(), mbr.getNorthWest(), mbr.getSouthWest()];
        ctx.beginPath();
		for (var i=0; i<coordinates.length; i++) {
			var dot = canvasOverlay._map.latLngToContainerPoint(coordinates[i]);
	        ctx[i==0?"moveTo":"lineTo"](dot.x, dot.y);
		}
		ctx.stroke();
	},

	_userDrawFunc: function(canvasOverlay, params) {
		self = this;
        var ctx = params.canvas.getContext('2d');
        ctx.clearRect(0, 0, params.canvas.width, params.canvas.height);
        ctx.fillStyle = this.options.BaseFillColor;
        ctx.strokeStyle = this.options.BaseLineColor;
        ctx.lineWidth = this.options.BaseLineWidth;
        
        var viewBounds = params.bounds;
       
        for (var i=0; i<this._data.length; i++) {
        	geometry = this._data[i];
        	if (!viewBounds.intersects(geometry.mbr))
        		continue;
        	
        	switch(geometry.type) {
        	case "Point":
        		pnt = geometry.coordinates;
        		self._drawPoint(canvasOverlay, ctx, pnt);
        		break;
        	case "MultiPoint":
        		for (var j=0; j<geometry.coordinates.length; j++) {
        			self._drawPoint(canvasOverlay, ctx, geometry.coordinates[j]);
        		}
        		break;
        	case "Polygon":
        		poly = geometry.coordinates;
        		self._drawPolygon(canvasOverlay, ctx, poly);
        		break;
        	case "MultiPolygon":
        		for (var j=0; j<geometry.coordinates.length; j++) {
        			self._drawPolygon(canvasOverlay, ctx, geometry.coordinates[j]);
        		}
        		break;
        	case "LineString":
        		line = geometry.coordinates;
        		self._drawLineString(ccanvasOverlay, ctx, line);
        		break;
        	case "MultiLineString":
        		for (var j=0; j<geometry.coordinates.length; j++) {
        			self._drawLineString(canvasOverlay, ctx, geometry.coordinates[j]);
        		}
        		break;
        	}
        }
    }
});

L.geoJson.canvas = function (geojson, options) {
	return new L.GeoJSON.Canvas(geojson, options);
};
