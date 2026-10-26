import { NextIntlClientProvider } from 'next-intl';
import {
  getMessages,
  getTranslations,
  setRequestLocale,
} from 'next-intl/server';
import { notFound } from 'next/navigation';
import { ClientDemoComponent } from '@/features/home/ClientDemoComponent';
import { routing } from '@/shared/i18n/routing';

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

export default async function HomePage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;

  if (!routing.locales.includes(locale as any)) {
    notFound();
  }

  setRequestLocale(locale);

  const t = await getTranslations('home');
  const allMessages = await getMessages();

  // Pick ONLY the 'home' namespace for client provider to optimize payload
  const clientMessages = {
    home: (allMessages as any).home,
  };

  return (
    <main style={{ padding: '2rem', fontFamily: 'var(--font-cairo)' }}>
      <h1 data-testid="page-title">{t('title')}</h1>
      <p data-testid="page-description">{t('description')}</p>

      <NextIntlClientProvider messages={clientMessages}>
        <ClientDemoComponent />
      </NextIntlClientProvider>
    </main>
  );
}
