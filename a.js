const fetch = require('node-fetch'); // 如果你在 Node.js 环境下使用，需要安装 node-fetch

const options = {
  method: 'POST',
  headers: {
    Authorization: 'Bearer sk-wtehfqfabhdhcwuvqrmaqvvwkxotzsmlwnfgfeywbsdgbfoz', // ✅ 替换成你的真实 API Token，去掉 < >
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    model: 'Qwen/QwQ-32B',
    messages: [
      {
        role: 'user',
        content: 'What opportunities and challenges will the Chinese large model industry face in 2025?'
      }
    ],
    stream: false,
    max_tokens: 512,
    enable_thinking: false,
    thinking_budget: 4096,
    min_p: 0.05,
    stop: null,
    temperature: 0.7,
    top_p: 0.7,
    top_k: 50,
    frequency_penalty: 0.5,
    n: 1,
    response_format: { type: 'text' },
    tools: [
      {
        type: 'function',
        function: {
          description: '<string>',
          name: '<string>',
          parameters: {},
          strict: false
        }
      }
    ]
  })
};

fetch('https://api.siliconflow.cn/v1/chat/completions', options)
  .then(response => {
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return response.json();
  })
  .then(response => {
    console.log('LLM response:', JSON.stringify(response, null, 2));

  })
  .catch(err => {
    console.error('Request failed:', err);
  });
