import {useMemo, useState} from 'react';
import type {ExploreCodeSnippetDto} from '../app/ExploreContext';
import {copyTextToClipboard} from '../sql/clipboard';

export function CodeSnippetsPanel({snippets}: {snippets: ExploreCodeSnippetDto[]}) {
  const [selectedSnippetId, setSelectedSnippetId] = useState(snippets[0]?.id);
  const [copiedSnippetId, setCopiedSnippetId] = useState<string | undefined>(undefined);
  const selectedSnippet = useMemo(
    () => snippets.find((snippet) => snippet.id === selectedSnippetId) ?? snippets[0],
    [selectedSnippetId, snippets]
  );

  if (snippets.length === 0) {
    return (
      <section className="dp-explore-code" aria-label="Codebeispiele">
        <p>Für dieses Datenthema sind keine Codebeispiele verfügbar.</p>
      </section>
    );
  }

  async function copySnippet(snippet: ExploreCodeSnippetDto) {
    await copyTextToClipboard(snippet.code);
    setCopiedSnippetId(snippet.id);
    window.setTimeout(() => setCopiedSnippetId(undefined), 1800);
  }

  return (
    <section className="dp-explore-code" aria-label="Codebeispiele">
      <div className="dp-explore-code__header">
        <div>
          <h4>Weiterverwenden</h4>
          <p>Statische Beispiele für lokale Arbeit mit den publizierten Parquet-Dateien.</p>
        </div>
        {selectedSnippet && (
          <button type="button" className="dp-explore-button" onClick={() => void copySnippet(selectedSnippet)}>
            {copiedSnippetId === selectedSnippet.id ? 'Code kopiert' : 'Code kopieren'}
          </button>
        )}
      </div>

      <div className="dp-explore-code__tabs" role="tablist" aria-label="Codebeispiele">
        {snippets.map((snippet) => (
          <button
            key={snippet.id}
            type="button"
            role="tab"
            aria-selected={selectedSnippet?.id === snippet.id}
            className={selectedSnippet?.id === snippet.id ? 'dp-explore-code__tab is-active' : 'dp-explore-code__tab'}
            onClick={() => setSelectedSnippetId(snippet.id)}
          >
            {snippet.title}
          </button>
        ))}
      </div>

      {selectedSnippet && (
        <pre className="dp-explore-code__snippet" aria-label={`${selectedSnippet.title} Code`}>
          <code>{selectedSnippet.code}</code>
        </pre>
      )}
    </section>
  );
}
