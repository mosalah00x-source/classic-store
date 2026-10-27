import { getRequestConfig } from 'next-intl/server';
import { routing } from './routing';

export default getRequestConfig(async ({ requestLocale }) => {
  let locale = await requestLocale;

  if (!locale || !routing.locales.includes(locale as 'ar' | 'en')) {
    locale = routing.defaultLocale;
  }

  return {
    locale,
    messages: (await import(`./messages/${locale}.json`)).default,
    onError(error) {
      if (process.env.NODE_ENV !== 'production') {
        throw error;
      }
      console.error('[i18n error]', error);
    },
    getMessageFallback({ key, namespace }) {
      const fullKey = namespace ? `${namespace}.${key}` : key;
      if (process.env.NODE_ENV !== 'production') {
        throw new Error(`[i18n] Missing message key: "${fullKey}"`);
      }
      console.error(`[i18n] Missing message key in production: "${fullKey}"`);
      return fullKey;
    },
  };
});
