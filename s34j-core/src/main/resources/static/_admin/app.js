angular
    .module('S34J', ['ngMaterial', 'ngMessages', 'ngRoute'])
    .config(function ($routeProvider, $locationProvider) {
        $locationProvider.hashPrefix('!');
        $routeProvider
            .when('/home', {templateUrl: 'home.tpl.html'})
            .when('/status', {templateUrl: 'home.tpl.html'})
            .when('/settings', {templateUrl: 'settings.tpl.html'})
            .when('/users', {templateUrl: 'home.tpl.html'})
            .when('/s3/:bucketName', {templateUrl: 'bucket.tpl.html'})
            .when('/s3/:bucketName/:objectName*', {templateUrl: 'bucket.tpl.html'})
            .otherwise('/home');
    })
    .controller('LoginController', function ($scope, $rootScope, $mdDialog) {
        $scope.reset = function () {
            $scope.loading = false;
            if (window.location.host.indexOf('localhost') >= 0) {
                $scope.accessKey = 'Q3AM3UQ867SPQQA43P2F';
                $scope.secretKey = 'zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG';
                $scope.host = 'https://play.minio.io:9000';
            } else if (window.location.host.indexOf('s34j-demo.appspot.com') >= 0) {
                $scope.accessKey = 'Q3AM3UQ867SPQQA43P2F';
                $scope.secretKey = 'zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG';
                $scope.host = 'https://play.minio.io:9000';
            } else {
                $scope.accessKey = '';
                $scope.secretKey = '';
                $scope.host = window.location.protocol + '//' + window.location.host;
            }
        };
        $scope.resetClicked = function () {
            $scope.reset();
        };
        $scope.loginClicked = function () {
            var loginForm = $scope.loginForm;
            if (loginForm.$valid) {
                $scope.loading = true;
                $rootScope.authenticate({
                    accessKey: $scope.accessKey,
                    secretKey: $scope.secretKey,
                    host: $scope.host
                }, function (result) {
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
        $rootScope.toggleSideNav = function (id) {
            $mdSidenav(id).toggle();
        };
        $rootScope.showLoginDialog = function (callback) {
            $mdDialog.show({
                templateUrl: 'dialog-login.tpl.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true
            }).then(function (success) {
                if (success) callback();
            });
        };
        $rootScope.authenticate = function (auth, callback) {
            var s3 = new AWS.S3({
                accessKeyId: auth.accessKey,
                secretAccessKey: auth.secretKey,
                endpoint: auth.host,
                s3ForcePathStyle: true,
                signatureVersion: 'v4'
            });
            $rootScope.loading = true;
            s3.listBuckets({}, function (err, data) {
                $rootScope.loading = false;
                $rootScope.s3 = s3;
                sessionStorage.accessKey = auth.accessKey;
                sessionStorage.secretKey = auth.secretKey;
                sessionStorage.host = auth.host;

                $rootScope.accessKey = auth.accessKey;
                $rootScope.host = auth.host;
                $rootScope.secretKey = auth.secretKey;
                if (err) {
                    console.log(err);
                    callback(false);
                } else {
                    console.log(data.Buckets);
                    $rootScope.buckets = data.Buckets;
                    callback(true);
                }
                $rootScope.$apply();
            });
        };
        $rootScope.login = function (callback) {
            if (sessionStorage.accessKey &&
                sessionStorage.secretKey &&
                sessionStorage.host) {
                $rootScope.authenticate({
                        accessKey: sessionStorage.accessKey,
                        secretKey: sessionStorage.secretKey,
                        host: sessionStorage.host
                    },
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
    .component('connectionInfo', {
        templateUrl: 'connection-info.tpl.html',
        controller: function ($scope) {
        }
    })
    .service('BaseController', function ($rootScope, $timeout) {
        var self = {};
        self.onS3Error = function (err) {
            console.log(err);
        };
        self.refreshBuckets = function () {
            $rootScope.loading = true;
            $rootScope.s3.listBuckets({}, function (err, data) {
                if (err) self.onS3Error(err);
                else $rootScope.buckets = data.Buckets;
                $rootScope.loading = false;
                $rootScope.$apply();
            })
        };
        self.newBucket = function (name) {
            console.log(name);
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
    .controller('CreateBucketController', function ($scope, $rootScope, $location, $mdDialog, BaseController) {
        angular.extend($scope, BaseController);
        $scope.loading = false;
        $scope.createBucketClicked = function () {
            var bucketForm = $scope.createBucketForm;
            bucketForm.newBucketName.$setValidity('server', true);
            if (!bucketForm.$valid) return;
            console.log('Create Bucket ' + $scope.newBucketName);
            $scope.loading = true;
            $rootScope.s3.createBucket({
                Bucket: $scope.newBucketName
            }, function (err, data) {
                if (err) {
                    $scope.serverError = err.message;
                    bucketForm.newBucketName.$setValidity('server', false);
                } else {
                    $location.path('/s3/' + $scope.newBucketName);
                    $mdDialog.hide();
                    $scope.refreshBuckets();
                }
                $scope.loading = false;
                $scope.$apply();
            });
        };
    })
    .controller('HomeController', function ($scope, $rootScope, $location, $mdDialog, BaseController) {
        angular.extend($scope, BaseController);
        $scope.onAuthenticated = function () {
            console.log('HomeController onAuthenticated');
            $rootScope.pageTitle = $rootScope.host;
        };
        $scope.openBucket = function (bucket) {
            $location.path('/s3/' + bucket.Name);
        };
        $scope.newBucketClicked = function () {
            $mdDialog.show({
                templateUrl: 'dialog-create-bucket.tpl.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true
            });
        };
        $scope.start($scope.onAuthenticated);
    })
    .controller('SettingsController', function ($scope, $rootScope, BaseController) {
        angular.extend($scope, BaseController);
        $scope.onAuthenticated = function () {
            console.log('SettingsController onAuthenticated')
            $rootScope.pageTitle = 'Settings';
        };
        $scope.start($scope.onAuthenticated);
    })
    .controller('BucketController', function ($scope, $rootScope, BaseController, $routeParams, $timeout) {
        angular.extend($scope, BaseController);
        $scope.objects = [];
        $scope.pageSize = 50;
        $scope.prefix = '';
        $scope.loadObject = function () {
            $rootScope.loading = true;
            $rootScope.s3.listObjectsV2({
                Bucket: $routeParams.bucketName,
                MaxKeys: $scope.pageSize,
                Prefix: $scope.prefix
            }, function (err, data) {
                if (err) {
                    $scope.onS3Error(err);
                } else {
                    console.log(data);
                    $scope.objects = data.Contents;
                    $scope.isTruncated = data.IsTruncated;
                }
                $rootScope.loading = false;
                $scope.$apply();
            });
        };
        $scope.onPrefixKeyPress = function (ev) {
            if ($scope.loadPromise) {
                $timeout.cancel($scope.loadPromise);
            }
            if (ev && ev.charCode === 13) {
                $scope.loadObject();
            } else {
                $scope.loadPromise = $timeout($scope.loadObject, 500);
            }
        };
        $scope.onAuthenticated = function () {
            console.log('BucketController onAuthenticated');
            $rootScope.pageTitle = $rootScope.host + '/' + $routeParams.bucketName +
                ($routeParams.objectName ? '/' + $routeParams.objectName : '');
            $scope.loadObject();
        };
        $scope.objectClicked = function (object) {
            console.log(object.Key + ' clicked.')
        };
        $scope.start($scope.onAuthenticated);
    });
