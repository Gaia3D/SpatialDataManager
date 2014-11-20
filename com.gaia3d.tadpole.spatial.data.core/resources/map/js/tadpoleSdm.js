var map;
var layerTadpole;
var layerTadpoleClick;

$(document).ready(onLoad);

/*
 * initial event
 */
function onLoad() {
	/* 지도의 center를 맞춘다 */
	map = L.map('map').setView([37.55, 127.07], 1);

	/* 지도의 title을 표현한다 */
	L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
		maxZoom: 18,
		attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
			'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
			'Imagery © <a href="http://mapbox.com">Mapbox</a>',
		id: 'examples.map-20v6611k'
	}).addTo(map);	

	/** define layer */
	// layerTadpole = L.geoJson();
	layerTadpole = L.TileLayer.maskCanvas();
	if( typeof(sampleData) != 'undefined' ) {
		layerTadpole.setData(sampleData);
		map.fitBounds(layerTadpole.bounds);
	}
	layerTadpole.addTo(map);
	
	layerTadpoleClick = L.geoJson();
}

/*
* clear map
*/
function clearAllLayersMap() {
	try {
		layerTadpole.clearLayers();
		layerTadpoleClick.clearLayers();
	} catch(err) {
		console.log("Rise exception(clearAllLayersMap function) : " + err);
	}
};

/*
* clear map
*/
function clearClickedLayersMap() {
	try {
		layerTadpoleClick.clearLayers();
	} catch(err) {
		console.log("Rise exception(cleareClickedLayersMap function) : " + err);
	}
};


/**
* drawing map
*
* @param geoJSON initial map data
* @param cole initialize user click color
*/
function drawingMap(txtGeoJSON, txtColor, strCenterX, strCenterY, strZoom) {
	try {
		/* console.log("==> color : " + color);
		console.log("==> geojsonFeature: \n" + txtGeoJSON ); */
		console.log(txtGeoJSON);
		
		/* http://stackoverflow.com/questions/25216165/put-a-geojson-on-a-leaflet-map-invalid-geojson-object-throw-new-errorinvalid */
		var geoJSON = jQuery.parseJSON(txtGeoJSON);
		// layerTadpole = L.geoJson(geoJSON).addTo(map);
		layerTadpole.setData(L.geoJson(geoJSON));
		
		/* first data set center */
		// map.setView([strCenterX, strCenterY], strZoom);
		map.fitBounds(layerTadpole.bounds);
		
		var myStyle = {
			    "color": txtColor,
			    "weight": 5,
			    "opacity": 0.65
			};
		var geojsonFeature = { "type": "Feature",   "geometry": {"type":"MultiPolygon","coordinates":[0, 0]} };
		layerTadpoleClick = L.geoJson(geojsonFeature,{
			style: myStyle
		}).addTo(map);
	} catch(err) {
		console.log(err);
	}
};

/**
* click event
*/
function onClickPoint(geoJSON) {
	try {
		/* console.log("==> geojsonFeature: \n" + geoJSON ); */
		layerTadpoleClick.addData(jQuery.parseJSON(geoJSON));
	} catch(err) {
		console.log(err);
	}
};

var loadData = function(url, layer) {
    $.getJSON(url).success(function(data) {
    	layer.setData(data);
        map.fitBounds(layer.bounds);
        map.addLayer(layer);
    }).error(function(err) {
        alert('An error occurred', err);
    });
};