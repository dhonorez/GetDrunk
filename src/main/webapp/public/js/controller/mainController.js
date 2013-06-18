
/** Main Controller **/
var MainCtrl = function($scope, queryApi, googleMapService, shapeService, $log) {
	var category = '';
	/*
	 
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
                                    			{"name":"Central","coordinate":{"lat":51.300508319,"lon":4.7335399},"headings":["Caf�s"],"website":null,"phone":"03 312 01 25","address":null},
                                    			{"name":"Schrauwen Guy","coordinate":{"lat":51.288792279,"lon":4.797356847},"headings":["Aannemers - Grondwerken","Containers voor stort & afval (Verhuur & vervoer van)"],"website":null,"phone":"014 71 68 74","address":null},
                                    			{"name":"Van Den Heuvel","coordinate":{"lat":51.31157835,"lon":4.7552378},"headings":["Veefokkerijen"],"website":null,"phone":"03 321 70 01","address":null},
                                    			{"name":"Ver H.A.G.A. San BVBA","coordinate":{"lat":51.24836105,"lon":4.8236302},"headings":["Tankcontrole","Centrale verwarming (Installatie van)"],"website":null,"phone":"014 88 08 13","address":null},
                                    			{"name":"Krijnen Keukens BVBA","coordinate":{"lat":51.30614605,"lon":4.736871461},"headings":["Keukens"],"website":null,"phone":"03 312 45 62","address":null},
                                    ]};
     */
	
	//initialize when map is loaded
    $scope.onMapIdle = function() {
        googleMapService.initialize();
        
    };
    
    $scope.selectCategory = function(cat) {
    	$category = cat;
    };
	
	$scope.search = function() {
		if (googleMapService.isShapeSelected()) {
			// setup query
			var query = { 	category:$category, 
							maxResults: 5, 
							openNow: false, 
							coordinates: googleMapService.getCoordinatesFromSelectedShape()
						};
			$log.info(query);
			// calculate boundaries (used to center the map when results come in)
			var bounds = new google.maps.LatLngBounds(); 
			query.coordinates.forEach(function(p) {
				bounds.extend(new google.maps.LatLng(p.lat, p.lon));
			});
			
			googleMapService.clearMarkers();
			
			// post query						
			queryApi.save(query, function(res){
			//res = dummyResponse;
			    $log.info('RESPONSE:');
				$log.info(res);
				googleMapService.centerMapByBounds(bounds);
				$scope.businesses=res.businesses;
				for (var i=0; i<res.businesses.length;i++){
					var business = res.businesses[i];
					$log.info(res.businesses[i]);
					googleMapService.addMarker(business.coordinate.lat, 
											   business.coordinate.lon, 
											   business.name, 
											   '<b>'+business.name+'</b>');				
				}
			});
		}
	};
	
	function initialise(){
		$scope.shapes = shapeService.getShapes();
		console.log($scope.shapes);		
	}
	
	initialise();

};
