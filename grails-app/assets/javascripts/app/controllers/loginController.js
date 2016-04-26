app.controller('loginController', function($scope, $location, securityService) {

    $scope.loginAttempt = {};

    $scope.isSignOutSuccessful = securityService.isSignOutSuccessful();

    $scope.doLogin = function() {
        delete $scope.isSignOutSuccessful

        securityService
            .login($scope.loginAttempt.username, $scope.loginAttempt.password)
            .finally(function(result){
                var currentUser = securityService.currentUser();
                if (currentUser) {
                    delete $scope.error;
                    $location.path('/home');
                } else {
                    $scope.alerts = [{msg: 'Invalid login', type: 'danger'}];
                    //$scope.error = 'Invalid login';
                }
            });
    };

});