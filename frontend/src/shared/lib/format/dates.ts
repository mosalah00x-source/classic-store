const TIMEZONE = 'Africa/Cairo';

export function formatDate(isoDateStr: string, locale: string = 'ar'): string {
  if (!isoDateStr) return '';
  const date = new Date(isoDateStr);
  if (isNaN(date.getTime())) {
    if (process.env.NODE_ENV !== 'production') {
      throw new Error(`[formatDate] Invalid ISO date string: "${isoDateStr}"`);
    }
    console.error(`[formatDate] Invalid ISO date string: "${isoDateStr}"`);
    return isoDateStr;
  }

  const formatter = new Intl.DateTimeFormat(locale, {
    timeZone: TIMEZONE,
    calendar: 'gregory',
    numberingSystem: 'latn',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return formatter.format(date);
}

export function formatDateTime(
  isoDateStr: string,
  locale: string = 'ar'
): string {
  if (!isoDateStr) return '';
  const date = new Date(isoDateStr);
  if (isNaN(date.getTime())) {
    if (process.env.NODE_ENV !== 'production') {
      throw new Error(
        `[formatDateTime] Invalid ISO date string: "${isoDateStr}"`
      );
    }
    console.error(`[formatDateTime] Invalid ISO date string: "${isoDateStr}"`);
    return isoDateStr;
  }

  const formatter = new Intl.DateTimeFormat(locale, {
    timeZone: TIMEZONE,
    calendar: 'gregory',
    numberingSystem: 'latn',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return formatter.format(date);
}
