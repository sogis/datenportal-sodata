import {describe, expect, it} from 'vitest';
import {ExploreContextLoadError, loadEmbeddedExploreContext} from './ExploreContextLoader';
import {sampleExploreContext} from '../test/sampleExploreContext';

describe('loadEmbeddedExploreContext', () => {
  it('parses a valid embedded context', () => {
    const documentRef = document.implementation.createHTMLDocument();
    documentRef.body.innerHTML = `<script id="datenportal-explore-context" type="application/json">${JSON.stringify(sampleExploreContext)}</script>`;

    expect(loadEmbeddedExploreContext(documentRef)).toMatchObject({
      version: 3,
      datasetId: 'ch.so.bauinventar',
      title: 'Bauinventar',
      catalogDatabase: {
        url: '/catalog/catalog.duckdb',
        database: 'catalog',
        schema: 'opendata'
      },
      featureFlags: {
        aiAssistant: false,
        webR: true,
        vega: false,
        mosaic: false,
        geospatial: false
      },
      rLaboratory: {
        dataFrameName: 'daten',
        runtimeBaseUrl: '/webr/0.6.0/',
        packageRepoUrl: '/webr-packages/',
        recommendedRows: 5000,
        warningRows: 10000,
        hardRows: 50000
      }
    });
  });

  it('fails clearly when the context element is missing', () => {
    const documentRef = document.implementation.createHTMLDocument();

    expect(() => loadEmbeddedExploreContext(documentRef)).toThrow(ExploreContextLoadError);
    expect(() => loadEmbeddedExploreContext(documentRef)).toThrow('Der Erkunden-Kontext wurde nicht gefunden.');
  });

  it('fails clearly when JSON is invalid', () => {
    const documentRef = document.implementation.createHTMLDocument();
    documentRef.body.innerHTML = '<script id="datenportal-explore-context" type="application/json">{broken</script>';

    expect(() => loadEmbeddedExploreContext(documentRef)).toThrow('Der Erkunden-Kontext ist kein gültiges JSON.');
  });

  it('fails clearly when the context shape is unexpected', () => {
    const documentRef = document.implementation.createHTMLDocument();
    documentRef.body.innerHTML = '<script id="datenportal-explore-context" type="application/json">{"version":2}</script>';

    expect(() => loadEmbeddedExploreContext(documentRef)).toThrow('Der Erkunden-Kontext hat ein unerwartetes Format.');
  });
});
