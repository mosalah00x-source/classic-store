import { getRequestConfig } from 'next-intl/server';
import { notFound } from 'next/navigation';
import { routing } from './routing';

export default getRequestConfig(async ({ requestLocale }) => {
  let locale = await requestLocale;

  if (!locale || !routing.locales.includes(locale as any)) {
    notFound();
  }

  return {
    locale,
    messages: (await import(`./messages/${locale}.json`)).default,
    onError(error) {
      if (process.env.NODE_ENV === 'development') {
        throw error;
      }
      console.error('[i18n] Missing key or translation error:', error);
    },
    getMessageFallback({ key }) {
      return key;
    },
  };
});
