/*
 * define java browfunction code
 */
var editorService = {
	/** save option */
	SAVE_OPTION			: "5"
};

$(document).ready(onLoad);

var map;
var resultLayer;
var normalLayer, heatmapLayer, clusterLayer;
var selectedLayer;

var options = {
		autoZoom: true,
		displayType: "normal"
}

options.canvasOptions = 
	{
		BasePointRadius: 3,
		BaseLineColor: 'rgba(0, 0, 255, 0.2)',
		BaseLineWidth: 2,
		BaseFillColor: 'rgba(0, 0, 255, 0.2)'
	};


options.heatmapOptions = {
	  // radius should be small ONLY if scaleRadius is true (or small radius is intended)
	  // if scaleRadius is false it will be the constant radius used in pixels
	  radius: 0.01,
	  maxOpacity: .8, 
	  // scales the radius based on map zoom
	  scaleRadius: true, 
	  // if set to false the heatmap uses the global maximum for colorization
	  // if activated: uses the data maximum within the current map boundaries 
	  //   (there will always be a red spot with useLocalExtremas true)
	  useLocalExtrema: true
	};

options.selectedOptions = {
	    color: "#ff7800", //txtColor,
	    weight: 5,
	    opacity: 0.65
	};

/**
 * save option
 * 
 * @param options
 */
function saveOption() {
	try {
		TadpoleBrowserHandler(editorService.SAVE_OPTION, JSON.stringify(options));
		console.log("==> options: \n" + JSON.stringify(options) );
    } catch(e) {
		console.log(e);
	}
}

/*
 * initial event
 */
function onLoad() {
	/* 지도의 center를 맞춘다 */
	map = L.map('map').setView([37.55, 127.07], 3);

	/* Base Layer */
	baseLayer = L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
		maxZoom: 18,
		attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
			'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
			'Imagery © <a href="http://mapbox.com">Mapbox</a>',
		id: 'examples.map-20v6611k'
	}).addTo(map);	
	
	normalLayer = L.geoJson.canvas(null, options.canvasOptions);
	heatmapLayer = L.geoJson.heatmap(null, options.heatmapOptions);
	clusterLayer = L.geoJson.cluster(null, null);

	switch (options.displayType) {
	case "normal":
		/* Normal Layer */
		resultLayer = normalLayer.addTo(map);
		break;
	case "heatmap":
		/* heatmap Layer */
		resultLayer = heatmapLayer.addTo(map);
		break;
	case "cluster":
		/* point cluster */
		resultLayer = clusterLayer.addTo(map);
		break;
	}
	
	// sample data
	if( typeof(sampleData) != 'undefined' ) {
		resultLayer.addData(sampleData);
	}
	
	/* Clicked Object Layer */
	selectedLayer = L.geoJson(null,{
		style: options.selectedOptions
	}).addTo(map);
	
	var resultMaps = {
	    "normal": normalLayer,
	    "heatmap": heatmapLayer,
	    "cluster": clusterLayer
	};

	var overlayMaps = null;
	
	L.control.sdmOption(resultMaps, options).addTo(map);
}

/*
* clear map
*/
function clearAllLayersMap() {
	try {
		resultLayer.clearLayers();
		selectedLayer.clearLayers();
	} catch(err) {
		console.log("Rise exception(clearAllLayersMap function) : " + err);
	}
};

/*
* clear selected object
*/
function clearClickedLayersMap() {
	try {
		selectedLayer.clearLayers();
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
		var newOptions = JSON.parse(txtUserOption);
		options = newOptions;
	} catch (err) {
		console.log(err);
	}
	
	try {
		clearAllLayersMap();
		
		/* http://stackoverflow.com/questions/25216165/put-a-geojson-on-a-leaflet-map-invalid-geojson-object-throw-new-errorinvalid */
		var geoJSON = jQuery.parseJSON(txtGeoJSON);
		resultLayer.addData(geoJSON);
		
		/* zoom to data bounds */
		if (options.autoZoom)
			map.fitBounds(resultLayer.getBounds());
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
		resultLayer.addData(geoJSON);
		if (options.autoZoom)
			map.fitBounds(resultLayer.getBounds());
	} catch(err) {
		console.log(err);
	}
};

/**
* click event
*/
function onClickPoint(txtGeoJSON, txtToopTip) {
	try {
		/* console.log("==> geojsonFeature: \n" + geoJSON ); */
		var geoJSON = jQuery.parseJSON(txtGeoJSON);
		selectedLayer.addData(geoJSON);
		bounds = L.geoJson(geoJSON).getBounds();
		map.fitBounds(bounds);
		if (bounds.getSouthWest() == bounds.getNorthEast()) { // 점인 경우 약간 축소 처리
			map.setZoom(map.getMaxZoom() - 2);
		}
		
		if(txtToopTip != "") selectedLayer.bindPopup(txtToopTip).openPopup();
	} catch(err) {
		console.log(err);
	}
};
