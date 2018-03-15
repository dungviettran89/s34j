angular
    .module('S34J', ['ngMaterial', 'ngMessages', 'ngRoute'])
    .config(function ($routeProvider, $locationProvider) {
        $locationProvider.hashPrefix('!');
        $routeProvider
            .when('/home', {templateUrl: 'page-home.tpl.html'})
            .when('/status', {templateUrl: 'page-home.tpl.html'})
            .when('/settings', {templateUrl: 'settings.tpl.html'})
            .when('/users', {templateUrl: 'page-home.tpl.html'})
            .when('/s3/:bucketName', {templateUrl: 'page-bucket.tpl.html'})
            .when('/s3/:bucketName/:objectName*', {templateUrl: 'page-bucket.tpl.html'})
            .otherwise('/home');
    })
    .controller('LoginController', function ($scope, $rootScope, $mdDialog) {
        $scope.reset = function () {
            $scope.loading = false;
            if (window.location.host.indexOf('s34j-dev.local') >= 0) {
                $scope.accessKey = 'Q3AM3UQ867SPQQA43P2F';
                $scope.secretKey = 'zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG';
                $scope.host = window.location.protocol + '//' + window.location.host;
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
    .filter('bytes', function () {
        return function (bytes, precision) {
            if (bytes === 0) return '0 byte';
            if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
            if (typeof precision === 'undefined') precision = 1;
            var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
                number = Math.floor(Math.log(bytes) / Math.log(1024));
            return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
        }
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
        templateUrl: 'fragment-connection-info.tpl.html',
        controller: function ($scope) {
        }
    })
    .service('ngCopy', ['$window', function ($window) {
        var body = angular.element($window.document.body);
        var textarea = angular.element('<textarea/>');
        textarea.css({
            position: 'fixed',
            opacity: '0'
        });

        return function (toCopy) {
            textarea.val(toCopy);
            body.append(textarea);
            textarea[0].select();

            try {
                var successful = document.execCommand('copy');
                if (!successful) throw successful;
            } catch (err) {
                window.prompt("Copy to clipboard: Ctrl+C, Enter", toCopy);
            }

            textarea.remove();
        }
    }])
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
    });

