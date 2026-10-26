'use client';

import { useTranslations } from 'next-intl';

export function ClientDemoComponent() {
  const t = useTranslations('home');

  return (
    <div
      data-testid="client-demo"
      style={{
        marginTop: '1rem',
        padding: '0.5rem',
        border: '1px dashed #ccc',
      }}
    >
      <p>{t('clientDemo')}</p>
    </div>
  );
}
