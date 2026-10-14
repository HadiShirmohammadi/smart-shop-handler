import test from 'node:test';
import assert from 'node:assert/strict';
import { amount, cartTotal, discountedPrice, imageUrl, isAdmin, tagsOf } from './format.js';

test('normalizes DTO and entity tags', () => {
  assert.deepEqual(tagsOf({ tags: ['Home', { name: 'Gift' }] }), ['Home', 'Gift']);
  assert.deepEqual(tagsOf({}), []);
});
test('handles backend admin property variants without trusting strings', () => {
  assert.equal(isAdmin({ admin: true }), true);
  assert.equal(isAdmin({ isAdmin: true }), true);
  assert.equal(isAdmin({ admin: 'true' }), false);
  assert.equal(isAdmin(null), false);
});
test('permits HTTP URLs and safe uploaded image paths', () => {
  assert.equal(imageUrl('/product/images/12345678-1234-1234-1234-123456789abc.png'), '/backend/product/images/12345678-1234-1234-1234-123456789abc.png');
  assert.equal(imageUrl('/product/images/../../secret.png'), '');
  assert.equal(imageUrl('https://example.com/product.jpg'), 'https://example.com/product.jpg');
  assert.equal(imageUrl('javascript:alert(1)'), '');
  assert.equal(imageUrl('data:text/html,unsafe'), '');
  assert.equal(imageUrl(null), '');
});
test('applies percentage discounts and rounds unit prices consistently', () => {
  assert.equal(discountedPrice({ price: 1000, discount: 20 }), 800);
  assert.equal(discountedPrice({ price: 1000, discount: 100 }), 0);
  assert.equal(discountedPrice({ price: 125, discount: 10 }), 113);
  assert.equal(discountedPrice({ price: 1000, discount: 0 }), 1000);
  assert.equal(discountedPrice({ price: 1000, discount: 200 }), 0);
});
test('calculates cart totals with percentage discounts', () => {
  assert.equal(cartTotal([{ quantity: 2, price: 0, product: { price: 125, discount: 10 } }, { quantity: 1, product: { price: 50 } }]), 276);
  assert.equal(cartTotal([]), 0);
});
test('formats backend price units', () => {
  assert.equal(amount(15000), '15,000');
  assert.equal(amount(undefined), '0');
});
