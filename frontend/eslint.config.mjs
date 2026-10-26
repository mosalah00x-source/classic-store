import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import nextPlugin from '@next/eslint-plugin-next';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import boundaries from 'eslint-plugin-boundaries';

export default tseslint.config(
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    plugins: {
      '@next/next': nextPlugin,
      'jsx-a11y': jsxA11y,
      boundaries,
    },
    settings: {
      'import/resolver': {
        typescript: {
          alwaysTryTypes: true,
          project: './tsconfig.json',
        },
      },
      'boundaries/include': ['src/**/*'],
      'boundaries/elements': [
        {
          type: 'app',
          pattern: 'src/app/**',
        },
        {
          type: 'features',
          pattern: 'src/features/*/**',
          capture: ['featureName'],
        },
        {
          type: 'shared',
          pattern: 'src/shared/**',
        },
        {
          type: 'server',
          pattern: 'src/server/**',
        },
        {
          type: 'mocks',
          pattern: 'src/mocks/**',
        },
      ],
    },
    rules: {
      ...nextPlugin.configs.recommended.rules,
      ...nextPlugin.configs['core-web-vitals'].rules,
      ...jsxA11y.configs.recommended.rules,

      // ADR-010 Layer & Feature Isolation Boundaries
      'boundaries/element-types': [
        'error',
        {
          default: 'disallow',
          rules: [
            {
              from: 'app',
              allow: ['features', 'shared', 'server'],
            },
            {
              from: 'features',
              allow: [
                'shared',
                ['features', { featureName: '${from.featureName}' }],
              ],
            },
            {
              from: 'shared',
              allow: ['shared'],
            },
            {
              from: 'server',
              allow: ['shared'],
            },
            {
              from: 'mocks',
              allow: ['shared'],
            },
          ],
        },
      ],

      // Disallow console.log (warn and error allowed; tokens/passwords must never be logged)
      'no-console': ['error', { allow: ['warn', 'error'] }],

      // Restrict direct access to browser storage globals
      'no-restricted-globals': [
        'error',
        {
          name: 'localStorage',
          message:
            'Do not access localStorage directly. Use theme/language storage helpers when approved in T03/T04.',
        },
        {
          name: 'sessionStorage',
          message:
            'Do not access sessionStorage directly. Use approved session state mechanisms.',
        },
      ],

      // Restrict float/number math on money identifiers (Standing Rule 7)
      'no-restricted-syntax': [
        'error',
        {
          selector:
            'CallExpression[callee.name="parseFloat"][arguments.0.name=/(price|total|amount|subtotal|discount)/i]',
          message:
            'Money values must remain exact formatted Strings with 2 decimal places. Do not parse money using parseFloat.',
        },
        {
          selector:
            'CallExpression[callee.name="Number"][arguments.0.name=/(price|total|amount|subtotal|discount)/i]',
          message:
            'Money values must remain exact formatted Strings with 2 decimal places. Do not cast money using Number().',
        },
      ],
    },
  },
  {
    ignores: ['.next/', 'node_modules/', 'coverage/', 'dist/', 'build/'],
  }
);
