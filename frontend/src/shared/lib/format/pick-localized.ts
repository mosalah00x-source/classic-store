type CapitalizedLocale = 'Ar' | 'En';

type LocalizedFields<BaseKey extends string> = {
  [K in `${BaseKey}${CapitalizedLocale}`]?: string | null;
};

/**
 * Utility to extract localized strings from entity objects with paired backend fields (e.g., titleAr/titleEn).
 * If requested locale value is missing/empty, falls back to the alternate locale value.
 * If both are missing/empty:
 *   - In dev/test: throws an error.
 *   - In production: logs console.error and returns an empty string or placeholder.
 */
export function pickLocalized<Entity extends object, BaseKey extends string>(
  entity: Entity & LocalizedFields<BaseKey>,
  baseKey: BaseKey,
  locale: string = 'ar'
): string {
  const isAr = locale === 'ar';
  const primaryKey = `${baseKey}${isAr ? 'Ar' : 'En'}` as keyof typeof entity;
  const fallbackKey = `${baseKey}${isAr ? 'En' : 'Ar'}` as keyof typeof entity;

  const primaryVal = entity[primaryKey];
  if (typeof primaryVal === 'string' && primaryVal.trim().length > 0) {
    return primaryVal.trim();
  }

  const fallbackVal = entity[fallbackKey];
  if (typeof fallbackVal === 'string' && fallbackVal.trim().length > 0) {
    return fallbackVal.trim();
  }

  if (process.env.NODE_ENV !== 'production') {
    throw new Error(
      `[pickLocalized] Both localized fields ("${String(primaryKey)}" and "${String(fallbackKey)}") are empty or missing for baseKey "${baseKey}".`
    );
  }

  console.error(
    `[pickLocalized] Missing localized text for baseKey "${baseKey}".`
  );
  return '';
}
