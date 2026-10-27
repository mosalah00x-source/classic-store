export function pickLocalized<
  T extends Record<string, unknown>,
  K extends string,
>(entity: T | null | undefined, baseKey: K, locale: string): string {
  if (!entity) {
    if (process.env.NODE_ENV !== 'production') {
      throw new Error('[pickLocalized] Entity is null or undefined');
    }
    console.error('[pickLocalized] Entity is null or undefined');
    return '';
  }

  const isArabic = locale === 'ar';

  const primaryCamel = `${baseKey}${isArabic ? 'Ar' : 'En'}`;
  const fallbackCamel = `${baseKey}${isArabic ? 'En' : 'Ar'}`;

  const primarySnake = `${baseKey}_${isArabic ? 'ar' : 'en'}`;
  const fallbackSnake = `${baseKey}_${isArabic ? 'en' : 'ar'}`;

  let primaryVal: unknown;
  let fallbackVal: unknown;

  if (primaryCamel in entity || fallbackCamel in entity) {
    primaryVal = entity[primaryCamel];
    fallbackVal = entity[fallbackCamel];
  } else if (primarySnake in entity || fallbackSnake in entity) {
    primaryVal = entity[primarySnake];
    fallbackVal = entity[fallbackSnake];
  }

  const primaryStr = typeof primaryVal === 'string' ? primaryVal.trim() : '';
  const fallbackStr = typeof fallbackVal === 'string' ? fallbackVal.trim() : '';

  if (primaryStr) {
    return primaryStr;
  }
  if (fallbackStr) {
    return fallbackStr;
  }

  if (process.env.NODE_ENV !== 'production') {
    throw new Error(
      `[pickLocalized] Missing localized values for field "${baseKey}" in both locales.`
    );
  }

  console.error(
    `[pickLocalized] Missing localized values for field "${baseKey}" in both locales.`
  );
  return '';
}
