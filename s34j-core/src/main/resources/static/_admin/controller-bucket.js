angular.module('S34J')
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
                        for (var i = 0; i < data.CommonPrefixes.length; i++) {
                            var prefix = data.CommonPrefixes[i];
                            if (!$.inArray(prefix, $scope.commonPrefixes)) {
                                $scope.commonPrefixes.push(prefix);
                            }
                        }
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
                case '.mp4':
                    return 'fa-file-video';
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
        $scope.menuClicked = function ($mdMenu, $event, object) {
            if ($scope.isSelecting()) {
                return $scope.selectObjectClicked(object);
            }
            $mdMenu.open($event);
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