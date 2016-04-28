app.directive('followingDirective', function () {
    return {
        template: '<button id="followingButton" style="margin-top: -10px" id="alreadyFollowing" type="button" class="btn btn-success" ' +
        'ng-show="!myOwnDetails && isAFollower">Following' +
        '</button>' +
        '<button id="followButton" style="margin-top: -10px" id="follow" type="button" class="btn btn-info"' +
        'ng-show="!myOwnDetails && !isAFollower"' +
        'ng-click="startFollowing()">Follow' +
        '</button>'
    };
});