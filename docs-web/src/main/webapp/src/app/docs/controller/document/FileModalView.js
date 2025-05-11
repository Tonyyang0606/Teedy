'use strict';

/**
 * File modal view controller.
 */
angular.module('docs').controller('FileModalView', function ($uibModalInstance, $scope, $state, $stateParams, $sce, Restangular, $transitions) {
  var setFile = function (files) {
    // Search current file
    _.each(files, function (value) {
      if (value.id === $stateParams.fileId) {
        $scope.file = value;
        $scope.trustedFileUrl = $sce.trustAsResourceUrl('../api/file/' + $stateParams.fileId + '/data');
      }
    });
  };

  // Load files
  Restangular.one('file/list').get({ id: $stateParams.id }).then(function (data) {
    $scope.files = data.files;
    setFile(data.files);

    // File not found, maybe it's a version
    if (!$scope.file) {
      Restangular.one('file/' + $stateParams.fileId + '/versions').get().then(function (data) {
        setFile(data.files);
      });
    }
  });

  /**
   * Return the next file.
   */
  $scope.nextFile = function () {
    var next = undefined;
    _.each($scope.files, function (value, key) {
      if (value.id === $stateParams.fileId) {
        next = $scope.files[key + 1];
      }
    });
    return next;
  };

  /**
   * Return the previous file.
   */
  $scope.previousFile = function () {
    var previous = undefined;
    _.each($scope.files, function (value, key) {
      if (value.id === $stateParams.fileId) {
        previous = $scope.files[key - 1];
      }
    });
    return previous;
  };

  /**
   * Navigate to the next file.
   */
  $scope.goNextFile = function () {
    var next = $scope.nextFile();
    if (next) {
      $state.go('^.file', { id: $stateParams.id, fileId: next.id });
    }
  };

  /**
   * Navigate to the previous file.
   */
  $scope.goPreviousFile = function () {
    var previous = $scope.previousFile();
    if (previous) {
      $state.go('^.file', { id: $stateParams.id, fileId: previous.id });
    }
  };

  /**
   * Open the file in a new window.
   */
  $scope.openFile = function () {
    window.open('../api/file/' + $stateParams.fileId + '/data');
  };

  /**
   * Open the file content a new window.
   */
  $scope.openFileContent = function () {
    window.open('../api/file/' + $stateParams.fileId + '/data?size=content');
  };

  
$scope.llmOutput = '';

$scope.extractFileContent = async function() {
  $scope.isLoading = true;
  $scope.llmOutput = '';
  $scope.$evalAsync(); 
  
  try {

    const fileResponse = await fetch('../api/file/' + $stateParams.fileId + '/data?size=content');
    if (!fileResponse.ok) {
      throw new Error('无法获取文件内容: HTTP ' + fileResponse.status);
    }
    const fileContent = await fileResponse.text();


    const llmOptions = {
      method: 'POST',
      headers: {
        Authorization: 'Bearer sk-wtehfqfabhdhcwuvqrmaqvvwkxotzsmlwnfgfeywbsdgbfoz', 
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: 'THUDM/GLM-4-32B-0414',
        messages: [{
          role: 'user',
          content: `请分析以下内容并给出中文关键信息：\n\n${fileContent.substring(0, 115000)}，请你只生成文字内容，我不要markdown格式，`
        }],
        stream: false,
        max_tokens: 8192,  
        temperature: 0.5   
      })
    };


    const timeoutPromise = new Promise((_, reject) => 
      setTimeout(() => reject(new Error('请求超时')), 150000)); 
    const llmResponse = await Promise.race([
      fetch('https://api.siliconflow.cn/v1/chat/completions', llmOptions),
      timeoutPromise
    ]);
    
    if (!llmResponse.ok) {
      throw new Error(`LLM接口错误: HTTP ${llmResponse.status}`);
    }

    const llmResult = await llmResponse.json();
    

    $scope.llmOutput = llmResult.choices?.[0]?.message?.content 
      || '未获取到有效响应';
    
  } catch (err) {
    console.error('处理失败:', err);
    $scope.llmOutput = `错误: ${err.message || '未知错误'}`;
    
    // 特殊处理常见错误
    if (err.message.includes('超时')) {
      $scope.llmOutput = '请求超时，请稍后重试';
    } else if (err.message.includes('401')) {
      $scope.llmOutput = '认证失败，请检查API密钥';
    }
    
  } finally {
    $scope.isLoading = false;
    $scope.$evalAsync(); // 确保最终状态更新
  }
};

$scope.llmOutput1 = '';

$scope.translate = async function() {
  $scope.isLoading1 = true;
  $scope.llmOutput1 = '';
  $scope.$evalAsync(); 
  
  try {

    const fileResponse = await fetch('../api/file/' + $stateParams.fileId + '/data?size=content');
    if (!fileResponse.ok) {
      throw new Error('无法获取文件内容: HTTP ' + fileResponse.status);
    }
    const fileContent = await fileResponse.text();


    const llmOptions = {
      method: 'POST',
      headers: {
        Authorization: 'Bearer sk-wtehfqfabhdhcwuvqrmaqvvwkxotzsmlwnfgfeywbsdgbfoz', 
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: 'THUDM/GLM-4-32B-0414',
        messages: [{
          role: 'user',
          content: `请翻译以下内容为中文：\n\n${fileContent.substring(0, 115000)}，请你只生成文字内容，我不要markdown格式，`
        }],
        stream: false,
        max_tokens: 8192,  
        temperature: 0.5   
      })
    };


    const timeoutPromise = new Promise((_, reject) => 
      setTimeout(() => reject(new Error('请求超时')), 150000)); 
    const llmResponse = await Promise.race([
      fetch('https://api.siliconflow.cn/v1/chat/completions', llmOptions),
      timeoutPromise
    ]);
    
    if (!llmResponse.ok) {
      throw new Error(`LLM接口错误: HTTP ${llmResponse.status}`);
    }

    const llmResult = await llmResponse.json();
    

    $scope.llmOutput1 = llmResult.choices?.[0]?.message?.content 
      || '未获取到有效响应';
    
  } catch (err) {
    console.error('处理失败:', err);
    $scope.llmOutput1 = `错误: ${err.message || '未知错误'}`;
    
    // 特殊处理常见错误
    if (err.message.includes('超时')) {
      $scope.llmOutput1 = '请求超时，请稍后重试';
    } else if (err.message.includes('401')) {
      $scope.llmOutput1 = '认证失败，请检查API密钥';
    }
    
  } finally {
    $scope.isLoading1 = false;
    $scope.$evalAsync(); // 确保最终状态更新
  }
};
  

  /**
   * Print the file.
   */
  $scope.printFile = function () {
    var popup = window.open('../api/file/' + $stateParams.fileId + '/data', '_blank');
    popup.onload = function () {
      popup.print();
      popup.close();
    }
  };

  /**
   * Close the file preview.
   */
  $scope.closeFile = function () {
    $uibModalInstance.dismiss();
  };

  // Close the modal when the user exits this state
  var off = $transitions.onStart({}, function(transition) {
    if (!$uibModalInstance.closed) {
      if (transition.to().name === $state.current.name) {
        $uibModalInstance.close();
      } else {
        $uibModalInstance.dismiss();
      }
    }
    off();
  });

  /**
   * Return true if we can display the preview image.
   */
  $scope.canDisplayPreview = function () {
    return $scope.file && $scope.file.mimetype !== 'application/pdf';
  };
});