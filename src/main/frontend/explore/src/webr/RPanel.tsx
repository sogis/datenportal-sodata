import {useEffect, useMemo, useRef, useState} from 'react';
import {Panel, PanelGroup, PanelResizeHandle} from 'react-resizable-panels';
import type {ExploreContextDto} from '../app/ExploreContext';
import {downloadBlob, sanitizeCsvFilename} from '../results/ResultExport';
import {limitSqlResultSnapshot, type SqlResultSnapshot} from '../results/sqlResultSnapshot';
import {copyTextToClipboard} from '../sql/clipboard';
import {buildRRecipes} from './RRecipes';
import {RCodeEditorField} from './RCodeEditorField';
import {RConsoleOutput} from './RConsoleOutput';
import {RPlotOutput} from './RPlotOutput';
import {WebRBridge, type RConsoleEntry, type RDataFrameInfo} from './WebRBridge';
import {initialWebRSteps, stepLabel, WebRRuntime, type WebRStepState} from './WebRRuntime';

type RPanelStatus = 'idle' | 'limit-warning' | 'loading' | 'ready' | 'error';

export function RPanel({
  context,
  snapshot,
  onDataFrameInfoChange,
  onBackToSql
}: {
  context: ExploreContextDto;
  snapshot?: SqlResultSnapshot;
  onDataFrameInfoChange?: (info?: RDataFrameInfo) => void;
  onBackToSql: () => void;
}) {
  const laboratory = context.rLaboratory;
  const [snapshotOverride, setSnapshotOverride] = useState<SqlResultSnapshot | undefined>(undefined);
  const activeSnapshot = snapshotOverride ?? snapshot;
  const recipes = useMemo(() => buildRRecipes(activeSnapshot, laboratory.dataFrameName), [activeSnapshot, laboratory.dataFrameName]);
  const [selectedRecipeId, setSelectedRecipeId] = useState(recipes[0]?.id ?? 'start');
  const selectedRecipe = recipes.find((recipe) => recipe.id === selectedRecipeId) ?? recipes[0];
  const [code, setCode] = useState(selectedRecipe?.code ?? '');
  const [status, setStatus] = useState<RPanelStatus>('idle');
  const [steps, setSteps] = useState<WebRStepState[]>(() => initialWebRSteps());
  const [consoleEntries, setConsoleEntries] = useState<RConsoleEntry[]>([]);
  const [running, setRunning] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dataFrameInfo, setDataFrameInfo] = useState<RDataFrameInfo | undefined>(undefined);
  const [lastPlot, setLastPlot] = useState<ImageBitmap | undefined>(undefined);
  const [plotCanvas, setPlotCanvas] = useState<HTMLCanvasElement | null>(null);
  const [hasTabularResult, setHasTabularResult] = useState(false);
  const runtimeRef = useRef<WebRRuntime | null>(null);
  const bridgeRef = useRef<WebRBridge | null>(null);

  useEffect(() => {
    setSnapshotOverride(undefined);
  }, [snapshot]);

  useEffect(() => {
    setSelectedRecipeId(recipes[0]?.id ?? 'start');
    setCode(recipes[0]?.code ?? '');
  }, [recipes]);

  useEffect(() => {
    if (!context.featureFlags.webR) {
      setStatus('error');
      setError('Das R-Labor ist in diesem Kontext nicht aktiviert.');
      return undefined;
    }
    if (!activeSnapshot) {
      setStatus('idle');
      setDataFrameInfo(undefined);
      onDataFrameInfoChange?.(undefined);
      return undefined;
    }
    if (!snapshotOverride && activeSnapshot.rowCount > laboratory.recommendedRows) {
      setStatus(activeSnapshot.rowCount > laboratory.hardRows ? 'error' : 'limit-warning');
      setError(activeSnapshot.rowCount > laboratory.hardRows
        ? `Das SQL-Resultat hat ${formatNumber(activeSnapshot.rowCount)} Zeilen und überschreitet die harte Grenze von ${formatNumber(laboratory.hardRows)} Zeilen.`
        : null);
      setDataFrameInfo(undefined);
      onDataFrameInfoChange?.(undefined);
      return undefined;
    }

    let active = true;
    void transferSnapshot(activeSnapshot, snapshotOverride ? snapshot?.rowCount : undefined).catch((transferError) => {
      if (!active) {
        return;
      }
      setStatus('error');
      setError(toErrorMessage(transferError));
    });

    return () => {
      active = false;
    };
  }, [activeSnapshot, context.featureFlags.webR, laboratory, onDataFrameInfoChange, snapshot?.rowCount, snapshotOverride]);

  function updateStep(next: WebRStepState) {
    setSteps((current) => current.map((state) => state.step === next.step ? next : state));
  }

  async function transferSnapshot(nextSnapshot: SqlResultSnapshot, limitedFrom?: number) {
    setStatus('loading');
    setError(null);
    setConsoleEntries([]);
    setLastPlot(undefined);
    setHasTabularResult(false);
    setSteps(initialWebRSteps());
    runtimeRef.current ??= new WebRRuntime(laboratory, updateStep);
    const webR = await runtimeRef.current.initialize();
    bridgeRef.current = new WebRBridge(webR);
    updateStep({step: 'data', status: 'running'});
    const info = await bridgeRef.current.loadDataFrame(nextSnapshot, laboratory.dataFrameName, limitedFrom);
    updateStep({step: 'data', status: 'done'});
    setDataFrameInfo(info);
    onDataFrameInfoChange?.(info);
    setConsoleEntries([{type: 'message', text: `${laboratory.dataFrameName} ist bereit (${formatNumber(info.rowCount)} Zeilen, ${formatNumber(info.columnCount)} Spalten).`}]);
    setStatus('ready');
  }

  async function runR() {
    if (!bridgeRef.current || status !== 'ready' || running || !code.trim()) {
      return;
    }
    setRunning(true);
    setError(null);
    try {
      const result = await bridgeRef.current.runR(code, laboratory.plotWidth, laboratory.plotHeight);
      setConsoleEntries(result.entries);
      setLastPlot(result.images.at(-1));
      setHasTabularResult(result.hasTabularResult);
    } catch (runError) {
      setError(toErrorMessage(runError));
    } finally {
      setRunning(false);
    }
  }

  async function copyR() {
    await copyTextToClipboard(code);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1800);
  }

  async function exportTable() {
    if (!bridgeRef.current || status !== 'ready') {
      return;
    }
    const csv = await bridgeRef.current.exportCurrentTableCsv(laboratory.dataFrameName);
    downloadBlob(
      new Blob([csv], {type: 'text/csv;charset=utf-8'}),
      sanitizeCsvFilename(`datenportal-${context.datasetId}-r-result.csv`)
    );
  }

  async function exportPlot() {
    if (!plotCanvas) {
      return;
    }
    const blob = await canvasToBlob(plotCanvas);
    downloadBlob(blob, `datenportal-${context.datasetId}-r-plot.png`);
  }

  function selectRecipe(recipeId: string) {
    const recipe = recipes.find((item) => item.id === recipeId);
    if (!recipe) {
      return;
    }
    setSelectedRecipeId(recipe.id);
    setCode(recipe.code);
  }

  function limitToRecommendedRows() {
    if (!snapshot) {
      return;
    }
    setSnapshotOverride(limitSqlResultSnapshot(snapshot, laboratory.recommendedRows));
  }

  const ready = status === 'ready';

  return (
    <div className="dp-explore-r-panel" aria-label="R Arbeitsbereich">
      <PanelGroup
        autoSaveId={`datenportal.explore.${context.datasetId}.r.v1`}
        className="dp-explore-lab dp-explore-r-lab dp-explore-resizable-group dp-explore-resizable-group--vertical"
        direction="vertical"
      >
        <Panel
          className="dp-explore-query-pane"
          defaultSize={43}
          id="r-query"
          maxSize={80}
          minSize={20}
          order={1}
          tagName="section"
          aria-label="R Editor"
        >
          <div className="dp-explore-query-pane__header">
            <div className="dp-explore-query-pane__toolbar-row">
              <div className="dp-explore-sql-toolbar" aria-label="R Aktionen">
                <div className="dp-explore-sql-toolbar__leading">
                  <RRecipePicker recipes={recipes} selectedRecipeId={selectedRecipeId} disabled={running} onSelect={selectRecipe} />
                </div>
                <div className="dp-explore-sql-toolbar__actions">
                  <button type="button" className="dp-explore-button dp-explore-button--primary" disabled={!ready || running || !code.trim()} onClick={() => void runR()}>
                    <svg className="bi bi-play-fill dp-explore-button__icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
                      <path d="m11.596 8.697-6.363 3.692c-.54.313-1.233-.066-1.233-.697V4.308c0-.63.692-1.01 1.233-.696l6.363 3.692a.802.802 0 0 1 0 1.393" />
                    </svg>
                    <span>R ausführen</span>
                  </button>
                  <button type="button" className="dp-explore-button dp-explore-button--secondary dp-explore-button--copy" disabled={running} onClick={() => void copyR()}>
                    {copied ? '✓ R kopiert' : 'R kopieren'}
                  </button>
                  <button type="button" className="dp-explore-button" disabled={!ready || running || (!hasTabularResult && !dataFrameInfo)} onClick={() => void exportTable()}>
                    Resultat exportieren
                  </button>
                  <button type="button" className="dp-explore-button" disabled={!plotCanvas || running} onClick={() => void exportPlot()}>
                    Plot exportieren
                  </button>
                </div>
              </div>
            </div>
            {error && (
              <p className="dp-explore-export-error" role="alert">
                {error}
              </p>
            )}
          </div>
          <RCodeEditorField value={code} onChange={setCode} onRun={() => void runR()} disabled={running || !ready} />
        </Panel>

        <ExploreResizeHandle label="R-Editor und R-Ausgabe Grösse anpassen" />

        <Panel
          className="dp-explore-r-result-pane"
          defaultSize={57}
          id="r-result"
          minSize={20}
          order={2}
          tagName="section"
          aria-label="R Resultat"
        >
          <div className="dp-explore-r-result-grid">
            <RConsoleOutput entries={consoleEntries} running={running} />
            <RPlotOutput image={lastPlot} onCanvasReady={setPlotCanvas} />
          </div>
        </Panel>
      </PanelGroup>

      {status === 'idle' && <RIdleState onBackToSql={onBackToSql} />}
      {status === 'limit-warning' && snapshot && (
        <RLimitState snapshot={snapshot} recommendedRows={laboratory.recommendedRows} onLimit={limitToRecommendedRows} onBackToSql={onBackToSql} />
      )}
      {status === 'loading' && <RLoadingOverlay steps={steps} />}
    </div>
  );
}

function RRecipePicker({
  recipes,
  selectedRecipeId,
  disabled,
  onSelect
}: {
  recipes: Array<{id: string; title: string; code: string}>;
  selectedRecipeId: string;
  disabled: boolean;
  onSelect: (recipeId: string) => void;
}) {
  return (
    <label className="dp-explore-recipe-picker">
      <span>Beispiel</span>
      <select
        aria-label="R-Rezept auswählen"
        value={selectedRecipeId}
        disabled={disabled}
        onChange={(event) => onSelect(event.target.value)}
      >
        {recipes.map((recipe) => (
          <option key={recipe.id} value={recipe.id}>{recipe.title}</option>
        ))}
      </select>
    </label>
  );
}

function RIdleState({onBackToSql}: {onBackToSql: () => void}) {
  return (
    <div className="dp-explore-r-state" role="status">
      <p className="dp-explore-runtime-overlay__title">Kein Data Frame übernommen</p>
      <p>Führe im SQL-Labor eine Abfrage aus und übernimm das Resultat nach R.</p>
      <button type="button" className="dp-explore-button dp-explore-button--secondary" onClick={onBackToSql}>
        Zurück ins SQL-Labor
      </button>
    </div>
  );
}

function RLimitState({
  snapshot,
  recommendedRows,
  onLimit,
  onBackToSql
}: {
  snapshot: SqlResultSnapshot;
  recommendedRows: number;
  onLimit: () => void;
  onBackToSql: () => void;
}) {
  return (
    <div className="dp-explore-r-state dp-explore-r-state--warning" role="alert">
      <p className="dp-explore-runtime-overlay__title">Data Frame zu gross</p>
      <p>Das SQL-Resultat enthält {formatNumber(snapshot.rowCount)} Zeilen. Für das R-Labor werden maximal {formatNumber(recommendedRows)} Zeilen empfohlen.</p>
      <div className="dp-explore-r-state__actions">
        <button type="button" className="dp-explore-button dp-explore-button--primary" onClick={onLimit}>
          Auf {formatNumber(recommendedRows)} Zeilen begrenzen
        </button>
        <button type="button" className="dp-explore-button dp-explore-button--secondary" onClick={onBackToSql}>
          Zurück ins SQL-Labor
        </button>
      </div>
    </div>
  );
}

function RLoadingOverlay({steps}: {steps: WebRStepState[]}) {
  return (
    <div className="dp-explore-runtime-overlay dp-explore-r-loading-overlay">
      <div className="dp-explore-runtime-overlay__card" role="status" aria-label="WebR Status" aria-live="polite">
        <p className="dp-explore-runtime-overlay__title">WebR wird geladen</p>
        <div className="dp-explore-runtime-progress" role="progressbar" aria-label="WebR Ladevorgang">
          <span className="dp-explore-runtime-progress__bar" />
        </div>
        <ul className="dp-explore-r-loading-steps">
          {steps.map((step) => (
            <li key={step.step} className={`is-${step.status}`}>
              <span aria-hidden="true" />
              {stepLabel(step.step)}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function ExploreResizeHandle({label}: {label: string}) {
  return (
    <PanelResizeHandle
      aria-label={label}
      className="dp-explore-resize-handle dp-explore-resize-handle--horizontal"
      hitAreaMargins={{coarse: 12, fine: 8}}
    >
      <span className="dp-explore-resize-handle__knob" aria-hidden="true" />
    </PanelResizeHandle>
  );
}

function canvasToBlob(canvas: HTMLCanvasElement): Promise<Blob> {
  return new Promise((resolve) => {
    canvas.toBlob((blob) => resolve(blob ?? new Blob([], {type: 'image/png'})), 'image/png');
  });
}

function formatNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}

function toErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
