import type {RConsoleEntry} from './WebRBridge';

export function RConsoleOutput({
  entries,
  running,
  canExport,
  onExport
}: {
  entries: RConsoleEntry[];
  running: boolean;
  canExport: boolean;
  onExport: () => void;
}) {
  return (
    <section className="dp-explore-r-output dp-explore-r-console" aria-label="R Konsole" aria-live="polite">
      <div className="dp-explore-r-output__header">
        <div className="dp-explore-r-output__actions">
          {running && <span className="dp-explore-r-output__status">läuft</span>}
          <button type="button" className="dp-explore-button" disabled={!canExport} onClick={onExport}>
            Resultat exportieren
          </button>
        </div>
      </div>
      <pre className="dp-explore-r-console__body">
        {entries.length === 0 ? '> ' : entries.map((entry, index) => (
          <ConsoleLine entry={entry} key={`${entry.type}-${index}`} />
        ))}
      </pre>
    </section>
  );
}

function ConsoleLine({entry}: {entry: RConsoleEntry}) {
  return (
    <span className={`dp-explore-r-console__line dp-explore-r-console__line--${entry.type}`}>
      {entry.text}
      {'\n'}
    </span>
  );
}
