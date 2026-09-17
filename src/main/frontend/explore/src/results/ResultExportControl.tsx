import {useState} from 'react';
import type {ResultExportFormat} from './ResultExport';

export function ResultExportControl({
  canExport,
  exportingFormat,
  onExport
}: {
  canExport: boolean;
  exportingFormat?: ResultExportFormat | null;
  onExport: (format: ResultExportFormat) => void;
}) {
  const [exportMenuOpen, setExportMenuOpen] = useState(false);
  const exportDisabled = !canExport || Boolean(exportingFormat);

  function exportResult(format: ResultExportFormat) {
    setExportMenuOpen(false);
    onExport(format);
  }

  return (
    <div className="dp-explore-result-export">
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
  );
}

function formatLabel(format: ResultExportFormat): string {
  return format === 'xlsx' ? 'XLSX' : format === 'parquet' ? 'Parquet' : 'CSV';
}
