import type ar from './shared/i18n/messages/ar.json';

type Messages = typeof ar;

declare global {
  // eslint-disable-next-line @typescript-eslint/no-empty-object-type
  interface IntlMessages extends Messages {}
}
