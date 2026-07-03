import type {ReactNode} from 'react';
import {useState} from 'react';
import type {ResultExportFormat} from '../results/ResultExport';

export function SqlToolbar({
  leading,
  running,
  canRun,
  canCancel,
  canExport,
  exportingFormat,
  onRun,
  onCancel,
  onCopy,
  onExport,
  copied
}: {
  leading?: ReactNode;
  running: boolean;
  canRun: boolean;
  canCancel: boolean;
  canExport: boolean;
  exportingFormat?: ResultExportFormat | null;
  onRun: () => void;
  onCancel: () => void;
  onCopy: () => void;
  onExport: (format: ResultExportFormat) => void;
  copied: boolean;
}) {
  const [exportMenuOpen, setExportMenuOpen] = useState(false);
  const exportDisabled = !canExport || Boolean(exportingFormat);

  function exportResult(format: ResultExportFormat) {
    setExportMenuOpen(false);
    onExport(format);
  }

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
      </div>

      <div className="dp-explore-sql-toolbar__export">
        <div className="dp-explore-export-split">
          <button
            type="button"
            className="dp-explore-button dp-explore-export-split__primary"
            disabled={exportDisabled}
            onClick={() => exportResult('csv')}
          >
            CSV
          </button>
          <button
            type="button"
            className="dp-explore-button dp-explore-export-split__toggle"
            disabled={exportDisabled}
            aria-label="Exportformat auswählen"
            aria-haspopup="menu"
            aria-expanded={exportMenuOpen}
            onClick={() => setExportMenuOpen((open) => !open)}
          >
            <svg className="dp-explore-button__icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
              <path d="M3.2 5.8a.7.7 0 0 1 1 0L8 9.6l3.8-3.8a.7.7 0 1 1 1 1L8.5 11a.7.7 0 0 1-1 0L3.2 6.8a.7.7 0 0 1 0-1Z" />
            </svg>
          </button>
          {exportMenuOpen && (
            <div className="dp-explore-export-menu" role="menu" aria-label="Exportformate">
              {(['csv', 'xlsx', 'parquet'] satisfies ResultExportFormat[]).map((format) => (
                <button
                  type="button"
                  role="menuitem"
                  className="dp-explore-export-menu__item"
                  key={format}
                  onClick={() => exportResult(format)}
                >
                  {formatLabel(format)}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function formatLabel(format: ResultExportFormat): string {
  return format === 'xlsx' ? 'XLSX' : format === 'parquet' ? 'Parquet' : 'CSV';
}
