var map = null;
var mapOptions = {
    zoom: 4,
    mapTypeId: google.maps.MapTypeId.ROADMAP
}
// Array containing array of points corresponding to each providers
var pt_arrays = [];

// Array containing heatmap for each provider
var heatmaps = [];

// used in the handleLocation function to resize the zoom 
var infowindow = new google.maps.InfoWindow();
var bounds = new google.maps.LatLngBounds();
var result;

// variables for testing purposes
var DOMResult;

// Add a marker to the map and push to the array.
function addMarker(location, description, isVisible){
    if(map == null) {
        map = new google.maps.Map(document.getElementById('map-canvas'),
            mapOptions);
    }

    var marker = new google.maps.Marker({
        position: location,
        map: map,
        visible: isVisible
    });
    
    bounds.extend(marker.position);
    
    //Add description to each marker on the map
    google.maps.event.addListener(marker, 'click', (function (marker) {
        return function () {
            infowindow.setContent(description);
            infowindow.open(map, marker);
        }
    })(marker));
}

function handleLocation(xmlDoc){
    
    var parser = new DOMParser();
    var s = new XMLSerializer();
    
    path = "//Location"
    
    var _name = xmlDoc.evaluate(path, xmlDoc, null, XPathResult.ANY_TYPE, null);
    result = _name.iterateNext();
    var _latitude;
    var _longitude;
    var _title;
    
    //create a loop in which we will call addMarker()
    while(result){
        var DOMResult = parser.parseFromString(s.serializeToString(result),"application/xml");
        
        _latitude   = DOMResult.getElementsByTagName("latitude")[0].childNodes[0].nodeValue;
        _longitude  = DOMResult.getElementsByTagName("longitude")[0].childNodes[0].nodeValue;
        _title      = DOMResult.getElementsByTagName("title")[0].childNodes[0].nodeValue;
        
        _title = "<b>Location:</b> "+_title+"<br />"
        +"<b>Latitude:</b> "+_latitude+"<br />"
        +"<b>Longitude:</b> "+_longitude;
        addMarker(new google.maps.LatLng(_latitude,_longitude), _title, true);
        result = _name.iterateNext()
    }
}

function getCircle(magnitude) {
    return {
        path: google.maps.SymbolPath.CIRCLE,
        fillColor: 'red',
        fillOpacity: .2,
        scale: Math.abs(magnitude) /Math.PI,
        strokeColor: 'white',
        strokeWeight: .5
    };
}

function handleMeasurements(xmlDoc){
    path = "//GeoMeasurement";
    
    // used to cast the node into a DOM element
    var parser = new DOMParser();
    var s = new XMLSerializer();
    
    // counter used to keep track of heatmaps length
    var i=0;
    
    // retuns all measurements nodes
    var _name = xmlDoc.evaluate(path, xmlDoc, null, XPathResult.ANY_TYPE, null);
    result = _name.iterateNext();
    
    // array in which all mesures of a single provider will be stored
    var pointArray = [];

    //create a loop in which we will call addMarker()
    while(result){
        
        // cast result into String then into an xml document
        DOMResult = parser.parseFromString(s.serializeToString(result),"application/xml");
        
        //var _timeStamp      = DOMResult.getElementsByTagName("timestamp")[0].childNodes[0].nodeValue;
        var _value          = DOMResult.getElementsByTagName("value")[0].childNodes[0].nodeValue;
        var _unit           = DOMResult.getElementsByTagName("unit")[0].childNodes[0].nodeValue;
        var _latitude       = DOMResult.getElementsByTagName("latitude")[0].childNodes[0].nodeValue;
        var _longitude      = DOMResult.getElementsByTagName("longitude")[0].childNodes[0].nodeValue;
        var _provider       = DOMResult.getElementsByTagName("provider")[0].childNodes[0].nodeValue;

        
        if(pt_arrays[_provider] == undefined){ // The provider does not have values
            
            // store the name of the provider in the arrays of points array
            pt_arrays[i] = _provider;
            i++;
        
            //created weighted measurment
            var weightedLoc ={
                location: new google.maps.LatLng(_latitude, _longitude),
                weight: Math.pow(_value, 2)
            };
            
            pointArray.push(weightedLoc);
            
            // set the property of heatmaps as an array of values
            pt_arrays[_provider] = pointArray;
            pointArray = [];
        } else {  // The provider already has values
            
            weightedLoc = {
                location: new google.maps.LatLng(_latitude, _longitude),
                weight: Math.pow(_value, 2)
            };
            
            pt_arrays[_provider].push(weightedLoc);
        }

        var _title = "<b>Provider:</b> " + _provider+"<br />"
        + "<b>Value:</b> "+_value + " " + _unit;
    
        //add a marker for every measurement
        addMarker(new google.maps.LatLng(_latitude,_longitude), _title, false);

        result = _name.iterateNext()
    }
    
    createHeatMap(i);
}

function toggleHeatmap(indice){
    heatmaps[indice].setMap(heatmaps[indice].getMap() ? null : map);
}

function createHeatMap(_num){
    for ( var i = 0 ; i < _num ; i++ ) {
        
        // create a corresponding heatmap
        var heatmap = new google.maps.visualization.HeatmapLayer({
            data: pt_arrays[pt_arrays[i]],
			dissipating: false
        });
        
        // store the heatmap in the table
        heatmaps[i]=heatmap;
        
        heatmaps[i].setMap(map);
        
        var button = document.createElement("input");
        button.type = "button";
        button.id = pt_arrays[i];
        button.onclick = function(arg) {
            return function() {
                toggleHeatmap(arg);
            }
        }(i);
        ;
        button.name = pt_arrays[i];
        button.value= pt_arrays[i];
        console.log(i);
            
        var foo = document.getElementById('fooBar');
        //Append the element in page (in span).  
        foo.appendChild(button);
    }
}

function parseXML(){
    if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
    } else {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    //SIYAJ atseka lah
    //----------------------------------------------------------------
    xmlhttp.open("GET","GetResults",false);
    xmlhttp.send();
    var xmlDoc=xmlhttp.responseXML;
    
    handleLocation(xmlDoc);
    handleMeasurements(xmlDoc);
    
    if(!bounds.isEmpty()){
        map.fitBounds(bounds);
    }
}

function initMap(){
    // Parse XML and retreive locations
    parseXML();
}

//google.maps.event.addDomListener(window, 'load', initMap);