import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'
import globals from 'globals'
import prettierConfig from 'eslint-config-prettier'

export default tseslint.config(
  // 不检查的东西
  { ignores: ['dist/**', 'node_modules/**', '*.config.js'] },

  // 三套规则集
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/essential'],

  // .vue 文件里的 <script lang="ts"> 要用 TS 解析器
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: { parser: tseslint.parser }
    }
  },

  // 运行环境：浏览器（认识 window / document 这些全局变量）
  {
    languageOptions: {
      globals: globals.browser
    }
  },

  // ⚠️ Prettier 配置放【最后】—— 它会关掉格式相关的规则
  prettierConfig,

  // 自己调整几条
  {
    rules: {
      'vue/multi-word-component-names': 'off',        // Navbar / Sidebar 这类布局组件不是 HTML 标签名，误报，关闭
      '@typescript-eslint/no-explicit-any': 'off',        // 渐进迁移中，允许 any
      '@typescript-eslint/no-unused-vars': ['error', {      // 没用到的变量：错误（需要保留时加 _ 前缀)
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_'
      }]
    }
  }
)
