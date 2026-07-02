import js from '@eslint/js'
import ts from 'typescript-eslint'
import vue from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import prettier from 'eslint-config-prettier'

export default [
  {
    ignores: ['node_modules', 'dist', 'coverage', 'test-results', '.turbo']
  },
  js.configs.recommended,
  ...ts.configs.recommended,
  ...vue.configs['flat/recommended'],
  prettier,
  {
    files: ['**/*.{js,jsx,ts,tsx,vue}'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: ts.parser,
        sourceType: 'module',
        ecmaVersion: 'latest'
      },
      globals: {
        console: 'readonly',
        process: 'readonly'
      }
    },
    rules: {
      'vue/multi-word-component-names': [
        'warn',
        {
          ignores: ['Home']
        }
      ],
      'vue/singleline-html-element-content-newline': 'off',
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-unused-vars': [
        'warn',
        {
          argsIgnorePattern: '^_'
        }
      ],
      'no-console': [
        'warn',
        {
          allow: ['warn', 'error']
        }
      ]
    }
  }
]
