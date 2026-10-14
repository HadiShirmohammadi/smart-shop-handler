import test from 'node:test';
import assert from 'node:assert/strict';
import { dictionaries, dirFor, LANGS } from './i18n.messages.js';

test('exposes English and Persian with matching keys', () => {
  assert.deepEqual(LANGS, ['en', 'fa']);
  assert.deepEqual(Object.keys(dictionaries.fa).sort(), Object.keys(dictionaries.en).sort());
});

test('maps language to text direction', () => {
  assert.equal(dirFor('en'), 'ltr');
  assert.equal(dirFor('fa'), 'rtl');
});

test('interpolates named placeholders without leaving tokens behind', () => {
  const render = (template, vars) => Object.entries(vars).reduce((acc, [name, value]) => acc.split(`{${name}}`).join(String(value)), template);
  assert.equal(render(dictionaries.en['catalog.count'], { count: 7 }), '7 products to discover');
  assert.equal(render(dictionaries.fa['cart.subtotalUnits'], { value: '1,200' }), '1,200 واحد');
});

test('keeps the discount placeholder in both languages', () => {
  assert.match(dictionaries.en['catalog.off'], /\{discount\}/);
  assert.match(dictionaries.fa['catalog.off'], /\{discount\}/);
});