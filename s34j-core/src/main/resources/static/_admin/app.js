angular
    .module('S34J', ['ngMaterial', 'ngMessages'])
    .service('$storage', function ($rootScope) {
        var $storage = {};
        $storage.login = function (accessKey, secretKey, callback) {
            $rootScope.loading = true;
            $storage.s3 = new AWS.S3({
                accessKeyId: accessKey,
                secretAccessKey: secretKey,
                endpoint: window.location,
                s3ForcePathStyle: true,
                signatureVersion: 'v4'
            });
            $storage.s3.listBuckets({}, function (err, data) {
                $rootScope.loading = false;
                $rootScope.$apply();
                if (err) console.log(err);
                callback(err == null);
            });
        };
        $storage.listBuckets = function (callback) {
            $rootScope.loading = true;
            $storage.s3.listBuckets({}, function (err, data) {
                $rootScope.loading = false;
                $rootScope.$apply();
                if (err) {
                    console.log(err);
                    if ($storage.errorCallback) {
                        $storage.errorCallback(err);
                    }
                } else {
                    callback(data);
                }
            });
        };
        return $storage;
    })
    .controller('LoginController', function ($scope, $mdDialog, $storage) {
        $scope.reset = function () {
            $scope.loading = false;
            $scope.accessKey = 'Q3AM3UQ867SPQQA43P2F';
            $scope.secretKey = 'zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG'
        };
        $scope.loginClicked = function () {
            if ($scope.loginForm.$valid) {
                $scope.loading = true;
                sessionStorage.accessKey = $scope.accessKey
                sessionStorage.secretKey = $scope.secretKey
                $storage.login($scope.accessKey, $scope.secretKey, function (result) {
                    $scope.loading = false;
                    if (!result) {
                        $scope.loginForm.accessKey.$setValidity('forbidden', false);
                        $scope.loginForm.secretKey.$setValidity('forbidden', false);
                    } else {
                        $mdDialog.hide(true);
                    }
                    $scope.$apply();
                });
            }
        };
        $scope.reset();
    })
    .controller('MainController', function ($rootScope, $mdSidenav, $mdDialog) {
        $rootScope.toggleSideNav = function (id) {
            $mdSidenav(id).toggle();
        };
        $rootScope.showLoginDialog = function (callback) {
            $mdDialog.show({
                templateUrl: 'index-login.tpl.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true
            }).then(function (success) {
                if (success) callback();
            });
        };
    })
    .controller('DataController', function ($rootScope, $scope, $storage) {
        $scope.onError = function (err) {
            if (err.statusCode === 403) {
                $rootScope.showLoginDialog($scope.begin);
            }
        };
        $scope.authenticated = function () {
            console.log('DataController authenticated.');
            $storage.errorCallback = $scope.onError;
            $storage.listBuckets(function () {

            });
        };
        $scope.begin = function () {
            console.log('DataController begin.');
            if (!sessionStorage.accessKey) {
                $rootScope.showLoginDialog($scope.authenticated);
            } else {
                $storage.login(sessionStorage.accessKey, sessionStorage.secretKey, function (result) {
                    if (result) {
                        $scope.authenticated();
                    } else {
                        $rootScope.showLoginDialog($scope.authenticated);
                    }
                });
            }
        };
        $scope.begin();
    });
