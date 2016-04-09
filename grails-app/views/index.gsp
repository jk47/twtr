<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <title>Twtr</title>
    <asset:javascript src="application.js"/>
    <asset:stylesheet src="application.css"/>
</head>

<body ng-app="app">
<div class="container" ng-controller="twtrController">
    <form id="login-form">
        <div id="login-container" class="container well" ng-show="!isLoggedIn">
            <div class="form-group">
                <div ng-show="logoutHappened"><h2>We'll miss you dearly!</h2></div>
                <div><h2>Login</h2></div>
                <br>
                <label for="loginHandle">Handle</label>
                <input id="loginHandle" class="form-control" placeholder="handle"
                       type="text" ng-model="auth.username" required />
            </div>
            <div class="form-group">
                <label for="loginPassword">Password</label>
                <input id="loginPassword" class="form-control" placeholder="password"
                       type="password" ng-model="auth.password" required />
            </div>

            <div class="alert-danger" ng-show="errorMessage && errorMessage.length > 0">{{errorMessage}}</div><br/>
            <button id="do-login" type="submit" class="btn btn-primary" ng-click="login()">Login Now</button>
            <h1>{{loginTest}}</h1>

            <br />
            <div id="auth-token">
                {{ auth.token }}
            </div>

        </div>
        <div id="logout-container" class="container well" ng-show="isLoggedIn">
            <h2>You are currently logged in as {{ auth.handle }}</h2>
            <button id="logout" ng-click="logout()">Logout</button>
        </div>

    </form>

</div>


</body>
</html>