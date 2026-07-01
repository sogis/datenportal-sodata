import {describe, expect, it} from 'vitest';
import {ExploreContextLoadError, loadEmbeddedExploreContext} from './ExploreContextLoader';
import {sampleExploreContext} from '../test/sampleExploreContext';

describe('loadEmbeddedExploreContext', () => {
  it('parses a valid embedded context', () => {
    const documentRef = document.implementation.createHTMLDocument();
    documentRef.body.innerHTML = `<script id="datenportal-explore-context" type="application/json">${JSON.stringify(sampleExploreContext)}</script>`;

    expect(loadEmbeddedExploreContext(documentRef)).toMatchObject({
      version: 1,
      datasetId: 'ch.so.bauinventar',
      title: 'Bauinventar'
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
