angular.module('hello', [ 'ngRoute' ]).config(function($routeProvider,$httpProvider) {
	
	$httpProvider.interceptors.push('responseObserver');

	$routeProvider.when('/', {
		templateUrl : 'home.html',
		controller : 'home',
		controllerAs : 'controller'
	}).otherwise('/');

}).factory('responseObserver', function responseObserver($q, $window) {
    return {
        'responseError': function(errorResponse) {
            switch (errorResponse.status) {
            case 403:
  //              $window.location = './403.html';
            	  window.alert("Access is forbidden for you!!!");            	
                break;
            case 404:
  //              $window.location = './403.html';
            	  window.alert("The remote resource is not available!!!");            	
            	  break;
            case 500:
 //               $window.location = './500.html';
          	    window.alert("Something went wrong!!!");
                break;
            }
         //   return $q.reject(errorResponse);
        }
    };
}).controller('navigation',

function($rootScope, $http, $location, $route) {

	var self = this;

	self.tab = function(route) {
		return $route.current && route === $route.current.controller;
	};

	$http.get('user').then(function(response) {
		if (response.data.name) {
			$rootScope.authenticated = true;
		} else {
			$rootScope.authenticated = false;
		}
	}, function() {
		$rootScope.authenticated = false;
	});

	self.credentials = {};

	self.logout = function() {
		$http.post('logout', {}).finally(function() {
			$rootScope.authenticated = false;
			$location.path("/");
		});
	}

}).controller('home', function($http) {
	var self = this;
	$http.get('resource/').then(function successCallback(response) {
		self.greeting = response.data;
	}, function errorCallback(response) {
//		window.alert("Error");
	})
	  });
//}).controller('home', function($http) {
//	var self = this;
//	$http.get('resource/').then(function(response) {
//		self.greeting = response.data;
//	})
//});
