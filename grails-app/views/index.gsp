
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
<div class="container">

    <h1>Twtr</h1>
    <div ng-controller="twtrController">
        <form class="navbar-form navbar-left" role="search">
            <div class="form-group">
                <input type="text" class="form-control" placeholder="Search People" ng-model="formInfo.SearchPeople">
            </div>
            <button type="submit" class="btn btn-default" ng-click="SearchPeople()">Submit</button>
            <div class="form-group">
                <input type="text" class="form-control" placeholder="Search Posts" ng-model="formInfo.SearchPosts">
            </div>
            <button type="submit" class="btn btn-default" ng-click="SearchPosts()">Submit</button>
        </form>
        {{ details }}
    </div>


</div>


</body>
</html>