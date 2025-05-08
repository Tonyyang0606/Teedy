'use strict';

/**
 * Settings Register Controller
 */
angular.module('docs').controller('SettingsRegister', function($scope, $state, Restangular, $translate, $dialog, User) {
  // Initialize user model
  $scope.user = {
    username: '',
    email: '',
    password: '',
    passwordconfirm: '',
    storage_quota: 100 // Default quota in MB
  };

  // Form submission
  $scope.register = function() {
    if ($scope.registerUserForm.$invalid) {
      return;
    }

    // Check if passwords match
    if ($scope.user.password !== $scope.user.passwordconfirm) {
      var title = $translate.instant('validation.password_confirm');
      var msg = $translate.instant('validation.password_confirm_message');
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns);
      return;
    }

    // Prepare data for API
    var registerData = {
      username: $scope.user.username,
      email: $scope.user.email,
      password: $scope.user.password,
      storage_quota: $scope.user.storage_quota
    };

    // Call register API
    Restangular.all('user').post(registerData).then(function() {
      // Registration success
      var title = $translate.instant('register.submit_title');
      var msg = $translate.instant('register.submit_message');
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      
      $dialog.messageBox(title, msg, btns).then(function() {
        // Redirect to login or other page after successful registration
        $state.go('login');
      });
    }, function(response) {
      // Registration failed
      var title = $translate.instant('register.error_title');
      var msg = '';
      
      if (response.data && response.data.message) {
        msg = response.data.message;
      } else {
        msg = $translate.instant('register.error_message');
      }
      
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns);
    });
  };

  // Cancel registration
  $scope.cancel = function() {
    $state.go('login');
  };
});