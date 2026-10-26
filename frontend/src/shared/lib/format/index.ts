const MONEY_PATTERN = /^\d+\.\d{2}$/;

/**
 * Formats a monetary string (e.g., "950.00") using Intl.NumberFormat.
 * Accepts ONLY string amounts with exactly two decimal places.
 * Uses Latin digits ('latn') in both Arabic and English locales.
 *
 * // OPEN-01: Arabic-Indic digits vs Latin digits choice.
 */
export function formatMoney(
  amount: string,
  currency: 'EGP' = 'EGP',
  locale: string = 'ar'
): string {
  if (typeof amount !== 'string' || !MONEY_PATTERN.test(amount)) {
    if (process.env.NODE_ENV !== 'production') {
      throw new Error(
        `[formatMoney] Invalid amount format: "${amount}". Expected string matching pattern "^\\d+\\.\\d{2}$".`
      );
    }
    return `${amount ?? ''} ${currency}`;
  }

  // Use Intl.NumberFormat with string formatting or BigInt/parts where supported without float rounding
  // In modern JS (Node 22, V8, modern browsers), Intl.NumberFormat supports format(string) or string parsing safely.
  // To avoid parseFloat / float precision issues, format directly or parse integer/fractional parts.
  const [integerPart, decimalPart] = amount.split('.');
  const bigIntValue = BigInt(integerPart);

  const formatter = new Intl.NumberFormat(`${locale}-u-nu-latn`, {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  // Format BigInt integer part, then replace decimal fraction if needed
  const formattedInt = formatter.format(bigIntValue);
  // Replace integer zero/value decimal component with exact decimalPart
  return formattedInt.replace(/\d+([.,]\d+)?/, (match) => {
    // If currency symbol or punctuation is around match
    const parts = formatter.formatToParts(bigIntValue);
    const decimalSep = parts.find((p) => p.type === 'decimal')?.value ?? '.';
    const formattedIntegerStr = new Intl.NumberFormat(`${locale}-u-nu-latn`, {
      useGrouping: true,
    }).format(bigIntValue);

    return `${formattedIntegerStr}${decimalSep}${decimalPart}`;
  });
}

/**
 * Formats a discount percentage.
 */
export function formatDiscountPercent(
  value: number | string,
  locale: string = 'ar'
): string {
  const num = typeof value === 'string' ? Number(value) : value;
  const formatter = new Intl.NumberFormat(`${locale}-u-nu-latn`, {
    style: 'percent',
    maximumFractionDigits: 2,
  });
  return formatter.format(num / 100);
}

/**
 * Formats a generic number with Latin digits.
 */
export function formatNumber(value: number, locale: string = 'ar'): string {
  const formatter = new Intl.NumberFormat(`${locale}-u-nu-latn`);
  return formatter.format(value);
}

/**
 * Formats an ISO-8601 date string in Africa/Cairo timezone with Gregorian calendar.
 */
export function formatDate(isoString: string, locale: string = 'ar'): string {
  const date = new Date(isoString);
  const formatter = new Intl.DateTimeFormat(`${locale}-u-nu-latn-ca-gregory`, {
    timeZone: 'Africa/Cairo',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
  return formatter.format(date);
}

/**
 * Formats an ISO-8601 datetime string in Africa/Cairo timezone with Gregorian calendar.
 */
export function formatDateTime(
  isoString: string,
  locale: string = 'ar'
): string {
  const date = new Date(isoString);
  const formatter = new Intl.DateTimeFormat(`${locale}-u-nu-latn-ca-gregory`, {
    timeZone: 'Africa/Cairo',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
  return formatter.format(date);
}
