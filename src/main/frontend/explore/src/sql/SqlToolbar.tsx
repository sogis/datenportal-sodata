export function SqlToolbar({
  running,
  canRun,
  canCancel,
  onRun,
  onCancel,
  onCopy,
  copied
}: {
  running: boolean;
  canRun: boolean;
  canCancel: boolean;
  onRun: () => void;
  onCancel: () => void;
  onCopy: () => void;
  copied: boolean;
}) {
  return (
    <div className="dp-explore-sql-toolbar" aria-label="SQL Aktionen">
      <button type="button" className="dp-explore-button dp-explore-button--primary" disabled={!canRun || running} onClick={onRun}>
        Ausführen
      </button>
      <button type="button" className="dp-explore-button" disabled={!canCancel} onClick={onCancel}>
        Abbrechen
      </button>
      <button type="button" className="dp-explore-button" disabled={running} onClick={onCopy}>
        {copied ? 'SQL kopiert' : 'SQL kopieren'}
      </button>
      <span className="dp-explore-muted">Ctrl/Cmd + Enter führt die Abfrage aus.</span>
    </div>
  );
}

