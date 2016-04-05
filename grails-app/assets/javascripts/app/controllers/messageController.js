app.controller('messageController', function ($scope, $location, $http, authService) {
    $scope.SearchMessages = function () {
        $http.get('/api/messages')
            .success(function (data, status, headers, config) {
                $scope.status = status;

            }).error(function (data, status, headers, config) {
            $scope.status = status;

        });
    };
});