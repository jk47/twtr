describe('twtrController', function () {
    beforeEach(module('app'));

    var $controller;
    var $rootScope;
    var $scope;

    var currentUser = undefined;

    var securityService = {
        currentUser: function () {
            return currentUser;
        },

        logout: function () {
        }
    };

    beforeEach(inject(function (_$controller_, _$rootScope_) {
        // The injector unwraps the underscores (_) from around the parameter names when matching
        $controller = _$controller_;
        $rootScope = _$rootScope_;
        $scope = {};
    }));

    describe("true", function()
    {
        it("Should be true", function(){
            expect(true).toBeTruthy()

        });

    });

});