import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const messagesDir = path.join(__dirname, '../src/shared/i18n/messages');
const arPath = path.join(messagesDir, 'ar.json');
const enPath = path.join(messagesDir, 'en.json');

function loadJson(filepath) {
  try {
    return JSON.parse(fs.readFileSync(filepath, 'utf8'));
  } catch (e) {
    console.error(`Failed to load JSON from ${filepath}:`, e);
    process.exit(1);
  }
}

function getFlattenedKeys(obj, prefix = '') {
  let keys = {};
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      const fullKey = prefix ? `${prefix}.${key}` : key;
      if (typeof obj[key] === 'object' && obj[key] !== null) {
        Object.assign(keys, getFlattenedKeys(obj[key], fullKey));
      } else {
        keys[fullKey] = obj[key];
      }
    }
  }
  return keys;
}

function extractICUPlaceholders(str) {
  const placeholders = new Set();
  const regex = /\{([^,}]+)/g;
  let match;
  while ((match = regex.exec(str)) !== null) {
    placeholders.add(match[1].trim());
  }
  return placeholders;
}

const arJson = loadJson(arPath);
const enJson = loadJson(enPath);

const arKeys = getFlattenedKeys(arJson);
const enKeys = getFlattenedKeys(enJson);

let errors = [];

// Check key parity
const arKeySet = new Set(Object.keys(arKeys));
const enKeySet = new Set(Object.keys(enKeys));

for (const key of arKeySet) {
  if (!enKeySet.has(key)) {
    errors.push(`Missing key in en.json: "${key}"`);
  }
}

for (const key of enKeySet) {
  if (!arKeySet.has(key)) {
    errors.push(`Missing key in ar.json: "${key}"`);
  }
}

// Check non-empty values and ICU placeholders
for (const key of arKeySet) {
  if (enKeySet.has(key)) {
    const arVal = String(arKeys[key]).trim();
    const enVal = String(enKeys[key]).trim();

    if (!arVal) {
      errors.push(`Empty value in ar.json for key: "${key}"`);
    }
    if (!enVal) {
      errors.push(`Empty value in en.json for key: "${key}"`);
    }

    const arPlaceholders = extractICUPlaceholders(arVal);
    const enPlaceholders = extractICUPlaceholders(enVal);

    for (const ph of arPlaceholders) {
      if (!enPlaceholders.has(ph)) {
        errors.push(
          `ICU placeholder mismatch for key "${key}": "${ph}" in ar but missing in en`
        );
      }
    }
    for (const ph of enPlaceholders) {
      if (!arPlaceholders.has(ph)) {
        errors.push(
          `ICU placeholder mismatch for key "${key}": "${ph}" in en but missing in ar`
        );
      }
    }
  }
}

if (errors.length > 0) {
  console.error('❌ i18n key parity check failed:');
  for (const err of errors) {
    console.error(`  - ${err}`);
  }
  process.exit(1);
}

console.log('✅ i18n key parity and ICU validation passed successfully.');
