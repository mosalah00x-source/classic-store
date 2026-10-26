import ar from './messages/ar.json';

type Messages = typeof ar;

declare global {
  // Use type safe message keys across next-intl
  interface IntlMessages extends Messages {}
}
