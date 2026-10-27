// OPEN-06: Using Latin digits (numberingSystem: "latn") for numbers across all locales

export function formatNumber(
  value: number | string,
  locale: string = 'ar'
): string {
  const formatter = new Intl.NumberFormat(locale, {
    numberingSystem: 'latn',
  });

  if (typeof value === 'string') {
    return formatter.format(value as unknown as number);
  }
  return formatter.format(value);
}

export function formatDiscountPercent(
  percent: number | string,
  locale: string = 'ar'
): string {
  const numStr =
    typeof percent === 'number' ? percent.toString() : percent.trim();
  const formatted = formatNumber(numStr, locale);
  return locale === 'ar' ? `%${formatted}` : `${formatted}%`;
}
