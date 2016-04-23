app.controller('twtrController', function ($scope, $location, $http, securityService) {
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

    $scope.doMessageSearch = function() {
        $http.get('/api/messages/search?term=' + $scope.searchArgs.text, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function (data){
                $scope.searchResults = data;
            })
            .error(function (error){
                alert("search error");
            })
    };
    
    $scope.doTweet = function() {
        var messageDetails = new Object();
        messageDetails.content = $scope.tweetArgs.text;
        messageDetails.account = $scope.currentId;
        var jsonBody = JSON.stringify(messageDetails);

        $http.post('/api/accounts/' + $scope.currentId + '/messages', jsonBody,
            {
                headers: {
                    'X-Auth-Token': $scope.auth.token.toString(),
                    'Content-Type': 'application/json'
                }

            })
            .success(function (data) {
                $scope.createdMessageResponse = data;
                $scope.success = true;
            })
            .error(function (error){
                alert("Tweet Error");
            })
    };
});