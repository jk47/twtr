app.controller('twtrController', function ($scope, $location, $http, securityService) {
    $scope.test = "test";


    $scope.doLogout = function() {
        securityService.logout();
    };

});