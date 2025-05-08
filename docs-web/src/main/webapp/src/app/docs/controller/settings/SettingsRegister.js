angular.module('docs').controller('SettingsRegister', function(Restangular ,$scope, $state, $dialog, $translate){
    $scope.loadUsers = function(){
        $scope.users = [{"username": "Adam", "email": "132@123", "storage": 132, "submit_time": "2025/05/26"}];
    };

    $scope.loadUsers();

    $scope.approve = function() {
        $dialog.messageBox("Approved", "Approved", [
            {
                result: 'ok',
                label: "OK",
                cssClass: 'btn-primary'
            }
        ]);
    }
    $scope.reject = function() {
        $dialog.messageBox("Rejected", "Rejected", [
            {
                result: 'ok',
                label: "OK",
                cssClass: 'btn-primary'
            }
        ]);
    };
});