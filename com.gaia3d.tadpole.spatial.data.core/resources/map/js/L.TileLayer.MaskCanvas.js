L.TileLayer.MaskCanvas = L.TileLayer.Canvas.extend({
/*    options: {
        radius: 5,
        useAbsoluteRadius: true,  // true: radius in meters, false: radius in pixels
        color: '#000',
        opacity: 0.5,
        noMask: false,  // true results in normal (filled) circled, instead masked circles
        lineColor: undefined,  // color of the circle outline if noMask is true
        debug: false
    },
*/
    options: {
    	tileSize: 512,
        opacity: 1.0,
        noMask: true,  // true results in normal (filled) circled, instead masked circles
        pointColor: 'rgba(0, 0, 255, 0.5)',  // color of the circle outline if noMask is true
        radius: 10,
        useAbsoluteRadius: true,  // true: radius in meters, false: radius in pixels
        lineColor: 'rgba(0, 0, 255, 0.5)',  // color of the circle outline if noMask is true
        lineWidth: 2,
        fillColor: 'rgba(0, 0, 200, 0.5)',
        debug: false
    },
    _flagOnLoad: false,

    initialize: function (options, data) {
        var self = this;
        self._quad = null;
        self.bound = null;
        
        L.Util.setOptions(this, options);

        this.drawTile = function (tile, tilePoint, zoom) {
            var ctx = {
                canvas: tile,
                tilePoint: tilePoint,
                zoom: zoom
            };

            if (self.options.debug) {
                self._drawDebugInfo(ctx);
            }
            this._draw(ctx);
        };
    },

    _drawDebugInfo: function (ctx) {
        var max = this.tileSize;
        var g = ctx.canvas.getContext('2d');
        g.globalCompositeOperation = 'destination-over';
        g.strokeStyle = '#000000';
        g.fillStyle = '#FFFF00';
        g.strokeRect(0, 0, max, max);
        g.font = "12px Arial";
        g.fillRect(0, 0, 5, 5);
        g.fillRect(0, max - 5, 5, 5);
        g.fillRect(max - 5, 0, 5, 5);
        g.fillRect(max - 5, max - 5, 5, 5);
        g.fillRect(max / 2 - 5, max / 2 - 5, 10, 10);
        g.strokeText(ctx.tilePoint.x + ' ' + ctx.tilePoint.y + ' ' + ctx.zoom, max / 2 - 30, max / 2 - 10);
    },

    setData: function(dataset) {
        var self = this;

        self.bounds = null;
        self._quad = null;
        
        self.addData(dataset);
    },

    addData: function(dataset) {
        var self = this;

        try {
            self._flagOnLoad = true;
            if (dataset instanceof L.GeoJSON) {
            	var subBounds = dataset.getBounds();
            	if (!self.bounds) 
            		self.bounds = subBounds;
            	else 
            		self.bounds.extend(subBounds);
            	
            	if (!self._quad) 
            		self._quad = new QuadTree(this._boundsToQuery(self.bounds), false, 6, 6);
            	else {
            		tempQuad = new QuadTree(this._boundsToQuery(self.bounds), false, 6, 6);
            		tempQuad.insert(self._quad.root.getChildren());
            		self._quad = tempQuad;
            	}
            	
            	dataset.getLayers().forEach(function(d) {
            		if (d.feature && d.feature.geometry && d.feature.geometry.type == "Point") {
    	                self._quad.insert({
    	                    x: d.getLatLng().lng, 
    	                    y: d.getLatLng().lat,
    	                    geometry: d.feature.geometry
    	                });
            		}
            		else {
            			bounds = d.getBounds();
    	                self._quad.insert({
    	                    x: bounds.getWest(), 
    	                    y: bounds.getSouth(),
    	                    width: bounds.getEast() - bounds.getWest(),
    	                    height:	bounds.getNorth() - bounds.getSouth(),
    	                    geometry: d.feature.geometry
    	                });
            		}
                });
            }
            else if (dataset instanceof Array) {
                this.bounds = new L.LatLngBounds(dataset);
                this._quad = new QuadTree(this._boundsToQuery(this.bounds), false, 6, 6);
                var first = dataset[0];
                var xc = 1, yc = 0;
                if (first instanceof L.LatLng) {
                    xc = "lng";
                    yc = "lat";
                }

                dataset.forEach(function(d) {
                    self._quad.insert({
                        x: d[xc], //lng
                        y: d[yc] //lat
                    });
                });
            }
		} catch(err) {
			console.log("Rise exception(addData function) : " + err);
		}

        self._flagOnLoad = false;
        if (this._map) {
            this.redraw();
        }
    },

    setRadius: function(radius) {
        this.options.radius = radius;
        this.redraw();
    },

    _tilePoint: function (ctx, coords) {
        // start coords to tile 'space'
        var s = ctx.tilePoint.multiplyBy(this.options.tileSize);

        // actual coords to tile 'space'
        var p = this._map.project(new L.LatLng(coords.y, coords.x));

        // point to draw
        var x = Math.round(p.x - s.x);
        var y = Math.round(p.y - s.y);
        return [x, y];
    },

    _boundsToQuery: function(bounds) {
        if (bounds.getSouthWest() == undefined) { return {x: 0, y: 0, width: 0.1, height: 0.1}; }  // for empty data sets
        return {
            x: bounds.getSouthWest().lng,
            y: bounds.getSouthWest().lat,
            width: bounds.getNorthEast().lng-bounds.getSouthWest().lng,
            height: bounds.getNorthEast().lat-bounds.getSouthWest().lat
        };
    },

    _getLatRadius: function () {
        return (this.options.radius / 40075017) * 360;
    },

    _getLngRadius: function () {
        return this._getLatRadius() / Math.cos(L.LatLng.DEG_TO_RAD * this._latlng.lat);
    },

    // call to update the radius
    projectLatlngs: function () {
        var lngRadius = this._getLngRadius(),
            latlng2 = new L.LatLng(this._latlng.lat, this._latlng.lng - lngRadius, true),
            point2 = this._map.latLngToLayerPoint(latlng2),
            point = this._map.latLngToLayerPoint(this._latlng);
        this._radius = Math.max(Math.round(point.x - point2.x), 1);
    },

    // the radius of a circle can be either absolute in pixels or in meters
    _getRadius: function() {
        if (this.options.useAbsoluteRadius) {
            return this._radius;
        } else{
            return this.options.radius;
        }
    },

    _draw: function (ctx) {
    	try {
        	if (this._flagOnLoad)
        		return;
        	
            if (!this._quad || !this._map) {
                return;
            }

            var tileSize = this.options.tileSize;

            var nwPoint = ctx.tilePoint.multiplyBy(tileSize);
            var sePoint = nwPoint.add(new L.Point(tileSize, tileSize));

            if (this.options.useAbsoluteRadius) {
                var centerPoint = nwPoint.add(new L.Point(tileSize/2, tileSize/2));
                this._latlng = this._map.unproject(centerPoint);
                this.projectLatlngs();
            }

            // padding
            var pad = new L.Point(this._getRadius(), this._getRadius());
            nwPoint = nwPoint.subtract(pad);
            sePoint = sePoint.add(pad);

            var bounds = new L.LatLngBounds(this._map.unproject(sePoint), this._map.unproject(nwPoint));

//            var nodes = this._quad.retrieveInBounds(this._boundsToQuery(bounds));
            var nodes = this._quad.retrieve(this._boundsToQuery(bounds));

            this._drawNodes(ctx, nodes);
    	} catch(err) {
    		console.log("Rise exception(_draw function) : " + err);
    	}
    	
    },

    _drawNodes: function (ctx, nodes) {
        var c = ctx.canvas,
            g = c.getContext('2d'),
            self = this,
            p,
            tileSize = this.options.tileSize;
        g.fillStyle = this.options.fillColor;

        if (this.options.lineColor) {
          g.strokeStyle = this.options.lineColor;
          g.lineWidth = this.options.lineWidth || 1;
        }
        g.globalCompositeOperation = 'source-over';
        if (!this.options.noMask) {
            g.fillRect(0, 0, tileSize, tileSize);
            g.globalCompositeOperation = 'destination-out';
        }
        nodes.forEach(function(node) {
        	if (typeof(node.geometry) == "undefined") {
				p = self._tilePoint(ctx, node);
				g.beginPath();
				g.arc(p[0], p[1], self._getRadius(), 0, Math.PI * 2);
				g.fill();
				if (self.options.lineColor) {
				    g.stroke();
				}
				return;
        	}
        	
        	var geometry = node.geometry;
        	var type = geometry.type;
        	switch (type) {
        	case "Point":
        		pnt = geometry.coordinates;
        		self._drawPoint(ctx, g, pnt);
        		break;
        	case "MultiPoint":
        		if (geometry.coordinates.length == 1) { // forEach 동작이상 대응
        			self._drawPoint(ctx, g, geometry.coordinates[0]);
        		}
        		else {
            		geometry.coordinates.forEach(function(multipnt) {
            			multipnt.forEach(function(pnt) {
            				self._drawPoint(ctx, g, pnt);
            			});
            		});
        		}
        		break;
        	case "Polygon":
        		poly = geometry.coordinates;
        		self._drawPolygon(ctx, g, poly);
        		break;
        	case "MultiPolygon":
        		geometry.coordinates.forEach(function(multipoly) {
        			multipoly.forEach(function(poly) {
        				self._drawPolygon(ctx, g, poly);
        			});
        		});
        		break;
        	case "LineString":
        		line = geometry.coordinates;
        		self._drawLineString(ctx, g, line);
        		break;
        	case "MultiLineString":
        		geometry.coordinates.forEach(function(multiline) {
        			multiline.forEach(function(line) {
        				self._drawLineString(ctx, g, line);
        			});
        		});
        		break;
        	}
        });
    },

    _drawPoint: function(ctx, g, pnt) {
    	self = this;
    	p = self._tilePoint(ctx, {x:pnt[0],y:pnt[1]});
		g.beginPath();
		g.arc(p[0], p[1], self._getRadius(), 0, Math.PI * 2);
		g.fill();
		if (this.options.lineColor) {
		   g.stroke();
		}
    },
    _drawLineString: function(ctx, g, line) {
    	self = this;
    	var isStart = true;
    	var p;
    	
		g.beginPath();
		line.forEach(function(pnt) {
            p = self._tilePoint(ctx, {x:pnt[0],y:pnt[1]});
            if(isStart) {
            	g.moveTo(p[0], p[1]);
            	isStart = false;
            } else {
                g.lineTo(p[0], p[1]);
            }
		});
        g.stroke();
    },
    _drawPolygon: function(ctx, g, polygon) {
    	self = this;		
		var startPnt = null;
		var p;
		
//		g.beginPath();
//		polygon.forEach(function(pnt) {
//			p = self._tilePoint(ctx, {x:pnt[0],y:pnt[1]});
//			if (!startPnt) {
//				startPnt = pnt;
//				g.moveTo(p[0], p[1]);
//				return;
//			}
//
//			g.lineTo(p[0], p[1]);
//			
//			if (pnt[0] == startPnt[0] && pnt[1] == startPnt[1]) {
//				startPnt = null;
//			}
//		});
//		if (startPnt) g.lineTo(p[0], p[1]);
//		g.closePath();
//        g.fill();
//        g.stroke();
		g.beginPath();
		polygon.forEach(function(pnt) {
			p = self._tilePoint(ctx, {x:pnt[0],y:pnt[1]});
			if (!startPnt) {
				startPnt = pnt;
				g.moveTo(p[0], p[1]);
				return;
			}

			g.lineTo(p[0], p[1]);
			
			if (pnt[0] == startPnt[0] && pnt[1] == startPnt[1]) {
				startPnt = null;
				g.closePath();
			}
		});
		if (startPnt) {
			g.lineTo(p[0], p[1]);
			g.closePath();
		}
        g.fill();
        g.stroke();
    }
});

L.TileLayer.maskCanvas = function(options) {
    var mc = new L.TileLayer.MaskCanvas(options);
    leafletVersion = parseInt(L.version.match(/\d{1,}\.(\d{1,})\.\d{1,}/)[1], 10);
    if (leafletVersion < 7) mc._createTile = mc._oldCreateTile;
    return mc;
};
