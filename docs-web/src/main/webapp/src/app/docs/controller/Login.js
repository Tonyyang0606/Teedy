'use strict';

/**
 * Login controller.
 */
angular.module('docs').controller('Login', function(Restangular, $scope, $rootScope, $state, $stateParams, $dialog, User, $translate, $uibModal) {
  $scope.codeRequired = false;

  // Get the app configuration
  Restangular.one('app').get().then(function(data) {
    $rootScope.app = data;
  });

  // Login as guest
  $scope.loginAsGuest = function() {
    $scope.user = {
      username: 'guest',
      password: ''
    };
    $scope.login();
  };
  
  // Login
  $scope.login = function() {
    User.login($scope.user).then(function() {
      User.userInfo(true).then(function(data) {
        $rootScope.userInfo = data;
      });

      if($stateParams.redirectState !== undefined && $stateParams.redirectParams !== undefined) {
        $state.go($stateParams.redirectState, JSON.parse($stateParams.redirectParams))
          .catch(function() {
            $state.go('document.default');
          });
      } else {
        $state.go('document.default');
      }
    }, function(data) {
      if (data.data.type === 'ValidationCodeRequired') {
        // A TOTP validation code is required to login
        $scope.codeRequired = true;
      } else {
        // Login truly failed
        var title = $translate.instant('login.login_failed_title');
        var msg = $translate.instant('login.login_failed_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };

  // Register - 独立定义的注册函数
  $scope.register = function() {
    $uibModal.open({
      templateUrl: 'partial/docs/register.html',
      controller: 'ModalRegister'
    }).result.then(function (registerUser) {
      if(registerUser === null){
        return;
      }
      // 注册成功后的处理逻辑
      registerUser.storage *= 1000000;
      Restangular.one("registerUser/register").put(registerUser).then(function () {
        var title = $translate.instant('login.register_submit_title');
        var msg = $translate.instant('login.register_submit_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }, function () {
        var title = $translate.instant('login.register_submit_title');
        var msg = $translate.instant('login.register_submit_error_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      });
      
      // 可选：自动登录新注册的用户
      // $scope.user = user;
      // $scope.login();
    });
  };

  // Password lost - 独立定义的密码找回函数
  $scope.openPasswordLost = function() {
    $uibModal.open({
      templateUrl: 'partial/docs/passwordlost.html',
      controller: 'ModalPasswordLost'
    }).result.then(function(username) {
      if (username === null) {
        return;
      }
      
      // 发送密码重置邮件
      Restangular.one('user').post('password_lost', {
        username: username
      }).then(function() {
        var title = $translate.instant('login.password_lost_sent_title');
        var msg = $translate.instant('login.password_lost_sent_message', { username: username });
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }, function() {
        var title = $translate.instant('login.password_lost_error_title');
        var msg = $translate.instant('login.password_lost_error_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      });
    });
  };
});