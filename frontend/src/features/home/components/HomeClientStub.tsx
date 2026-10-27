'use client';

import { useTranslations } from 'next-intl';

export function HomeClientStub() {
  const t = useTranslations('common.cart');
  return (
    <div data-testid="client-stub">
      <p>{t('itemsCount', { count: 3 })}</p>
    </div>
  );
}
