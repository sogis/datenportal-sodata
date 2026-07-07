import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';
import type {ExploreRLaboratoryDto} from '../app/ExploreContext';
import type {RDataFrameInfo} from './WebRBridge';

export function RDataFramePanel({
  snapshot,
  info,
  laboratory
}: {
  snapshot?: SqlResultSnapshot;
  info?: RDataFrameInfo;
  laboratory: ExploreRLaboratoryDto;
}) {
  const rows = info?.rowCount ?? snapshot?.rowCount;
  const columns = info?.columns ?? snapshot?.columns;
  const tooLarge = snapshot ? snapshot.rowCount > laboratory.recommendedRows : false;

  return (
    <section className="dp-r-dataframe-panel" aria-label="Datenbasis R-Labor">
      <header className="dp-r-dataframe-panel__header">
        <p className="dp-explore-kicker">Datengrundlage</p>
        <h2>Data Frame</h2>
      </header>

      {!snapshot && (
        <p className="dp-explore-muted">Noch kein SQL-Resultat ins R-Labor übernommen.</p>
      )}

      {snapshot && (
        <dl className="dp-r-dataframe-panel__facts">
          <div>
            <dt>Quelle</dt>
            <dd>aktuelles SQL-Resultat</dd>
          </div>
          <div>
            <dt>Data Frame</dt>
            <dd>{info?.name ?? laboratory.dataFrameName}</dd>
          </div>
          <div>
            <dt>Anzahl Zeilen</dt>
            <dd>{formatNumber(rows ?? 0)}</dd>
          </div>
          <div>
            <dt>Anzahl Spalten</dt>
            <dd>{formatNumber(columns?.length ?? 0)}</dd>
          </div>
        </dl>
      )}

      {tooLarge && (
        <div className="dp-r-dataframe-panel__warning" role="note">
          <strong>Data Frame gross</strong>
          <span>Empfohlen: max. {formatNumber(laboratory.recommendedRows)} Zeilen</span>
        </div>
      )}

      {columns && columns.length > 0 && (
        <div className="dp-r-dataframe-panel__columns">
          <h3>Spalten</h3>
          <ul>
            {columns.map((column) => (
              <li key={column.name}>
                <small className={`dp-r-dataframe-panel__type dp-r-dataframe-panel__type--${rTypeClass(column.rType)}`}>
                  {column.rType}
                </small>
                <span title={column.name}>{column.name}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {info?.warnings && info.warnings.length > 0 && (
        <div className="dp-r-dataframe-panel__warnings">
          <h3>Typ-Hinweise</h3>
          <ul>
            {info.warnings.slice(0, 4).map((warning) => (
              <li key={warning}>{warning}</li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}

function formatNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}

function rTypeClass(type: string): string {
  switch (type) {
    case 'integer':
    case 'numeric':
      return 'numeric';
    case 'character':
      return 'text';
    case 'logical':
      return 'boolean';
    default:
      return 'other';
  }
}
