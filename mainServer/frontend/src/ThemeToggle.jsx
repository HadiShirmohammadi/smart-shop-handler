import { useEffect, useState } from 'react';
import { Moon, Sun } from 'lucide-react';
import './theme.css';

export default function ThemeToggle() {
  const [theme, setTheme] = useState(() => document.documentElement.dataset.theme || 'light');

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.style.colorScheme = theme;
    document.querySelector('meta[name="theme-color"]')?.setAttribute('content', theme === 'dark' ? '#131c17' : '#fafbf8');
    try { localStorage.setItem('gather-theme', theme); } catch { /* Theme switching works even when storage is blocked. */ }
  }, [theme]);

  useEffect(() => {
    const sync = (event) => {
      if (event.key === 'gather-theme' && ['light', 'dark'].includes(event.newValue)) setTheme(event.newValue);
    };
    window.addEventListener('storage', sync);
    return () => window.removeEventListener('storage', sync);
  }, []);

  const dark = theme === 'dark';
  return <button className="theme-toggle" type="button" aria-label={dark ? 'Switch to light theme' : 'Switch to dark theme'} aria-pressed={dark} title={dark ? 'Switch to light theme' : 'Switch to dark theme'} onClick={() => setTheme(dark ? 'light' : 'dark')}>
    {dark ? <Sun size={18} /> : <Moon size={18} />}
    <span>{dark ? 'Light mode' : 'Dark mode'}</span>
  </button>;
}
