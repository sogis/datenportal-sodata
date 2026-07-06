import type {RConsoleEntry} from './WebRBridge';

export function RConsoleOutput({entries, running}: {entries: RConsoleEntry[]; running: boolean}) {
  return (
    <section className="dp-explore-r-output dp-explore-r-console" aria-label="R Konsole" aria-live="polite">
      <div className="dp-explore-r-output__header">
        <h3>Konsole</h3>
        {running && <span>läuft</span>}
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
