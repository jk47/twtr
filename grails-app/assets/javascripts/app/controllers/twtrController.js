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
            })
    };
    
    var doTweet = function() {
        var messageDetails = new Object();
        messageDetails.content = $scope.tweetText;
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

                $http.get('/api/accounts/'+$scope.currentId   + '/messages', {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
                    .success(function (data){
                        $scope.messages = data;
                    });

                $scope.tweetText = null;

                $scope.alert = { type: 'success', msg: 'Message Posted!' };
            })
            .error(function (error){
                alert("Tweet Error");
            })
    };
});