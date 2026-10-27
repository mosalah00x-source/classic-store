// OPEN-06: Arabic-Indic digits vs Latin digits
// Monetary amounts use Latin digits (numberingSystem: "latn") across all locales
// to match the static prototype reference.

const MONEY_PATTERN = /^\d+\.\d{2}$/;

export function formatMoney(
  amount: string,
  currency: 'EGP' = 'EGP',
  locale: string = 'ar'
): string {
  if (typeof amount !== 'string' || !MONEY_PATTERN.test(amount)) {
    if (process.env.NODE_ENV !== 'production') {
      throw new Error(
        `[formatMoney] Invalid money amount string: "${amount}". Amount must be a string with exactly 2 decimal places (e.g. "712.50").`
      );
    }
    console.error(
      `[formatMoney] Invalid money amount string: "${amount}". Rendering safe fallback.`
    );
    return locale === 'ar' ? `0.00 ${currency}` : `${currency} 0.00`;
  }

  const formatter = new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    currencyDisplay: 'code',
    numberingSystem: 'latn',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  return formatter.format(amount as unknown as number);
}
