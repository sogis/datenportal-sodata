import {describe, expect, it} from 'vitest';
import {sqlResultSnapshotFromQueryResult} from './sqlResultSnapshot';
import type {QueryResultState} from './queryResultTypes';
import type {ExploreTableDto} from '../app/ExploreContext';

describe('sqlResultSnapshotFromQueryResult', () => {
  it('creates a typed, JSON-safe snapshot from a successful SQL result', () => {
    const result: QueryResultState = {
      status: 'success',
      sourceSql: 'select * from daten',
      executedSql: 'select * from daten limit 1000',
      columns: ['objekt_id', 'betrag', 'datum', 'zeitpunkt', 'geom', 'anzahl', 'nitrat'],
      rows: [
        {
          objekt_id: 9007199254740999n,
          betrag: '123456789.1234',
          datum: new Date('2026-07-06T00:00:00Z'),
          zeitpunkt: new Date('2026-07-06T12:34:56Z'),
          geom: new Uint8Array([1, 2]),
          anzahl: 7,
          nitrat: null
        }
      ],
      rowCount: 1
    };
    const tables: ExploreTableDto[] = [{
      id: 'daten',
      name: 'daten',
      title: 'Daten',
      parquetUrl: '/daten.parquet',
      primary: true,
      columns: [
        {name: 'objekt_id', type: 'BIGINT', nullable: false, roles: ['identifier']},
        {name: 'betrag', type: 'DECIMAL(18,4)', nullable: true, roles: ['measure']},
        {name: 'datum', type: 'DATE', nullable: true, roles: ['date']},
        {name: 'zeitpunkt', type: 'TIMESTAMP', nullable: true, roles: ['date']},
        {name: 'geom', type: 'GEOMETRY', nullable: true, roles: ['geometry']},
        {name: 'anzahl', type: 'INTEGER', nullable: false, roles: ['measure']},
        {name: 'nitrat', type: 'DOUBLE', nullable: true, roles: ['measure']}
      ]
    }];

    const snapshot = sqlResultSnapshotFromQueryResult(result, tables);

    expect(snapshot.columns.map((column) => [column.name, column.rType])).toEqual([
      ['objekt_id', 'character'],
      ['betrag', 'character'],
      ['datum', 'Date'],
      ['zeitpunkt', 'POSIXct'],
      ['geom', 'character'],
      ['anzahl', 'integer'],
      ['nitrat', 'numeric']
    ]);
    expect(snapshot.rows[0]).toEqual([
      '9007199254740999',
      '123456789.1234',
      '2026-07-06',
      '2026-07-06T12:34:56.000Z',
      '[2 Bytes]',
      7,
      null
    ]);
  });
});
