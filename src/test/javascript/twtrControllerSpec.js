describe('twtrController', function () {
    beforeEach(module('app'));

    var $httpBackend;
    var $scope;
    var $controller;
    var securityService;
    var tweetService;


    beforeEach(inject(function (_$httpBackend_, _$controller_ , _$rootScope_, _securityService_, _tweetService_) {
        // The injector unwraps the underscores (_) from around the parameter names when matching
        $httpBackend = _$httpBackend_;
        securityService = _securityService_;
        securityService.setCurrentUser({
            username: 'john',
            roles: ['the role'],
            token: "theToken",
        });
        securityService.setToken("theToken");
        securityService.setUsername("john");
        tweetService = _tweetService_;
        $scope = _$rootScope_.$new();
        $controller = _$controller_;

    }));


    describe("tweeting a message", function () {

        beforeEach(function () {
            spyOn(tweetService, 'doTweet');
            $controller('twtrController', {
                $scope: $scope,
                securityService: securityService,
                tweetService: tweetService
            });

        });

        it("handles the posting of a message", function () {
            $httpBackend.expectGET('/api/accounts/handle=john').respond(200,
                {
                    dateCreated: "2016-05-07T14:49:37Z",
                    email: "john@gmail.com",
                    followerCount: "1",
                    followingCount: "0",
                    handle: "john",
                    id: 2,
                    lastUpdated: "2016-05-07T14:49:37Z",
                    password: "thePassword!",
                    realName: "john"
                });

            $httpBackend.expectGET('/api/accounts/2/messages').respond(200, [{}]);

            /*$httpBackend.expectPOST('/api/accounts/2/messages', {
                content: "twitter message content",
                account: 2
            }).respond(201,
                {
                    class: "twtr.Message",
                    id: 109,
                    account: {
                        class: "twtr.Account",
                        id: 1
                    },
                    content: "dsf",
                    dateCreated: "2016-05-07T15:19:11Z"
                });
        */

            expect($scope).toBeDefined();
            expect($scope.currentUser).toBeDefined();

            $scope.doTweet();
            $httpBackend.flush();
            expect(tweetService.doTweet).toHaveBeenCalledTimes(1);

        });
    });
});

