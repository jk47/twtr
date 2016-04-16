app.factory('securityService', ['$http', '$rootScope', 'webStorage', function ($http, $rootScope, webStorage) {
  var service = {};
  var currentUser;

  var setCurrentUser = function(user){
    currentUser = user;
    webStorage.set('restaurantUser', currentUser);
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
  };

  service.getToken = function(){
    return currentUser.token;
  };

    service.getCurrentUser= function(){
        return currentUser;
    };

  service.getUsername= function(){
    return currentUser.username;
  };

  service.logout = function () {
    currentUser = undefined
    delete $rootScope.currentUser;
  };

  service.login = function (username, password) {
    var loginPayload = {username: username, password: password};
    return $http.post('/api/login', loginPayload).then(loginSuccess, loginFailure);
  };

  service.currentUser = function () {
    return currentUser;
  };

  setCurrentUser(webStorage.get('restaurantUser'));

  return service;
}]);