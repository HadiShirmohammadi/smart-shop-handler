export const amount = (value) => new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(Number(value) || 0);
export const tagsOf = (product) => (product.tags || []).map((tag) => typeof tag === 'string' ? tag : tag.name).filter(Boolean);
export const isAdmin = (user) => user?.admin === true || user?.isAdmin === true;
export function discountedPrice(product) {
  const price = Math.max(0, Number(product?.price) || 0);
  const discount = Math.min(100, Math.max(0, Number(product?.discount) || 0));
  return Math.round(price * (100 - discount) / 100);
}
export function imageUrl(value) {
  if (typeof value !== 'string') return '';
  if (/^\/product\/images\/[0-9a-f-]{36}\.(jpg|png)$/.test(value)) {
    return `${(import.meta.env?.VITE_API_BASE_URL || '/backend').replace(/\/$/, '')}${value}`;
  }
  try { const url = new URL(value); return ['https:', 'http:'].includes(url.protocol) ? url.href : ''; } catch { return ''; }
}
export function cartTotal(items) {
  return items.reduce((total, item) => total + discountedPrice(item.product) * Number(item.quantity || 0), 0);
}
