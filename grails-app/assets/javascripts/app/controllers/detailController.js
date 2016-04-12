app.controller('detailController', function ($scope, $location, $http, securityService) {
    $scope.auth = {};
    $scope.auth.token = securityService.getToken();
    $scope.username = securityService.getUsername();

    // get the handle that the page should show
    var params = $location.search();
    if(!params['handle']){
        $scope.handle = $scope.username;
    }
    else {
        $scope.handle = params['handle'];
    }

    $scope.getAccount = function() {
        $http.get('/api/accounts/handle=' + $scope.handle,{headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function(data){
                $scope.account = data;
                $scope.getTweets();

            })
            .error(function (error){
                alert("get account error");
            })

    };

    $scope.getTweets = function() {
        $http.get('/api/accounts/' + $scope.id + '/messages', {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function(data){
                $scope.tweets = data;
            })
            .error(function (error){
                alert("get tweets error");
            })
            .finally( function(){
                // not sure if i need this
            })
    };

    $scope.getFollowers = function() {};

    $scope.isCurrentUser = function() {};

    $scope.startFollowing = function() {};

    $scope.updateUserDetails = function() {};

    
});
