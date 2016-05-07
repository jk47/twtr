app.controller('twtrController', function ($scope, $location, $http, securityService, tweetService) {
    $scope.auth = {};
    $scope.auth.token = securityService.getToken();
    $scope.username = securityService.getUsername();
    $scope.currentUser = securityService.getCurrentUser();

    $http.get('/api/accounts/handle='+$scope.currentUser.username, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
        .success(function (data){
            $scope.currentId = data.id;
            $scope.account = data;

            $http.get('/api/accounts/'+data.id  + '/messages', {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
                .success(function (data){
                    $scope.messages = data;
                });
        });

    $scope.doLogout = function() {
        securityService.logout();
    };

    $scope.doClearTweet = function() {
        $scope.tweetText = null;
    };

    $scope.doCloseAlert = function() {
        $scope.alert = null;
    };

    $scope.doMessageSearch = function() {
        $http.get('/api/messages/search?term=' + $scope.searchArgs.text, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function (data){
                $scope.searchResults = data;
            })
            .error(function (error){
                alert("search error");
            });
    };

    $scope.doTweet = function(){
        var messageContent =  $scope.tweetText;
        var messageAccount = $scope.currentId;
        var token = $scope.auth.token.toString();
        var currentId = $scope.currentId;
        tweetService.doTweet(messageContent, messageAccount, token, currentId);
    }
    
});