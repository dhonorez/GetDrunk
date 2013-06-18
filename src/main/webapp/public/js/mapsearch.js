
var app = angular.module('mapsearch', [ 'ngResource','ui']).config(
		function($routeProvider) {
			$routeProvider.when('/', {
				controller : MainCtrl,
				templateUrl : 'partials/main.html'
			}).otherwise({
				redirectTo : '/'
			});
		});

/** NMI Reporter API - resource **/
app.factory('queryApi', function($resource) {
	//return $resource('http://localhost\\:4567/query');
	return $resource('http://192.168.0.163\\:4567/query');
	//return $resource('/nmi-reporter/api/run/feed_monitor/:connection/:query');
});




