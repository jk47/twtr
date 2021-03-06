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
                templateUrl: '/app/details.html',
                controller: 'detailController'
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
                redirectTo: '/login'
            })
    })

    // Protect all routes other than login
    .run(function ($rootScope, $location, securityService) {
        $rootScope.$on('$routeChangeStart', function (event, next) {
            // route to home view even though login page has been requested because
            // a user is currently logged in
            if (next.$$route.originalPath == '/login'){
                if (securityService.currentUser()) {
                    $location.path('/home');
                }
            }
            else if (next.$$route.originalPath != '/login') {
                if (!securityService.currentUser()) {
                    $location.path('/login');
                }
            }
        });
    });
