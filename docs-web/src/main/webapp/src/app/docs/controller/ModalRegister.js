'use strict';

/**
 * Modal register controller.
 */
angular.module('docs').controller('ModalRegister', function ($scope, $uibModalInstance) {
  $scope.user = {
    username: '',
    // Add any other registration fields you need here
    // For example:
    // email: '',
    // password: '',
    // confirmPassword: ''
  };
  
  $scope.close = function(registerUser) {
    $uibModalInstance.close(registerUser);
}
});