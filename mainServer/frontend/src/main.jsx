import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import { LanguageProvider, dictionaries } from './lib/i18n.jsx';
import './styles.css';
import './theme.css';
import './i18n.css';

class ErrorBoundary extends React.Component {
  state = { failed: false };
  static getDerivedStateFromError() { return { failed: true }; }
  render() {
    if (this.state.failed) {
      const lang = (typeof document !== 'undefined' && document.documentElement.lang === 'fa') ? 'fa' : 'en';
      const dict = dictionaries[lang] || dictionaries.en;
      return <main className="empty"><h1>{dict['error.title']}</h1><p>{dict['error.message']}</p><button onClick={() => location.reload()}>{dict['error.reload']}</button></main>;
    }
    return this.props.children;
  }
}

createRoot(document.getElementById('root')).render(<React.StrictMode><ErrorBoundary><LanguageProvider><App /></LanguageProvider></ErrorBoundary></React.StrictMode>);
