app.service('securityService', ['$http', '$rootScope', '$location', 'webStorage', function ($http, $rootScope, $location, webStorage) {
  var securityService = {};
  var currentUser;
  var isSignOutSuccessful;

  var setCurrentUser = function(user){
    currentUser = user;
    webStorage.set('twitterUser', currentUser);
    $rootScope.$emit('userChange', currentUser);
  };

  securityService.setCurrentUser = function(user){
    currentUser = user;
    webStorage.set('twitterUser', currentUser);
    $rootScope.$emit('userChange', currentUser);
  };

  var loginSuccess = function (response) {
    setCurrentUser({
      username: response.data.username,
      roles: response.data.roles,
      token: response.data['access_token'],
    });
  };

  var loginFailure = function () {
    setCurrentUser(undefined);
    delete $rootScope.currentUser;
  };

  securityService.isSignOutSuccessful = function(){
    return isSignOutSuccessful;
  };

  securityService.getToken = function(){
    return currentUser.token;
  };

  securityService.setToken = function(newToken){
    currentUser.token = newToken;
  };

    securityService.getCurrentUser= function(){
        return currentUser;
    };

  securityService.getUsername= function(){
    return currentUser.username;
  };

  securityService.setUsername= function(newName){
     currentUser.username = newName;
  };

  securityService.logout = function () {
    setCurrentUser(undefined);
    delete $rootScope.currentUser;

    isSignOutSuccessful = true;

    $location.url($location.path());
    $location.path('/login');
  };

  securityService.login = function (username, password) {
    var loginPayload = {username: username, password: password};
    return $http.post('/api/login', loginPayload).then(loginSuccess, loginFailure);
  };

  securityService.currentUser = function () {
    return currentUser;
  };

  setCurrentUser(webStorage.get('twitterUser'));

  return securityService;
}]);