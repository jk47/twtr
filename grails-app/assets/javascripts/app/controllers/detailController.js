app.controller('detailController', function ($scope, $location, $http, securityService) {
    $scope.auth = {};
    $scope.auth.token = securityService.getToken();
    $scope.username = securityService.getUsername();
    $scope.currentUser = securityService.getCurrentUser();
    $http.get('/api/accounts/handle='+$scope.currentUser.username, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
        .success(function (data){
            $scope.currentId = data.id;
        })

    // get the handle that the page should show
    var params = $location.search();
    if(!params['handle']){
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
            .error(function (error){
                alert(error.toString());
            })

    };

    $scope.getTweets = function() {
        $http.get('/api/accounts/' + $scope.account.id + '/messages', {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
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

    $scope.isFollower = function() {
        $http.get('/api/accounts/' + $scope.currentId + '/followers', {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function (data){
                var isAFollower = false;
                for (var i = 0; i<data.length; i++){
                    alert("detailHanle: " + $scope.detailHandle );
                    alert("data: " + data[i].handle);
                    if (data[i].handle == $scope.detailHandle){
                        isAFollower = true;
                    }
                }
                $scope.isAFollower = isAFollower;
            })
            .error( function(error){
                alert(error);
            })

    };

    $scope.isCurrentUser = function() {
        $scope.myOwnDetails = ($scope.username == $scope.account.handle);
    };

    $scope.startFollowing = function() {
        $http.get('/api/accounts/' + $scope.currentId + '/follow/' + $scope.account.id, {headers: {'X-Auth-Token': $scope.auth.token.toString()}})
            .success(function (data){
                $scope.getAccount();//refresh the current account
            })
            .error(function (error){
                alert(error);
            })
    };

    $scope.updateUserDetails = function() {
        var updates = new Object();
        updates.handle = $scope.newHandle;
        updates.newEmailAddress = $scope.newEmail;
        var jsonbody = JSON.stringify(updates);
        //TO DO: make new endpoint for editing the user details
        
        
    };


    
});
