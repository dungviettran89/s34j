angular
    .module('S34J', ['ngMaterial'])
    .controller('Main', function ($scope, $mdSidenav) {
        $scope.toggleSideNav = function (id) {
            $mdSidenav(id).toggle();
        }
    });
