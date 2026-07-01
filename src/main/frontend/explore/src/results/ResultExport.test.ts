import {afterEach, describe, expect, it, vi} from 'vitest';
import {exportRowsToCsv, resultCsvFilename, rowsToCsv, sanitizeCsvFilename} from './ResultExport';

describe('ResultExport', () => {
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
    expect(sanitizeCsvFilename('messung')).toBe('messung.csv');
  });

  it('creates a download link and revokes the object URL', () => {
    const createObjectURL = vi.fn().mockReturnValue('blob:result');
    const revokeObjectURL = vi.fn();
    vi.stubGlobal('URL', {createObjectURL, revokeObjectURL});
    const click = vi.fn();
    const documentRef = document.implementation.createHTMLDocument();
    const originalCreateElement = documentRef.createElement.bind(documentRef);
    vi.spyOn(documentRef, 'createElement').mockImplementation((tagName: string) => {
      const element = originalCreateElement(tagName);
      if (tagName === 'a') {
        Object.defineProperty(element, 'click', {value: click});
      }
      return element;
    });

    exportRowsToCsv([{name: 'Solothurn'}], ['name'], 'result.csv', documentRef);

    expect(createObjectURL).toHaveBeenCalledWith(expect.any(Blob));
    expect(click).toHaveBeenCalled();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:result');
  });
});

