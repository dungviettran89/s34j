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
        templateUrl: 'connection-info.tpl.html',
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
    })
    .controller('CreateBucketController', function ($scope, $rootScope, $location, $mdDialog, BaseController) {
        angular.extend($scope, BaseController);
        $scope.loading = false;
        $scope.newBucketNameKeyPressed = function (ev) {
            if (ev && ev.keyCode === 13) {
                $scope.createBucketClicked();
            }
        };
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
    .controller('BucketController', function ($scope, $rootScope, BaseController, $routeParams, $timeout, $window,
                                              $mdToast, ngCopy) {
        angular.extend($scope, BaseController);
        $scope.objects = [];
        $scope.bucketName = $routeParams.bucketName;
        $scope.objectName = $routeParams.objectName;
        $scope.commonPrefixes = [];
        $scope.pageSize = 50;
        $scope.prefix = $routeParams.objectName;
        $scope.select = {};
        $scope.loadObject = function () {
            $rootScope.loading = true;
            $rootScope.s3.listObjectsV2({
                    Bucket: $routeParams.bucketName,
                    MaxKeys: $scope.pageSize,
                    Prefix: $scope.prefix,
                    Delimiter: '/'
                },
                function (err, data) {
                    if (err) {
                        $scope.onS3Error(err);
                    } else {
                        console.log(data);
                        $scope.objects = data.Contents;
                        $scope.isTruncated = data.IsTruncated;
                        $scope.nextContinuationToken = data.NextContinuationToken;
                        $scope.commonPrefixes = data.CommonPrefixes;
                    }
                    $rootScope.loading = false;
                    $scope.$apply();
                }
            )
        };
        $scope.loadMore = function () {
            $rootScope.loading = true;
            $rootScope.s3.listObjectsV2({
                    Bucket: $routeParams.bucketName,
                    MaxKeys: $scope.pageSize,
                    Prefix: $scope.prefix,
                    Delimiter: '/',
                    ContinuationToken: $scope.nextContinuationToken
                },
                function (err, data) {
                    if (err) {
                        $scope.onS3Error(err);
                    } else {
                        console.log(data);
                        $scope.objects = $scope.objects.concat(data.Contents);
                        $scope.commonPrefixes = $scope.commonPrefixes.concat(data.CommonPrefixes);
                        $scope.isTruncated = data.IsTruncated;
                        $scope.nextContinuationToken = data.NextContinuationToken;
                    }
                    $rootScope.loading = false;
                    $scope.$apply();
                }
            )
        };
        $scope.getIcon = function (name) {
            if ($scope.isSelecting()) {
                return $scope.select[name] ? 'fa-check-square' : 'fa-square';
            }
            var suffix = name.substr(name.lastIndexOf('.'));
            switch (suffix) {
                case'.png':
                case'.jpg':
                case'.jpeg':
                    return 'fa-image';
                case '.txt':
                    return 'fa-file-alt';
                default:
                    return 'fa-file';
            }
        };
        $scope.commonPrefixClicked = function (commonPrefix) {
            $scope.prefix = commonPrefix.Prefix;
            $scope.loadDelayed(100);
        };
        $scope.loadDelayed = function (delay) {
            if ($scope.loadPromise) {
                $timeout.cancel($scope.loadPromise);
            }
            $scope.loadPromise = $timeout($scope.loadObject, 500);
        };
        $scope.onPrefixKeyPress = function (ev) {
            if (ev && ev.charCode === 13) {
                $scope.loadDelayed(100);
            } else {
                $scope.loadDelayed(500);
            }
        };
        $scope.onAuthenticated = function () {
            console.log('BucketController onAuthenticated');
            $rootScope.pageTitle = $rootScope.host + '/' + $routeParams.bucketName +
                ($routeParams.objectName ? '/' + $routeParams.objectName : '');
            $scope.loadObject();
        };
        $scope.objectClicked = function (object, $index) {
            if ($scope.isSelecting()) {
                return $scope.selectObjectClicked(object);
            }
            $timeout(function () {
                angular.element('#menu-' + $index).triggerHandler('click');
            }, 0);
        };
        $scope.isSelecting = function () {
            for (var key in $scope.select) {
                if ($scope.select[key]) {
                    return true;
                }
            }
            return false;
        };
        $scope.selectObjectClicked = function (object) {
            $scope.select[object.Key] = !$scope.select[object.Key];
        };
        $scope.openObjectClicked = function (object) {
            var params = {Bucket: $scope.bucketName, Key: object.Key, Expires: 180};
            var url = $rootScope.s3.getSignedUrl('getObject', params);
            $window.open(url, '_blank');
        };
        $scope.copyLink = function (object, isPublic) {
            var url = $rootScope.host + '/' + $scope.bucketName + '/' + object.Key;
            if (isPublic) {
                var params = {Bucket: $scope.bucketName, Key: object.Key, Expires: 180};
                url = $rootScope.s3.getSignedUrl('getObject', params);
            }
            ngCopy(url);
            $mdToast.show(
                $mdToast.simple()
                    .textContent('Link copied to Clipboard!')
                    .position('top right')
                    .hideDelay(3000)
            );
        };
        $scope.selectNone = function () {
            $scope.select = {};
        };
        $scope.selectAll = function () {
            for (var i in $scope.objects) {
                var object = $scope.objects[i];
                $scope.select[object.Key] = true;
            }
        };
        $scope.selectCount = function () {
            var count = 0;
            for (var key in $scope.select) {
                if ($scope.select[key]) {
                    count++;
                }
            }
            return count;
        };
        $scope.start($scope.onAuthenticated);
    });
