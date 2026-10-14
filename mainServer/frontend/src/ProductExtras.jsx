import { useEffect, useState } from 'react';
import { Upload, X } from 'lucide-react';
import { amount, discountedPrice } from './lib/format';
import { useI18n } from './lib/i18n';
import './product-extras.css';

export function ProductPrice({ product }) {
  const { t } = useI18n();
  const price = discountedPrice(product);
  return <span className="price-display">{Number(product.discount) > 0 && <del>{amount(product.price)}</del>}<strong>{amount(price)} <small>{t('extras.units')}</small></strong></span>;
}

export function ImageUpload({ file, onChange, disabled }) {
  const { t } = useI18n();
  const [preview, setPreview] = useState('');
  const [error, setError] = useState('');
  useEffect(() => {
    if (!file) { setPreview(''); return; }
    const url = URL.createObjectURL(file);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);
  return <div className="image-upload"><label className="field"><span>{t('extras.imageLabel')}</span><div className="upload-picker"><Upload size={18} /><span>{t('extras.chooseImage')}</span><input type="file" accept="image/jpeg,image/png" disabled={disabled} onChange={(event) => {
    const selected = event.target.files?.[0]; setError('');
    if (selected && (!['image/jpeg', 'image/png'].includes(selected.type) || selected.size > 5 * 1024 * 1024)) {
      setError(t('extras.imageError')); event.target.value = ''; onChange(null); return;
    }
    onChange(selected || null);
  }} /></div></label>{error && <p role="alert" className="danger">{error}</p>}{preview && <div className="upload-preview"><img src={preview} alt={t('extras.previewAlt')} /><span>{file.name}</span><button type="button" className="icon-button" disabled={disabled} aria-label={t('extras.removeImage')} onClick={() => onChange(null)}><X size={16} /></button></div>}</div>;
}
