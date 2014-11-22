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
	layerTadpole = L.geoJson.canvas();
//	layerTadpole = L.geoJson();
	if( typeof(sampleData) != 'undefined' ) {
		layerTadpole.addData(sampleData);
	}
	
//	layerTadpole = L.TileLayer.maskCanvas();
//	if( typeof(sampleData) != 'undefined' ) {
//		layerTadpole.setData(sampleData);
//		map.fitBounds(layerTadpole.bounds);
//	}
	layerTadpole.addTo(map);
	
	var myStyle = {
		    "color": "#ff7800", //txtColor,
		    "weight": 5,
		    "opacity": 0.65
		};
	var geojsonFeature = { "type": "Feature",   "geometry": {"type":"MultiPolygon","coordinates":[0, 0]} };
	layerTadpoleClick = L.geoJson(geojsonFeature,{
		style: myStyle
	}).addTo(map);
}

/*
* clear map
*/
function clearAllLayersMap() {
	try {
//		layerTadpole.initialize();
		layerTadpole.clearLayers();
		layerTadpoleClick.clearLayers();
	} catch(err) {
		console.log("Rise exception(clearAllLayersMap function) : " + err);
	}
};

/*
* clear selected object
*/
function clearClickedLayersMap() {
	try {
		layerTadpoleClick.clearLayers();
	} catch(err) {
		console.log("Rise exception(cleareClickedLayersMap function) : " + err);
	}
};


/**
* Initialize drawing map 
*
* @param geoJSON initial map data
* @param txtColor initialize user click color
*/
function drawingMapInit(txtGeoJSON, txtColor) {
	try {
		/* console.log("==> color : " + color);
		console.log("==> geojsonFeature: \n" + txtGeoJSON ); */
		
		/* http://stackoverflow.com/questions/25216165/put-a-geojson-on-a-leaflet-map-invalid-geojson-object-throw-new-errorinvalid */
		var geoJSON = jQuery.parseJSON(txtGeoJSON);
		layerTadpole.addData(geoJSON);
//		layerTadpole.setData(L.geoJson(geoJSON));
		
		/* first data set center */
		map.fitBounds(layerTadpole.getBounds());
//		map.fitBounds(layerTadpole.bounds);
	} catch(err) {
		console.log(err);
	}
};

/**
* add map data
*
* @param geoJSON initial map data
*/
function drawMapAddData(txtGeoJSON) {
	try {
		var geoJSON = jQuery.parseJSON(txtGeoJSON);
		layerTadpole.addData(geoJSON);
		map.fitBounds(layerTadpole.getBounds());
//		layerTadpole.addData(L.geoJson(geoJSON));
//		map.fitBounds(layerTadpole.bounds);
	} catch(err) {
		console.log(err);
	}
};

/**
* click event
*/
function onClickPoint(txtGeoJSON) {
	try {
		/* console.log("==> geojsonFeature: \n" + geoJSON ); */
		var geoJSON = jQuery.parseJSON(txtGeoJSON);
		layerTadpoleClick.addData(geoJSON);
		bounds = L.geoJson(geoJSON).getBounds();
		map.fitBounds(bounds);
		if (bounds.min == bounds.max) { // 점인 경우 약간 축소 처리
			map.setZoom(map.getMaxZoom() - 2);
		}
	} catch(err) {
		console.log(err);
	}
};
