
/* Services */


app.factory('googleMapService', function($q, $rootScope) {
    
	var drawingManager;
    var selectedShape;
    var colors = ['#1E90FF', '#FF1493', '#32CD32', '#FF8C00', '#4B0082'];
    var selectedColor;
    var colorButtons = {};
    var map = null;
    var markersArray = [];
    var lastInfoWindow= null;
    
    function setSelection(shape){
    	    clearSelection();
            selectedShape = shape;
            shape.setEditable(true);
            selectColor(shape.get('fillColor') || shape.get('strokeColor'));
    };
    
    function deleteSelectedShape() {
        if (selectedShape) {
        	selectedShape.setMap(null);
        }
    }
    
    function clearSelection() {
        if (selectedShape) {
            selectedShape.setEditable(false);
            selectedShape = null;
        }
	}
    
    function selectColor(color) {
        selectedColor = color;
        for (var i = 0; i < colors.length; ++i) {
          var currColor = colors[i];
          colorButtons[currColor].style.border = currColor == color ? '2px solid #789' : '2px solid #fff';
        }

        // Retrieves the current options from the drawing manager and replaces the
        // stroke or fill color as appropriate.
        var polylineOptions = drawingManager.get('polylineOptions');
        polylineOptions.strokeColor = color;
        drawingManager.set('polylineOptions', polylineOptions);

        var rectangleOptions = drawingManager.get('rectangleOptions');
        rectangleOptions.fillColor = color;
        drawingManager.set('rectangleOptions', rectangleOptions);

        var circleOptions = drawingManager.get('circleOptions');
        circleOptions.fillColor = color;
        drawingManager.set('circleOptions', circleOptions);

        var polygonOptions = drawingManager.get('polygonOptions');
        polygonOptions.fillColor = color;
        drawingManager.set('polygonOptions', polygonOptions);
    };
    
    function buildColorPalette() {
        var colorPalette = document.getElementById('color-palette');
        for (var i = 0; i < colors.length; ++i) {
          var currColor = colors[i];
          var colorButton = makeColorButton(currColor);
          colorPalette.appendChild(colorButton);
          colorButtons[currColor] = colorButton;
        }
        selectColor(colors[0]);
     };
     
     function setSelectedShapeColor(color) {
         if (selectedShape) {
           if (selectedShape.type == google.maps.drawing.OverlayType.POLYLINE) {
           	selectedShape.set('strokeColor', color);
           } else {
           	selectedShape.set('fillColor', color);
           }
         }
       };
       
      function makeColorButton(color) {
    	  var button = document.createElement('span');
		  button.className = 'color-button';
		  button.style.backgroundColor = color;
		  google.maps.event.addDomListener(button, 'click', function() {
		 	  selectColor(color);
		 	  setSelectedShapeColor(color);
		  });
		
		  return button;
      };
      
      function makeInfoWindowEvent(map, infowindow, marker) {
  		google.maps.event.addListener(marker, 'click', function() {
  			if (lastInfoWindow){
  				lastInfoWindow.close();
  			}
  			infowindow.open(map, marker);
  			lastInfoWindow = infowindow;
  		});
      };
      
      function getCoordinatesFromSelectedShape(){
      	var coordinates = [];
    	if (selectedShape){
    		if (selectedShape.type == google.maps.drawing.OverlayType.POLYGON) {
    			var path = selectedShape.getPath();
    			path.forEach(function(p) {
    				coordinates.push({lat:p.lat(), lon:p.lng()});    			
    			});
    		} else if (selectedShape.type == google.maps.drawing.OverlayType.RECTANGLE) {
    			bounds = selectedShape.getBounds();
    			nordEast = bounds.getNorthEast();
    			southWest = bounds.getSouthWest();
    			coordinates.push( {lat:nordEast.lat(), lon:nordEast.lng()});
    			coordinates.push( {lat:nordEast.lat(), lon:southWest.lng()});
    			coordinates.push( {lat:southWest.lat(), lon:southWest.lng()});
    			coordinates.push( {lat:southWest.lat(), lon:nordEast.lng()});
    		}
    	}
    	return coordinates;
    }
      
    function drawShape(){
      var bermudaTriangle;

      var triangleCoords = [
        new google.maps.LatLng(25.774252, -80.190262),
        new google.maps.LatLng(18.466465, -66.118292),
        new google.maps.LatLng(32.321384, -64.75737),
        new google.maps.LatLng(25.774252, -80.190262)
      ];

      // Construct the polygon
      // Note that we don't specify an array or arrays, but instead just
      // a simple array of LatLngs in the paths property
      bermudaTriangle = new google.maps.Polygon({
        paths: triangleCoords,
        strokeColor: "#FF0000",
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: "#FF0000",
        fillOpacity: 0.35
      });

      bermudaTriangle.setMap(map);
    }
    
    return {
    	getMap: function(){
    		return map;
    	},
    	
    	isShapeSelected: function (){
    		return (selectedShape!=undefined && selectedShape!=null);
    	},
    	
    	getCoordinatesFromSelectedShape: function(){
        	return getCoordinatesFromSelectedShape();
        	
        },
    	
    	clearMarkers: function(){
          	for (var i = 0; i < markersArray.length; i++ ) {
          	    markersArray[i].setMap(null);
          	  }
          	  markersArray = [];
        },
        
        centerMapByBounds: function(bounds){
        	map.setCenter(bounds.getCenter());
			map.fitBounds(bounds);
        },
        
        addMarker: function(lat, lon, title, content){
        	var marker = new google.maps.Marker({
			    position: new google.maps.LatLng(lat, lon),
			    map: map,
			    title: title
			});
			markersArray.push(marker );
			var infowindow = new google.maps.InfoWindow({
			    content: content
			});
			makeInfoWindowEvent(map, infowindow, marker);
        },

        
        initialize: function() {
		  map = new google.maps.Map(document.getElementById('map'), {
		    zoom: 10,
		    center: new google.maps.LatLng(51.2, 4.40),
		    mapTypeId: google.maps.MapTypeId.ROADMAP,
		    disableDefaultUI: true,
		    zoomControl: true
		  });
		 
		  var polyOptions = {
		    strokeWeight: 0,
		    fillOpacity: 0.45,
		    editable: true
		  };
		  // Creates a drawing manager attached to the map that allows the user to draw
		  // markers, lines, and shapes.
		  drawingManager = new google.maps.drawing.DrawingManager({
			  position: google.maps.ControlPosition.TOP_CENTER,
			  drawingMode: google.maps.drawing.OverlayType.POLYGON,
			  drawingControlOptions: {
			      position: google.maps.ControlPosition.TOP_CENTER,
			      drawingModes: [
			        //google.maps.drawing.OverlayType.MARKER,
		        //google.maps.drawing.OverlayType.CIRCLE,
		        google.maps.drawing.OverlayType.POLYGON,
		        //google.maps.drawing.OverlayType.POLYLINE,
			        google.maps.drawing.OverlayType.RECTANGLE
			      ]
			  },
			  markerOptions: {
				  draggable: true
			  },
			  polylineOptions: {
				  editable: true
			  },
			  rectangleOptions: polyOptions,
			  circleOptions: polyOptions,
			  polygonOptions: polyOptions,
			  map: map
		  });
		
		  google.maps.event.addListener(drawingManager, 'overlaycomplete', function(e) {
		  if (e.type != google.maps.drawing.OverlayType.MARKER) {
		      // Switch back to non-drawing mode after drawing a shape.
		      drawingManager.setDrawingMode(null);
		
		      // Add an event listener that selects the newly-drawn shape when the user
		      // mouses down on it.
		      var newShape = e.overlay;
		      newShape.type = e.type;
		      google.maps.event.addListener(newShape, 'click', function() {
		            setSelection(newShape);
		            console.log('TEST');
		            console.log(JSON.stringify(getCoordinatesFromSelectedShape()));
		          });
		          setSelection(newShape);
		      }
		  });
		
		  // Clear the current selection when the drawing mode is changed, or when the
		  // map is clicked.
		  google.maps.event.addListener(drawingManager, 'drawingmode_changed', clearSelection);
		  google.maps.event.addListener(map, 'click', clearSelection);
		  google.maps.event.addDomListener(document.getElementById('delete-button'), 'click', deleteSelectedShape);
		  buildColorPalette();
           
        }       
    };
});
