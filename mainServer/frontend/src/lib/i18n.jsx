import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { LANGS, dictionaries, dirFor } from './i18n.messages.js';

export { LANGS, dictionaries, dirFor };

const STORAGE_KEY = 'gather-lang';

const I18nContext = createContext(null);

function detectInitialLang() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (LANGS.includes(saved)) return saved;
  } catch { /* Fall back to the document/locale hint. */ }
  const documentLang = (typeof document !== 'undefined' && document.documentElement.lang) || 'en';
  return documentLang.toLowerCase().startsWith('fa') ? 'fa' : 'en';
}

export function LanguageProvider({ children }) {
  const [lang, setLangState] = useState(detectInitialLang);
  const dir = dirFor(lang);

  useEffect(() => {
    document.documentElement.lang = lang;
    document.documentElement.dir = dir;
    try { localStorage.setItem(STORAGE_KEY, lang); } catch { /* Language still applies for this session. */ }
  }, [lang, dir]);

  useEffect(() => {
    const sync = (event) => { if (event.key === STORAGE_KEY && LANGS.includes(event.newValue)) setLangState(event.newValue); };
    window.addEventListener('storage', sync);
    return () => window.removeEventListener('storage', sync);
  }, []);

  const setLang = useCallback((next) => { if (LANGS.includes(next)) setLangState(next); }, []);

  const t = useCallback((key, vars) => {
    const dict = dictionaries[lang] || dictionaries.en;
    let value = dict[key] ?? dictionaries.en[key] ?? key;
    if (vars) for (const [name, replacement] of Object.entries(vars)) value = value.split(`{${name}}`).join(String(replacement));
    return value;
  }, [lang]);

  const value = useMemo(() => ({ lang, dir, t, setLang }), [lang, dir, t, setLang]);
  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n() {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error('useI18n must be used within a LanguageProvider');
  return ctx;
}