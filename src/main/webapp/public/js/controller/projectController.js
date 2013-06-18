



/** Main Controller **/
var ProjectCtrl = function($scope, $location) {

	$scope.navClass = function (page) {
        var currentRoute = $location.path().substring(1) || 'home';
        return page === currentRoute ? 'active' : '';
    }; 
		
	
};
