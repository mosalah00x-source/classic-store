import { setRequestLocale, getTranslations } from 'next-intl/server';
import { NextIntlClientProvider } from 'next-intl';
import { notFound } from 'next/navigation';
import { routing, type Locale } from '@/shared/i18n/routing';
import { HomeClientStub } from '@/features/home/components/HomeClientStub';

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

export default async function HomePage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;

  if (!routing.locales.includes(locale as Locale)) {
    notFound();
  }

  setRequestLocale(locale);

  const t = await getTranslations('home');

  // Pick ONLY the namespace subset for the client component payload optimization
  const messages = {
    common: {
      cart: {
        itemsCount: (await getTranslations('common.cart'))('itemsCount'),
      },
    },
  };

  return (
    <main style={{ padding: '2rem', textAlign: 'center' }}>
      <h1>{t('title')}</h1>
      <p>{t('tagline')}</p>
      <NextIntlClientProvider messages={messages} locale={locale}>
        <HomeClientStub />
      </NextIntlClientProvider>
    </main>
  );
}
