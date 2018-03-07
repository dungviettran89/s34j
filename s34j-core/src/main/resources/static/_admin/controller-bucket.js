angular.module('S34J')
    .controller('NewFileController', function ($scope, $rootScope, $mdDialog, $routeParams, $timeout, file, prefix) {
        $scope.bucketName = $routeParams.bucketName;
        $scope.file = file;
        $scope.newFileName = prefix ? (prefix + file.name) : (file.name);
        $scope.cancel = function () {
            $mdDialog.hide(false);
        };
        $scope.uploadSelectedFile = function () {
            console.log('Uploading ' + $scope.newFileName);
            $scope.newFileForm.newFileName.$setValidity('server', true);
            $scope.loading = true;

            $rootScope.s3.upload({
                Body: file,
                Bucket: $scope.bucketName,
                Key: $scope.newFileName,
                ContentType: file.type
            }, function (err, data) {
                if (err) {
                    $scope.serverError = err.message;
                    $scope.newFileForm.newFileName.$setValidity('server', false);
                } else {
                    $mdDialog.hide(true);
                }
                $scope.loading = false;
                $scope.$apply();
            });
        };
    })
    .controller('BucketController', function ($scope, $rootScope, BaseController, $routeParams, $timeout, $window,
                                              $mdToast, ngCopy, $mdDialog) {
        angular.extend($scope, BaseController);
        $scope.objects = [];
        $scope.bucketName = $routeParams.bucketName;
        $scope.objectName = $routeParams.objectName;
        $scope.commonPrefixes = [];
        $scope.pageSize = 32;
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
                return $scope.select[name] ? 'far fa-check-square' : 'far fa-square';
            }
            var suffix = name.substr(name.lastIndexOf('.'));
            switch (suffix) {
                case'.png':
                case'.jpg':
                case'.jpeg':
                    return 'far fa-image';
                case '.mp4':
                    return 'far fa-file-video';
                case '.txt':
                    return 'far fa-file-alt';
                case '.apk':
                    return 'fab fa-android';
                default:
                    return 'far fa-file';
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
            $scope.openMenu(object, $index);
        };
        $scope.openMenu = function (object, $index) {
            $timeout(function () {
                angular.element('#menu-' + $index).triggerHandler('click');
            }, 0);
        };
        $scope.menuClicked = function ($mdMenu, $event, object) {
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
        $scope.deleteObject = function (object) {
            var confirmDialog = $mdDialog.confirm()
                .title('Delete Object')
                .textContent('Do you want to delete ' + object.Key)
                .ariaLabel('Delete Object')
                .ok('Delete')
                .cancel('Cancel');

            $mdDialog.show(confirmDialog).then(function () {
                $scope.loading = true;
                console.log("Deleting " + object.Key);
                $rootScope.s3.deleteObject({
                    Bucket: $scope.bucketName,
                    Key: object.Key
                }, function (err, data) {
                    if (err) {
                        $mdDialog.show($mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title('Error')
                            .textContent(err.message)
                            .ariaLabel('Can not delete')
                            .ok('OK'));
                    } else {
                        $scope.loadObject();
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent('Object deleted.')
                                .position('top right')
                                .hideDelay(3000)
                        );
                    }
                    $scope.loading = false;
                    $scope.$apply();
                });
            }, function () {
            });
        };
        $scope.deleteSelectedObjects = function () {
            var params = {Bucket: $scope.bucketName, Delete: {Objects: [], Quiet: true}};
            for (var k in $scope.select) {
                if ($scope.select[k] === true)
                    params.Delete.Objects.push({Key:k});
            }
            var confirmDialog = $mdDialog.confirm()
                .title('Delete Object')
                .textContent('Do you want to delete ' + params.Delete.Objects.length + ' objects?')
                .ariaLabel('Delete Object')
                .ok('Delete')
                .cancel('Cancel');
            $mdDialog.show(confirmDialog).then(function () {
                console.log('Deleting ', params.Delete.Objects);
                $scope.loading = true;
                $rootScope.s3.deleteObjects(params, function (err, data) {
                    if (err) {
                        $mdDialog.show($mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title('Error')
                            .textContent(err.message)
                            .ariaLabel('Can not delete')
                            .ok('OK'));
                    } else {
                        $scope.loadObject();
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent('Objects deleted.')
                                .position('top right')
                                .hideDelay(3000)
                        );
                        $scope.select = {};
                    }
                    $scope.loading = false;
                    $scope.$apply();
                });
            }, function () {
            });
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
        $scope.hidePrefix = function (name) {
            var parent = $scope.getParent(name);
            return parent ? name.replace(parent, '') : name;
        };
        $scope.shouldShowParent = function () {
            return $scope.prefix && $scope.prefix.indexOf('/') > 0;
        };
        $scope.getParent = function (name) {
            if (name.lastIndexOf('/') === name.length - 1) {
                name = name.substr(0, name.length - 1);
            }
            if (!name) return "";
            if (name.lastIndexOf('/') <= 0) return "";
            return name.substr(0, name.lastIndexOf('/') + 1);
        };
        $scope.showNewFileDialog = function () {
            $('<input type="file">').on('change', function () {
                console.log(this.files);
                if (this.files.length === 1) {
                    $mdDialog.show({
                        templateUrl: 'dialog-new-file.tpl.html',
                        controller: 'NewFileController',
                        locals: {
                            file: this.files[0],
                            prefix: $scope.prefix
                        },
                        parent: angular.element(document.body),
                    }).then(function (uploaded) {
                        if (uploaded) {
                            $scope.loadObject();
                        }
                    });
                }
            }).click();
        };
        $scope.start($scope.onAuthenticated);
    });