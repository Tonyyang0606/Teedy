angular.module('docs').controller('SettingsRegister', function(Restangular ,$scope, $state, $dialog, $translate){
    $scope.loadUsers = function(){
        Restangular.one("registerUser/list").get().then(function(data) {
            $scope.users = data.register_users.map(function(u) {
                u.storage /= 1000000;
                return u;
            });
        });
    };

    $scope.loadUsers();

    $scope.operate = function(user, status){
        if(status < 1 || status > 2) return;
        submit_data = {"id": user.id, "status": status};
        Restangular.one("registerUser/operate").post('', submit_data).then(function(data) {
            user.status = data.status;
            user.operated_time = data.operated_time;
            let title = $translate.instant("settings.register.operation_result_title");
            let message = $translate.instant("settings.register.operation_result_success");
            message += ": ";
            if(user.status === 1){
                message += $translate.instant("settings.register.status_approved");
            } else {
                message += $translate.instant("settings.register.status_rejected");
            }
            $dialog.messageBox(title, message, [
                {
                    result: 'ok',
                    label: $translate.instant("ok"),
                    cssClass: 'btn-primary'
                }
            ]);
        }, function(data){
            let title = $translate.instant("settings.register.operation_result_title");
            let message = $translate.instant("settings.register.operation_result_fail");
            message += ": ";
            if(data.error === "invalid status"){
                message += $translate.instant("settings.register.operation_result_invalid_status");
            } else if(date.error === "no such user"){
                message += $translate.instant("settings.register.operation_result_no_such_user");
            } else if(date.error === "server error"){
                message += $translate.instant("setting.register.operation_result_server_error");
            }
            $dialog.messageBox(title, message, [
                {
                    result: 'ok',
                    label: $translate.instant("ok"),
                    cssClass: 'btn-primary'
                }
            ]);
        });
    }
});