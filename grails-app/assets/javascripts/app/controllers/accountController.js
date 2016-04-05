app.controller('accountController', function ($scope, $location, $http, authService) {
    $scope.SearchPeople = function () {
        $http.get('/api/accounts')
            .success(function (data, status, headers, config) {
                $scope.status = status;

            }).error(function (data, status, headers, config) {
            $scope.status = status;

        });
    };
});