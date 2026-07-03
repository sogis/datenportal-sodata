import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import {
  copySqlForResult,
  exportQueryResult,
  exportQueryResultWithDuckDbCopy,
  exportRowsToCsv,
  resultCsvFilename,
  resultExportFilename,
  rowsToCsv,
  sanitizeCsvFilename
} from './ResultExport';
import type {QueryResultState} from './queryResultTypes';

describe('ResultExport', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('exports semicolon-delimited CSV with escaped values and CRLF rows', () => {
    const csv = rowsToCsv(
      ['name', 'bemerkung', 'leer', 'gross'],
      [
        {name: 'Solothurn', bemerkung: 'eins;zwei', leer: null, gross: 10n},
        {name: 'Olten', bemerkung: 'Text mit "Zitat"', leer: undefined, gross: 20n}
      ]
    );

    expect(csv).toBe('name;bemerkung;leer;gross\r\nSolothurn;"eins;zwei";;10\r\nOlten;"Text mit ""Zitat""";;20');
  });

  it('sanitizes filenames', () => {
    expect(resultCsvFilename('ch.so/bau inventar')).toBe('datenportal-ch.so-bau-inventar-result.csv');
    expect(resultExportFilename('ch.so/bau inventar', 'xlsx')).toBe('datenportal-ch.so-bau-inventar-result.xlsx');
    expect(resultExportFilename('ch.so/bau inventar', 'parquet')).toBe('datenportal-ch.so-bau-inventar-result.parquet');
    expect(sanitizeCsvFilename('messung')).toBe('messung.csv');
  });

  it('creates a CSV download link and revokes the object URL', () => {
    const {createObjectURL, revokeObjectURL, clickedDownloads, documentRef} = createDownloadHarness();

    exportRowsToCsv([{name: 'Solothurn'}], ['name'], 'result.csv', documentRef);

    expect(createObjectURL).toHaveBeenCalledWith(expect.any(Blob));
    expect(clickedDownloads).toEqual(['result.csv']);
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:result');
  });

  it('generates Parquet COPY SQL from the executed result SQL', () => {
    expect(copySqlForResult('select *\nfrom ch_so_bauinventar\nlimit 1000;', 'result.parquet', 'parquet')).toBe(`copy (
select *
from ch_so_bauinventar
limit 1000
) to 'result.parquet' with (format parquet);`);
  });

  it('generates XLSX COPY SQL with header and sheet name', () => {
    expect(copySqlForResult('select * from ch_so_bauinventar limit 1000;', "result's.xlsx", 'xlsx')).toBe(`copy (
select * from ch_so_bauinventar limit 1000
) to 'result''s.xlsx' with (format xlsx, header true, sheet 'Daten');`);
  });

  it('exports Parquet through DuckDB-Wasm COPY and downloads the virtual file', async () => {
    const {createObjectURL, clickedDownloads, documentRef} = createDownloadHarness();
    const connectorHarness = createWasmConnectorHarness(new Uint8Array([1, 2, 3]));

    await exportQueryResultWithDuckDbCopy(
      'select * from ch_so_bauinventar limit 1000',
      'parquet',
      'result.parquet',
      connectorHarness.connector,
      documentRef
    );

    expect(connectorHarness.execute).toHaveBeenNthCalledWith(1, 'install parquet;');
    expect(connectorHarness.execute).toHaveBeenNthCalledWith(2, 'load parquet;');
    expect(connectorHarness.execute).toHaveBeenNthCalledWith(3, expect.stringContaining('with (format parquet);'));
    expect(connectorHarness.copyFileToBuffer).toHaveBeenCalledWith(expect.stringMatching(/\.parquet$/));
    expect(connectorHarness.dropFile).toHaveBeenCalledWith(expect.stringMatching(/\.parquet$/));
    expect(createObjectURL).toHaveBeenCalledWith(expect.objectContaining({
      type: 'application/vnd.apache.parquet'
    }));
    expect(clickedDownloads).toEqual(['result.parquet']);
  });

  it('exports XLSX through DuckDB-Wasm COPY and downloads the virtual file', async () => {
    const {createObjectURL, clickedDownloads, documentRef} = createDownloadHarness();
    const connectorHarness = createWasmConnectorHarness(new Uint8Array([80, 75, 3, 4]));

    await exportQueryResultWithDuckDbCopy(
      'select * from ch_so_bauinventar limit 1000',
      'xlsx',
      'result.xlsx',
      connectorHarness.connector,
      documentRef
    );

    expect(connectorHarness.execute).toHaveBeenNthCalledWith(1, 'install excel;');
    expect(connectorHarness.execute).toHaveBeenNthCalledWith(2, 'load excel;');
    expect(connectorHarness.execute).toHaveBeenNthCalledWith(3, expect.stringContaining('format xlsx, header true'));
    expect(connectorHarness.copyFileToBuffer).toHaveBeenCalledWith(expect.stringMatching(/\.xlsx$/));
    expect(connectorHarness.dropFile).toHaveBeenCalledWith(expect.stringMatching(/\.xlsx$/));
    expect(createObjectURL).toHaveBeenCalledWith(expect.objectContaining({
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    }));
    expect(clickedDownloads).toEqual(['result.xlsx']);
  });

  it('exports CSV without requiring DuckDB and DuckDB formats with executedSql', async () => {
    const {clickedDownloads, documentRef} = createDownloadHarness();
    const connectorHarness = createWasmConnectorHarness(new Uint8Array([1]));
    const result: QueryResultState = {
      status: 'success',
      sourceSql: 'select * from ch_so_bauinventar',
      executedSql: 'select * from ch_so_bauinventar limit 1000',
      columns: ['gemeindename'],
      rows: [{gemeindename: 'Solothurn'}],
      rowCount: 1
    };

    await exportQueryResult(result, 'csv', 'ch.so.bauinventar', undefined, documentRef);
    await exportQueryResult(result, 'parquet', 'ch.so.bauinventar', connectorHarness.connector, documentRef);

    expect(clickedDownloads).toEqual([
      'datenportal-ch.so.bauinventar-result.csv',
      'datenportal-ch.so.bauinventar-result.parquet'
    ]);
    expect(connectorHarness.execute).toHaveBeenLastCalledWith(expect.stringContaining('select * from ch_so_bauinventar limit 1000'));
  });
});

function createDownloadHarness() {
  const createObjectURL = vi.fn().mockReturnValue('blob:result');
  const revokeObjectURL = vi.fn();
  vi.stubGlobal('URL', {createObjectURL, revokeObjectURL});
  const clickedDownloads: string[] = [];
  const documentRef = document.implementation.createHTMLDocument();
  const originalCreateElement = documentRef.createElement.bind(documentRef);
  vi.spyOn(documentRef, 'createElement').mockImplementation((tagName: string) => {
    const element = originalCreateElement(tagName);
    if (tagName === 'a') {
      Object.defineProperty(element, 'click', {
        value: () => clickedDownloads.push((element as HTMLAnchorElement).download)
      });
    }
    return element;
  });
  return {createObjectURL, revokeObjectURL, clickedDownloads, documentRef};
}

function createWasmConnectorHarness(bytes: Uint8Array) {
  const execute = vi.fn().mockResolvedValue(undefined);
  const flushFiles = vi.fn().mockResolvedValue(null);
  const copyFileToBuffer = vi.fn().mockResolvedValue(bytes);
  const dropFile = vi.fn().mockResolvedValue(null);
  const connector = {
    type: 'wasm',
    execute,
    getDb: () => ({
      flushFiles,
      copyFileToBuffer,
      dropFile
    })
  } as unknown as DuckDbConnector;
  return {connector, execute, flushFiles, copyFileToBuffer, dropFile};
}
