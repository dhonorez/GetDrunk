
/** Main Controller **/
var MainCtrl = function($scope, queryApi, $log) {

	var drawingManager;
    var selectedShape;
    var colors = ['#1E90FF', '#FF1493', '#32CD32', '#FF8C00', '#4B0082'];
    var selectedColor;
    var colorButtons = {};
    var map = null;
    var markersArray = [];
    var lastInfoWindow= null;
    
    
    var dummyResponse = { businesses: [			{"name":"Nutrex NV","coordinate":{"lat":51.2484987,"lon":4.81255115},"headings":["Veevoeder - Fabr. & Grooth."],"website":null,"phone":"014 88 31 11","address":null},
                                    			{"name":"Sterkens Alarmsystemen NV","coordinate":{"lat":51.301490475,"lon":4.746795425},"headings":["Bewaking - Apparaten & systemen","Alarmsystemen - Installateurs"],"website":null,"phone":"03 311 68 72","address":null},
                                    			{"name":"Boelieboe BVBA","coordinate":{"lat":51.250697523,"lon":4.815884473},"headings":["Kinderdagverblijven & -oppasdiensten"],"website":null,"phone":"0476 86 99 90","address":null},
                                    			{"name":"Batisol BVBA","coordinate":{"lat":51.268576489,"lon":4.794347117},"headings":["Hernieuwbare energie"],"website":null,"phone":null,"address":null},
                                    			{"name":"Medi 8 EBVBA","coordinate":{"lat":51.268205713,"lon":4.712585694},"headings":["Verzekeringsmakelaars"],"website":null,"phone":"03 311 59 42","address":null},
                                    			{"name":"Michiels Luc BVBA","coordinate":{"lat":51.27226605,"lon":4.68814075},"headings":["Osteopaten"],"website":null,"phone":"03 309 45 85","address":null},
                                    			{"name":"Super Jaro BVBA","coordinate":{"lat":51.302113363,"lon":4.733273388},"headings":["Supermarkten & warenhuizen"],"website":null,"phone":"03 312 11 06","address":null},
                                    			{"name":"Made by Greta-Fotografie en Design","coordinate":{"lat":51.264845717,"lon":4.788350158},"headings":["Fotografie - Reportages"],"website":null,"phone":"03 322 50 29","address":null},
                                    			{"name":"Brignola G","coordinate":{"lat":51.238012108,"lon":4.721091221},"headings":["Tandartsen"],"website":null,"phone":"03 464 08 94","address":null},
                                    			{"name":"De Roeck L","coordinate":{"lat":51.266100524,"lon":4.721289334},"headings":["Boekhouders & fiscalisten"],"website":null,"phone":null,"address":null},
                                    			{"name":"Van Roy Evy","coordinate":{"lat":51.307045525,"lon":4.742804613},"headings":["Groenten & fruit - Kleinh."],"website":null,"phone":null,"address":null},
                                    			{"name":"Central","coordinate":{"lat":51.300508319,"lon":4.7335399},"headings":["Cafés"],"website":null,"phone":"03 312 01 25","address":null},
                                    			{"name":"Schrauwen Guy","coordinate":{"lat":51.288792279,"lon":4.797356847},"headings":["Aannemers - Grondwerken","Containers voor stort & afval (Verhuur & vervoer van)"],"website":null,"phone":"014 71 68 74","address":null},
                                    			{"name":"Van Den Heuvel","coordinate":{"lat":51.31157835,"lon":4.7552378},"headings":["Veefokkerijen"],"website":null,"phone":"03 321 70 01","address":null},
                                    			{"name":"Ver H.A.G.A. San BVBA","coordinate":{"lat":51.24836105,"lon":4.8236302},"headings":["Tankcontrole","Centrale verwarming (Installatie van)"],"website":null,"phone":"014 88 08 13","address":null},
                                    			{"name":"Krijnen Keukens BVBA","coordinate":{"lat":51.30614605,"lon":4.736871461},"headings":["Keukens"],"website":null,"phone":"03 312 45 62","address":null},
                                    ]};
	
	//initialize when map is loaded
    $scope.onMapIdle = function() {
        initialize();
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
    			coordinates.push( {lat:southWest.lat(), lon:southWest.lng()});
    			coordinates.push( {lat:nordEast.lat(), lon:southWest.lng()});
    			coordinates.push( {lat:southWest.lat(), lon:nordEast.lng()});
    		}
    	}
    	return coordinates;
    }
	
	$scope.search = function() {
		if (selectedShape) {
			// setup query
			var query = { category:'', maxResults: 300, openNow: false, coordinates: getCoordinatesFromSelectedShape()};
			$log.info(query);
			// calculate boundaries (used to center the map when results come in)
			var bounds = new google.maps.LatLngBounds(); 
			query.coordinates.forEach(function(p) {
				bounds.extend(new google.maps.LatLng(p.lat, p.lon));
			});
			
			clearMarkers();
			
			// post query						
			//queryApi.save(query, function(res){
			res = dummyResponse;
			    $log.info('RESPONSE:');
				$log.info(res);
				map.setCenter(bounds.getCenter());
				map.fitBounds(bounds);
				for (var i=0; i<res.businesses.length;i++){
					var business = res.businesses[i];
					$log.info(res.businesses[i]);
					var marker = new google.maps.Marker({
					    position: new google.maps.LatLng(business.coordinate.lat, business.coordinate.lon),
					    map: map,
					    title: business.name
					});
					markersArray.push(marker );
					var infowindow = new google.maps.InfoWindow({
					    content: 'name: '+business.name
					});
					makeInfoWindowEvent(map, infowindow, marker);
				}
			//});
		}
	};
	
	
	function makeInfoWindowEvent(map, infowindow, marker) {
		google.maps.event.addListener(marker, 'click', function() {
			if (lastInfoWindow){
				lastInfoWindow.close();
			}
			infowindow.open(map, marker);
			lastInfoWindow = infowindow;
		});
	}
	
    function clearSelection() {
        if (selectedShape) {
          selectedShape.setEditable(false);
          selectedShape = null;
        }
    }

    function setSelection(shape) {
        clearSelection();
        selectedShape = shape;
        shape.setEditable(true);
        selectColor(shape.get('fillColor') || shape.get('strokeColor'));
    };

    function deleteSelectedShape() {
        if (selectedShape) {
          selectedShape.setMap(null);
        }
    };
      
    function clearMarkers(){
      	for (var i = 0; i < markersArray.length; i++ ) {
      	    markersArray[i].setMap(null);
      	  }
      	  markersArray = [];
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
    }

    function setSelectedShapeColor(color) {
      if (selectedShape) {
        if (selectedShape.type == google.maps.drawing.OverlayType.POLYLINE) {
          selectedShape.set('strokeColor', color);
        } else {
          selectedShape.set('fillColor', color);
        }
      }
    }

    function makeColorButton(color) {
      var button = document.createElement('span');
      button.className = 'color-button';
      button.style.backgroundColor = color;
      google.maps.event.addDomListener(button, 'click', function() {
        selectColor(color);
        setSelectedShapeColor(color);
      });

      return button;
    }

    function buildColorPalette() {
       var colorPalette = document.getElementById('color-palette');
       for (var i = 0; i < colors.length; ++i) {
         var currColor = colors[i];
         var colorButton = makeColorButton(currColor);
         colorPalette.appendChild(colorButton);
         colorButtons[currColor] = colorButton;
       }
       selectColor(colors[0]);
    }

    function initialize() {
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
