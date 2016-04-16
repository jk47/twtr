app.factory('securityService', ['$http', '$rootScope', '$location', 'webStorage', function ($http, $rootScope, $location, webStorage) {
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
    delete $rootScope.currentUser;
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
    setCurrentUser(undefined);
    delete $rootScope.currentUser;

    $location.url($location.path());
    $location.path('/login');
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