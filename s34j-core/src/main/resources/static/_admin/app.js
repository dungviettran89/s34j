angular
    .module('S34J', ['ngMaterial', 'ngMessages'])
    .controller('LoginController', function ($scope) {
        $scope.reset = function () {
            $scope.loading = false;
            $scope.accessKey = undefined;
            $scope.secretKey = undefined
        };
        $scope.login = function () {
            $scope.loading = true;
            debugger;
        };
        $scope.reset();
    })
    .controller('MainController', function ($rootScope, $mdSidenav, $mdDialog) {
        $rootScope.accessKeyId = localStorage.accessKeyId;
        $rootScope.secretAccessKey = localStorage.secretAccessKey;

        $rootScope.toggleSideNav = function (id) {
            $mdSidenav(id).toggle();
        };

        $rootScope.showLoginDialog = function () {
            $mdDialog.show({
                templateUrl: 'index-login.tpl.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true
            }).then(function (success) {
                console.log(success);
            });
        };

        if (!$rootScope.accessKeyId) {
            $rootScope.showLoginDialog();
        }
    })
    .controller('DataController', function ($rootScope, $scope) {
        var s3 = new AWS.S3({
            accessKeyId: $rootScope.accessKeyId,
            secretAccessKey: $rootScope.secretAccessKey,
            endpoint: window.location,
            s3ForcePathStyle: true,
            signatureVersion: 'v4'
        });
        s3.listBuckets({}, function (err, data) {
            console.log('err', err);
            console.log('data', data);
        });
    });
