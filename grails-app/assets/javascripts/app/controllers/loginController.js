app.controller('loginController', function($scope, $location, securityService) {

    $scope.loginAttempt = {};

    $scope.doLogin = function() {
        securityService
            .login($scope.loginAttempt.username, $scope.loginAttempt.password)
            .finally(function(result){
                var currentUser = securityService.currentUser();
                if (currentUser) {
                    delete $scope.error;
                    $location.path('/home');
                } else {
                    $scope.error = 'Invalid login';
                }
            });
    };

});