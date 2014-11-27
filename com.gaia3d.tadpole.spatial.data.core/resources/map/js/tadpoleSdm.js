/*
 * define java browfunction code
 */
var editorService = {
	/** save option */
	SAVE_OPTION			: "5"
};

$(document).ready(onLoad);

var map;
var layerTadpole;
var layerTadpoleClick;

var options = {
		autoZoom: true,
		displayType: "normal"
}

options.canvasOptions = 
	{
		BasePointRadius: 3,
		BaseLineColor: 'rgba(0, 0, 255, 0.2)',
		BaseLineWidth: 2,
		BaseFillColor: 'rgba(0, 0, 255, 0.2)',
		SelectedPointRadius: 3,
		SelectedLineColor: 'rgba(0, 0, 255, 0.2)',
		SelectedLineWidth: 2,
		SelectedFillColor: 'rgba(0, 0, 255, 0.2)'
	};

/**
 * save option
 * 
 * @param options
 */
function saveOption(options) {
	try {
		TadpoleBrowserHandler(editorService.SAVE_OPTION, options);
    } catch(e) {
		console.log(e);
	}
}

options.heatmapOptions = {
		  // radius should be small ONLY if scaleRadius is true (or small radius is intended)
		  // if scaleRadius is false it will be the constant radius used in pixels
		  "radius": 0.01,
		  "maxOpacity": .8, 
		  // scales the radius based on map zoom
		  "scaleRadius": true, 
		  // if set to false the heatmap uses the global maximum for colorization
		  // if activated: uses the data maximum within the current map boundaries 
		  //   (there will always be a red spot with useLocalExtremas true)
		  "useLocalExtrema": true
		};


/*
 * initial event
 */
function onLoad() {
	/* 지도의 center를 맞춘다 */
	map = L.map('map').setView([37.55, 127.07], 3);

	/* Base Layer */
	L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
		maxZoom: 18,
		attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
			'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
			'Imagery © <a href="http://mapbox.com">Mapbox</a>',
		id: 'examples.map-20v6611k'
	}).addTo(map);	

	switch (options.displayType) {
	case "normal":
		/* Normal Layer */
		layerTadpole = L.geoJson.canvas();
		layerTadpole.setOptions(options.canvasOptions);
		layerTadpole.addTo(map);
		break;
	case "heatmap":
		/* heatmap Layer */
		layerTadpole = L.geoJson.heatmap(null, options.heatmapOptions);
		layerTadpole.addTo(map);
		break;
	case "cluster":
		/* point cluster */
		layerTadpole = L.geoJson.cluster(null, null);
		layerTadpole.addTo(map);
		break;
	}
	
	
	// sample data
	if( typeof(sampleData) != 'undefined' ) {
		layerTadpole.addData(sampleData);
	}
	
	/* Clicked Object Layer */
	var myStyle = {
		    "color": "#ff7800", //txtColor,
		    "weight": 5,
		    "opacity": 0.65
		};
	layerTadpoleClick = L.geoJson(null,{
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
* @param txtUserOption initialize user options
*/
function drawingMapInit(txtGeoJSON, txtUserOption) {
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
