// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= encoding UTF-8
//= require jquery-2.1.3.js
//= require ../bower/bootstrap/bootstrap.js
//= require ../bower/angular/angular.js
//= require_self
//= require_tree app

// Create the angular application called 'app'
angular.module('app', []);

// Define a controller called 'welcomeController'
angular.module('app').controller('angularController', function($scope) {
    $scope.greeting = 'Hello Stranger'
});