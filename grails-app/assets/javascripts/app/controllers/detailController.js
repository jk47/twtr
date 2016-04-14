app.controller('detailController', function ($scope, $location, $http, securityService) {
    $scope.auth = {};
    $scope.auth.token = securityService.getToken();
    $scope.username = securityService.getUsername();
    $scope.currentUser = securityService.getCurrentUser();
    $http.get('/api/accounts/handle=' + $scope.currentUser.username, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
        .success(function (data) {
            $scope.currentId = data.id;
        })

    // get the handle that the page should show
    var params = $location.search();
    if (!params['handle']) {
        $scope.handle = $scope.username;
    }
    else {
        $scope.handle = params['handle'];
    }


    $scope.getAccount = function() {
        $http.get('/api/accounts/handle=' + $scope.handle ,{headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function(data){
                $scope.account = data;
                $scope.detailHandle = $scope.account.handle;
                $scope.getTweets();
                $scope.isCurrentUser();
                $scope.isFollower();
            })
            .error(function (error) {
                alert(error.toString());
            })

    };

    $scope.getTweets = function () {
        $http.get('/api/accounts/' + $scope.account.id + '/messages', {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function (data) {
                $scope.tweets = data;
            })
            .error(function (error) {
                alert("get tweets error");
            })
            .finally(function () {
                // not sure if i need this
            })
    };


    $scope.isFollower = function () {
        $http.get('/api/accounts/' + $scope.account.id + '/followers', {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function (data) {
                var isAFollower = false;
                for (var i = 0; i < data.length; i++) {
                    if (data[i].handle == $scope.handle) {
                        isAFollower = true;
                    }
                }
                $scope.isAFollower = isAFollower;
            })
            .error(function (error) {
                alert(error);
            })

    };

    $scope.isCurrentUser = function () {
        $scope.myOwnDetails = ($scope.username == $scope.account.handle);
    };

    $scope.startFollowing = function () {
        $http.get('/api/accounts/' + $scope.currentId + '/follow/' + $scope.account.id, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function (data) {
                $scope.getAccount();//refresh the current account
            })
            .error(function (error) {
                alert(error);
            })
    };

    $scope.updateUserDetails = function () {
        delete $scope.error
        delete $scope.success

        var newUserDetails = new Object();
        newUserDetails.email = $scope.account.email
        newUserDetails.realName = $scope.account.realName
        var jsonBody = JSON.stringify(newUserDetails)

        $http.put('/api/accounts/' + $scope.currentId + '/', jsonBody,
            {
                headers: {
                    'X-Auth-Token': $scope.auth.token.toString(),
                    'Content-Type': 'application/json'
                }
            })
            .success(function (data, status, headers) {
                $scope.success = 'Update successful.'
            })
            .error(function (error) {
                $scope.error = error;
            })

    };


});
