$(document).ready(onLoad);

var map;
var layerTadpole;
var layerTadpoleClick;

var options = 
	{
		autoZoom: true,
		BasePointRadius: 3,
		BaseLineColor: 'rgba(0, 0, 255, 0.2)',
		BaseLineWidth: 2,
		BaseFillColor: 'rgba(0, 0, 255, 0.2)',
		SelectedPointRadius: 3,
		SelectedLineColor: 'rgba(0, 0, 255, 0.2)',
		SelectedLineWidth: 2,
		SelectedFillColor: 'rgba(0, 0, 255, 0.2)'
	}


/*
 * initial event
 */
function onLoad() {
	/* 지도의 center를 맞춘다 */
	map = L.map('map').setView([37.55, 127.07], 3);

	/* 지도의 title을 표현한다 */
	L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
		maxZoom: 18,
		attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
			'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
			'Imagery © <a href="http://mapbox.com">Mapbox</a>',
		id: 'examples.map-20v6611k'
	}).addTo(map);	

	/** define layer */
//	layerTadpole = L.geoJson();
	layerTadpole = L.geoJson.canvas();
	if( typeof(sampleData) != 'undefined' ) {
		layerTadpole.addData(sampleData);
	}
	
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
		clearAllLayersMap();
		
		/* http://stackoverflow.com/questions/25216165/put-a-geojson-on-a-leaflet-map-invalid-geojson-object-throw-new-errorinvalid */
		var geoJSON = jQuery.parseJSON(txtGeoJSON);
		layerTadpole.addData(geoJSON);
		
		/* zoom to data bounds */
		if (options.autoZoom)
			map.fitBounds(layerTadpole.getBounds());
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
		if (options.autoZoom)
			map.fitBounds(layerTadpole.getBounds());
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
//		bounds = layerTadpole._calcMbr(geoJSON.features[0].geometry.coordinates);
		map.fitBounds(bounds);
		if (bounds.getSouthWest() == bounds.getNorthEast()) { // 점인 경우 약간 축소 처리
			map.setZoom(map.getMaxZoom() - 2);
		}
	} catch(err) {
		console.log(err);
	}
};
