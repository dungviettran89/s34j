angular
    .module('S34J', ['ngMaterial', 'ngMessages', 'ngRoute'])
    .config(function ($routeProvider, $locationProvider) {
        $locationProvider.hashPrefix('!');
        $routeProvider
            .when('/home', {templateUrl: 'home.tpl.html'})
            .when('/status', {templateUrl: 'home.tpl.html'})
            .when('/settings', {templateUrl: 'home.tpl.html'})
            .when('/users', {templateUrl: 'home.tpl.html'})
            .when('/s3/:bucketName', {templateUrl: 'bucket.tpl.html'})
            .when('/s3/:bucketName/:objectName*', {templateUrl: 'bucket.tpl.html'})
            .otherwise('/home');
    })
    .controller('LoginController', function ($scope, $rootScope, $mdDialog) {
        $scope.reset = function () {
            $scope.loading = false;
            $scope.accessKey = 'Q3AM3UQ867SPQQA43P2F';
            $scope.secretKey = 'zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG'
        };
        $scope.loginClicked = function () {
            var loginForm = $scope.loginForm;
            if (loginForm.$valid) {
                $scope.loading = true;
                $rootScope.authenticate($scope.accessKey, $scope.secretKey, function (result) {
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
    .run(function ($rootScope, $mdSidenav, $mdDialog) {
        console.log('Application startup!');
        $rootScope.windowLocationHost = window.location.host;
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
        $rootScope.authenticate = function (accessKey, secretKey, callback) {
            var s3 = new AWS.S3({
                accessKeyId: accessKey,
                secretAccessKey: secretKey,
                endpoint: window.location,
                s3ForcePathStyle: true,
                signatureVersion: 'v4'
            });
            s3.listBuckets({}, function (err, data) {
                $rootScope.loading = false;
                $rootScope.s3 = s3;
                sessionStorage.accessKey = accessKey;
                sessionStorage.secretKey = secretKey;
                $rootScope.accessKey = accessKey;
                if (err) {
                    console.log(err);
                    callback(false);
                } else {
                    console.log(data.Buckets);
                    $rootScope.buckets = data.Buckets;
                    callback(true);
                }
            });
        };
        $rootScope.login = function (callback) {
            if (sessionStorage.accessKey) {
                $rootScope.authenticate(sessionStorage.accessKey, sessionStorage.secretKey,
                    function (result) {
                        if (!result) {
                            console.log('Stored token is invalid!');
                            $rootScope.showLoginDialog(callback);
                        } else {
                            console.log('Stored token is valid!');
                            callback();
                        }
                    });
            } else {
                $rootScope.showLoginDialog(callback);
            }
        };
        $rootScope.logout = function () {
            $rootScope.s3 = undefined;
            sessionStorage.accessKey = undefined;
            sessionStorage.secretKey = undefined;
            window.location.reload(true);
        };
    })
    .service('BaseController', function ($rootScope, $timeout) {
        var self = {};
        self.onS3Error = function (err) {
            console.log(err);
        };
        self.listBuckets = function (callback) {
            $rootScope.loading = true;
            $rootScope.s3.listBuckets({}, function (err, data) {
                if (err) {
                    $scope.on(err);
                } else {
                    callback(data);
                }
            })
        };
        self.start = function (callback) {
            if (!$rootScope.s3) {
                $rootScope.login(callback);
            } else {
                callback();
            }
        };
        return self;
    })
    .controller('HomeController', function ($scope, $rootScope, BaseController) {
        angular.extend($scope, BaseController);
        $scope.onAuthenticated = function () {
            console.log('HomeController onAuthenticated')
        };
        $scope.start($scope.onAuthenticated);
    })
    .controller('BucketController', function ($scope, $rootScope, BaseController) {
        angular.extend($scope, BaseController);
        $scope.onAuthenticated = function () {
            console.log('BucketController onAuthenticated')
        };
        $scope.start($scope.onAuthenticated);
    });
