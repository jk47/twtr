app.controller('twtrController', function ($scope, $location, $http, securityService) {
    $scope.auth = {};
    $scope.auth.token = securityService.getToken();
    $scope.username = securityService.getUsername();
    $scope.currentUser = securityService.getCurrentUser();
    $http.get('/api/accounts/handle='+$scope.currentUser.username, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
        .success(function (data){
            $scope.currentId = data.id;
        })

    $scope.doLogout = function() {
        securityService.logout();
    };

    $scope.doMessageSearch = function() {
        $http.get('/api/messages/search', {headers: {'X-Auth-Token': $scope.auth.token.toString(), 'term': $scope.searchArgs.text}})
            .success(function (data){
                $scope.searchResults = data;
            })
            .error(function (error){
                alert("search error");
            })
    };
});