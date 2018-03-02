angular.module('S34J')
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
    });