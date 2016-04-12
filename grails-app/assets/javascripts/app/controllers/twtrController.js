app.controller('twtrController', function ($scope, $location, $http, securityService) {
    $scope.searchArgs = {}
    $scope.searchResults = [
        {
            "message" : "Message1",
            "handle" : "Handle1"
        },
        {
            "message" : "Message2",
            "handle" : "Handle2"
        },
        {
            "message" : "Message3",
            "handle" : "Handle3"
        },
    ]

    $scope.doLogout = function() {
        securityService.logout();
    };

    $scope.doMessageSearch = function() {
        var textToSearch = $scope.searchArgs.text;
    };
});