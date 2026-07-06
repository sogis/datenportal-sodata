import {describe, expect, it} from 'vitest';
import {mapDuckDbColumnToR, normalizeDuckDbValueForR} from './DuckDbToWebRTypeMapper';

describe('mapDuckDbColumnToR', () => {
  it('keeps risky DuckDB types lossless as character', () => {
    expect(mapDuckDbColumnToR({name: 'id', duckdbType: 'BIGINT', nullable: false, roles: ['identifier']})).toMatchObject({
      rType: 'character',
      warning: expect.stringContaining('Identifier')
    });
    expect(mapDuckDbColumnToR({name: 'betrag', duckdbType: 'DECIMAL(18, 4)', nullable: true, roles: ['measure']})).toMatchObject({
      rType: 'character',
      warning: expect.stringContaining('DECIMAL')
    });
    expect(mapDuckDbColumnToR({name: 'geom', duckdbType: 'GEOMETRY', nullable: true, roles: ['geometry']})).toMatchObject({
      rType: 'character',
      warning: expect.stringContaining('Geometrien')
    });
  });

  it('maps common scalar types to conservative R types', () => {
    expect(mapDuckDbColumnToR({name: 'anzahl', duckdbType: 'INTEGER', nullable: false, roles: ['measure']}).rType).toBe('integer');
    expect(mapDuckDbColumnToR({name: 'nitrat', duckdbType: 'DOUBLE', nullable: true, roles: ['measure']}).rType).toBe('numeric');
    expect(mapDuckDbColumnToR({name: 'aktiv', duckdbType: 'BOOLEAN', nullable: true, roles: ['unknown']}).rType).toBe('logical');
    expect(mapDuckDbColumnToR({name: 'datum', duckdbType: 'DATE', nullable: true, roles: ['date']}).rType).toBe('Date');
    expect(mapDuckDbColumnToR({name: 'ts', duckdbType: 'TIMESTAMP', nullable: true, roles: ['date']}).rType).toBe('POSIXct');
  });
});

describe('normalizeDuckDbValueForR', () => {
  it('serializes dates, bigints, binaries and nulls for JSON transfer', () => {
    expect(normalizeDuckDbValueForR(null, {rType: 'character'})).toBeNull();
    expect(normalizeDuckDbValueForR(123n, {rType: 'character'})).toBe('123');
    expect(normalizeDuckDbValueForR(new Date('2026-07-06T10:11:12Z'), {rType: 'Date'})).toBe('2026-07-06');
    expect(normalizeDuckDbValueForR(new Date('2026-07-06T10:11:12Z'), {rType: 'POSIXct'})).toBe('2026-07-06T10:11:12.000Z');
    expect(normalizeDuckDbValueForR(new Uint8Array([1, 2, 3]), {rType: 'character'})).toBe('[3 Bytes]');
  });
});
