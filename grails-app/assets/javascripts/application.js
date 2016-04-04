// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= encoding UTF-8
//= require jquery-2.2.0.js
//= require ../bower/bootstrap/bootstrap.js
//= require ../bower/angular/angular.js
//= require angular-route/angular-route
//= require_self
//= require_tree app

// Create the angular application called 'app'
var app = angular.module('myApp', []);

// Define a controller called 'welcomeController'
app.controller('twtrController', function ($scope, $http) {
    $scope.formInfo = {};

    $scope.SearchPeople = function () {
        $http.get('/api/accounts')
            .success(function (data, status, headers, config) {
                $scope.accounts = status;

            }).error(function (data, status, headers, config) {
                $scope.accounts = status;

        });
    };
});