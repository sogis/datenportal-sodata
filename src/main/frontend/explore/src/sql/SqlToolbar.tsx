import type {ReactNode} from 'react';

export function SqlToolbar({
  leading,
  running,
  canRun,
  canCancel,
  canTransferToR,
  onRun,
  onCancel,
  onCopy,
  onTransferToR,
  copied
}: {
  leading?: ReactNode;
  running: boolean;
  canRun: boolean;
  canCancel: boolean;
  canTransferToR?: boolean;
  onRun: () => void;
  onCancel: () => void;
  onCopy: () => void;
  onTransferToR?: () => void;
  copied: boolean;
}) {
  return (
    <div className="dp-explore-sql-toolbar" aria-label="SQL Aktionen">
      {leading && <div className="dp-explore-sql-toolbar__leading">{leading}</div>}
      <div className="dp-explore-sql-toolbar__actions">
        <button type="button" className="dp-explore-button dp-explore-button--primary" disabled={!canRun || running} onClick={onRun}>
          <svg className="bi bi-play-fill dp-explore-button__icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
            <path d="m11.596 8.697-6.363 3.692c-.54.313-1.233-.066-1.233-.697V4.308c0-.63.692-1.01 1.233-.696l6.363 3.692a.802.802 0 0 1 0 1.393" />
          </svg>
          <span>Ausführen</span>
        </button>
        <button type="button" className="dp-explore-button" disabled={!canCancel} onClick={onCancel}>
          Abbrechen
        </button>
        <button type="button" className="dp-explore-button dp-explore-button--secondary dp-explore-button--copy" disabled={running} onClick={onCopy}>
          <span>{copied ? '✓ SQL kopiert' : 'SQL kopieren'}</span>
        </button>
        {onTransferToR && (
          <button
            type="button"
            className="dp-explore-button dp-explore-button--secondary"
            disabled={!canTransferToR || running}
            onClick={onTransferToR}
          >
            Nach R übernehmen
          </button>
        )}
      </div>

    </div>
  );
}
