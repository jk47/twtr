app.service('tweetService', ['$http', '$rootScope', '$location', 'webStorage', function ($http, $rootScope, $location, webStorage) {
  var tweetService = {};

  tweetService.doTweet = function(content, account, token, currentId){
    var messageDetails = new Object();
    messageDetails.content = content;
    messageDetails.account = account;  
    var jsonBody = JSON.stringify(messageDetails);

    $http.post('/api/accounts/' + currentId + '/messages', jsonBody,
        {
          headers: {
            'X-Auth-Token': token,
            'Content-Type': 'application/json'
          }

        })
        .success(function (data) {
          $rootScope.createdMessageResponse = data;
          $rootScope.success = true;

          $http.get('/api/accounts/'+ currentId   + '/messages', {headers: {'X-Auth-Token': token}})
              .success(function (data){
                $rootScope.messages = data;
              });

          $rootScope.tweetText = null;

          $rootScope.alert = { type: 'success', msg: 'Message Posted!' };
          
        })
        .error(function (error){
          alert("Tweet Error");
        })
  }

  return tweetService;
}]);