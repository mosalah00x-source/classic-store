import type { Metadata } from 'next';
import { Cairo } from 'next/font/google';
import { notFound } from 'next/navigation';
import { setRequestLocale } from 'next-intl/server';
import type { ReactNode } from 'react';
import { routing } from '@/shared/i18n/routing';

const cairo = Cairo({
  subsets: ['arabic', 'latin'],
  display: 'swap',
  variable: '--font-cairo',
});

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string }>;
}): Promise<Metadata> {
  const { locale } = await params;
  const isAr = locale === 'ar';
  return {
    title: isAr
      ? 'كلاسيك ستور - المتجر الإلكتروني'
      : 'ClassicStore - Online Store',
    description: isAr
      ? 'متجر إلكتروني رائد في مصر والشرق الأوسط'
      : 'Leading e-commerce store in Egypt and MENA',
  };
}

export default async function LocaleLayout({
  children,
  params,
}: {
  children: ReactNode;
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;

  if (!routing.locales.includes(locale as any)) {
    notFound();
  }

  setRequestLocale(locale);

  const dir = locale === 'ar' ? 'rtl' : 'ltr';

  return (
    <html lang={locale} dir={dir} className={cairo.variable}>
      <body className={cairo.className}>{children}</body>
    </html>
  );
}
