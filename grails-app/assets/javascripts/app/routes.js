app.config(function ($routeProvider) {

        $routeProvider
            .when('/login', {
                templateUrl: '/app/login.html',
                controller: 'loginController'
            })
            .when('/exit', {
                templateUrl: '/app/exit.html',
            })
            .when('/userDetail/:handle?', {
                templateUrl: '/app/userDetail.html',
                controller: 'userDetailController'
            })
            .when('/home/:handle?', {
                templateUrl: '/app/home.html',
                controller: 'twtrController'
            })
            .when('/feed', {
                templateUrl: '/app/feed.html',
                controller: 'feedController'
            })
            .when('/detail', {
                templateUrl: '/app/details.html',
                controller: 'detailController'
            })
            .otherwise({
                redirectTo: '/feed'
            })
    })

    // Protect all routes other than login
    .run(function ($rootScope, $location, securityService) {
        $rootScope.$on('$routeChangeStart', function (event, next) {
            if (next.$$route.originalPath != '/login') {
                if (!securityService.currentUser() && (next.$$route.originalPath != '/exit')) {
                    $location.path('/login');
                }
            }
        });
    });
