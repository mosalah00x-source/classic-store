import type ar from './shared/i18n/messages/ar.json';

type Messages = typeof ar;

declare global {
  // Use type safe message keys with `next-intl`
  interface IntlMessages extends Messages {}
}
