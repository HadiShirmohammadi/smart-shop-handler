import { Languages } from 'lucide-react';
import { useI18n } from './lib/i18n';
import './i18n.css';

export default function LanguageToggle() {
  const { lang, setLang, t } = useI18n();
  const next = lang === 'en' ? 'fa' : 'en';
  const label = lang === 'en' ? 'فارسی' : 'English';
  return <button className="lang-toggle" type="button" aria-label={t('lang.switchAria')} title={t('lang.switchAria')} onClick={() => setLang(next)}>
    <Languages size={17} />
    <span className="lang-code">{label}</span>
  </button>;
}