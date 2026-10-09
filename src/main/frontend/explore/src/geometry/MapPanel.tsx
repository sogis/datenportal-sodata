import {useEffect, useMemo, useRef, useState} from 'react';
import Map from 'ol/Map.js';
import View from 'ol/View.js';
import Feature from 'ol/Feature.js';
import WKB from 'ol/format/WKB.js';
import TileLayer from 'ol/layer/Tile.js';
import VectorLayer from 'ol/layer/Vector.js';
import VectorSource from 'ol/source/Vector.js';
import {Circle, Fill, Stroke, Style} from 'ol/style.js';
import {isEmpty} from 'ol/extent.js';
import {unByKey} from 'ol/Observable.js';
import type {ExploreMapDto} from '../app/ExploreContext';
import type {QueryResultState} from '../results/queryResultTypes';
import {downloadBlob, sanitizeDownloadFilename} from '../results/ResultExport';
import {formatResultCell} from '../results/arrowResult';
import {geometryCandidates, geometryValue, inspectWkb, validateGeometryResult, type ResultGeometry} from './resultGeometry';
import {LV95, WMTS_RESOLUTIONS, createBackground} from './lv95Map';
import {mapColorScale} from './mapStyle';
import 'ol/ol.css';

export default function MapPanel({result, config, selected, onSelect, datasetId}: {
  result: QueryResultState; config: ExploreMapDto; selected?: ResultGeometry;
  onSelect: (geometry: ResultGeometry | undefined) => void; datasetId: string;
}) {
  const host = useRef<HTMLDivElement>(null);
  const mapRef = useRef<Map | undefined>(undefined);
  const tileFailures = useRef(0);
  const display = useRef({color: '', label: '', scale: mapColorScale([], false)});
  const [tooltipColumn, setTooltipColumn] = useState('');
  const [colorColumn, setColorColumn] = useState('');
  const [background, setBackground] = useState(true);
  const [backgroundError, setBackgroundError] = useState(false);
  const [tooltip, setTooltip] = useState('');
  const [exportError, setExportError] = useState('');
  const [exporting, setExporting] = useState(false);
  const candidates = useMemo(() => geometryCandidates(result), [result]);
  const prepared = useMemo(() => {
    if (!selected || result.status !== 'success') return undefined;
    try {
      if (result.rows.length > config.maxFeatures) throw new Error(`Die Karte unterstützt bis zu ${config.maxFeatures.toLocaleString('de-CH')} Objekte. Bitte SQL-Ergebnis einschränken.`);
      const stats = validateGeometryResult(result, [selected], config);
      const reader = new WKB();
      const features: Feature[] = [];
      for (let i = 0; i < result.rows.length; i++) {
        const bytes = geometryValue(result, selected, i);
        if (bytes == null || inspectWkb(bytes as Uint8Array).empty) continue;
        const geometry = reader.readGeometry(bytes as Uint8Array, {dataProjection: LV95, featureProjection: LV95});
        if (geometry) features.push(new Feature({geometry, resultRow: i}));
      }
      return {features, stats, error: undefined};
    } catch (error) {return {features: [], stats: undefined, error: error instanceof Error ? error.message : String(error)};}
  }, [result, selected, config]);
  const attributes = result.columns.filter((name) => !candidates.some((g) => g.column === name)
    && result.rows.every((row) => row[name] == null || ['string', 'number', 'bigint', 'boolean'].includes(typeof row[name])));
  const label = attributes.includes(tooltipColumn) ? tooltipColumn : attributes.find((name) => /name|label/i.test(name)) ?? attributes[0] ?? '';
  const color = attributes.includes(colorColumn) ? colorColumn : '';
  const scale = useMemo(() => mapColorScale(result.rows.map((row) => row[color]), Boolean(color)), [result, color]);

  display.current = {color, label, scale};
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    map.getLayers().item(0).setVisible(background);
    map.getLayers().item(1).setOpacity(color ? 0.75 : 1);
    map.getLayers().item(1).changed();
    setTooltip('');
  }, [background, color, scale, label]);

  useEffect(() => {
    if (!host.current || !prepared || prepared.error || !prepared.features.length) return;
    const source = new VectorSource<Feature>({features: prepared.features, wrapX: false});
    const tiles = createBackground(config);
    tileFailures.current = 0;
    setBackgroundError(false);
    setTooltip('');
    const failed = tiles.on('tileloaderror', () => {tileFailures.current++; setBackgroundError(true);});
    const vector = new VectorLayer({source, style: (feature) => {
      const {color, scale} = display.current;
      const paint = scale.color(result.rows[feature.get('resultRow')]?.[color]);
      const polygon = ['Polygon', 'MultiPolygon'].includes(feature.getGeometry()?.getType() ?? '');
      return new Style({stroke: new Stroke({color: polygon && color ? '#506174' : paint, width: polygon ? 0.8 : 1.5}),
        fill: new Fill({color: color ? paint : 'rgba(16,78,139,0.08)'}),
        image: new Circle({radius: 5, fill: new Fill({color: paint}), stroke: new Stroke({color: '#ffffff', width: 1})})});
    }});
    vector.setOpacity(color ? 0.75 : 1);
    const base = new TileLayer({source: tiles, visible: background});
    const map = new Map({target: host.current, layers: [base, vector],
      view: new View({projection: LV95, center: [2615000,1240000], resolution: 100,
        minResolution: WMTS_RESOLUTIONS.at(-1), maxResolution: WMTS_RESOLUTIONS[0]})});
    mapRef.current = map;
    const extent = source.getExtent();
    if (extent && !isEmpty(extent)) map.getView().fit(extent, {padding: [32,32,32,32], maxZoom: 18});
    const show = (pixel: number[]) => {
      const row = map.forEachFeatureAtPixel(pixel, (feature) => result.rows[feature.get('resultRow')], {hitTolerance: 5});
      const {label} = display.current;
      setTooltip(row && label ? `${label}: ${formatResultCell(row[label])}` : '');
    };
    const hover = map.on('pointermove', (event) => {if (!event.dragging) show(event.pixel);});
    const click = map.on('singleclick', (event) => show(event.pixel));
    const resize = new ResizeObserver(() => map.updateSize());
    resize.observe(host.current);
    return () => {resize.disconnect(); unByKey([failed, hover, click]); map.setTarget(undefined); map.dispose(); mapRef.current = undefined;};
    // Display options update the existing layers; preserve the user's viewport.
  }, [prepared, config, result]);

  function fit() {
    const map = mapRef.current;
    if (!map) return;
    const layer = map.getLayers().item(1) as VectorLayer;
    const extent = layer.getSource()?.getExtent();
    if (extent && !isEmpty(extent)) map.getView().fit(extent, {padding: [32,32,32,32], maxZoom: 18});
  }
  async function exportPng() {
    const map = mapRef.current;
    if (!map) return;
    setExporting(true); setExportError('');
    try {
      await new Promise<void>((resolve, reject) => {
        const key = map.once('rendercomplete', () => {clearTimeout(timer); resolve();});
        const timer = window.setTimeout(() => {unByKey(key); reject(new Error('Die Hintergrundkarte wurde nicht vollständig geladen.'));}, 10000);
        map.renderSync();
      });
      if (background && tileFailures.current) throw new Error('Hintergrundkacheln fehlen. Hintergrund ausschalten oder später erneut exportieren.');
      const size = map.getSize()!;
      if (size[1] + 56 + scale.legend.length * 20 > 16000) {
        throw new Error('Zu viele Kategorien für den PNG-Export. Bitte eine andere Farbspalte wählen.');
      }
      const canvas = document.createElement('canvas');
      canvas.width = size[0]; canvas.height = size[1] + 56 + (scale.legend.length ? 24 + scale.legend.length * 20 : 0);
      const ctx = canvas.getContext('2d');
      if (!ctx) throw new Error('PNG-Ausgabe ist im Browser nicht verfügbar.');
      ctx.fillStyle = '#ffffff'; ctx.fillRect(0, 0, canvas.width, canvas.height);
      map.getViewport()!.querySelectorAll<HTMLCanvasElement>('.ol-layer canvas').forEach((layer) => {
        if (!layer.width) return;
        ctx.save();
        ctx.globalAlpha = Number(layer.parentElement?.style.opacity || layer.style.opacity || 1);
        const transform = new DOMMatrix(layer.style.transform || undefined);
        ctx.setTransform(transform); ctx.drawImage(layer, 0, 0); ctx.restore();
      });
      ctx.font = '14px sans-serif'; ctx.fillStyle = '#172c42';
      ctx.fillText(`${datasetId} · LV95 / EPSG:2056`, 12, size[1] + 20);
      ctx.fillText(background ? `${config.attribution} · geo.so.ch` : 'Ohne Hintergrundkarte', 12, size[1] + 42);
      if (scale.legend.length) ctx.fillText(`Farbe: ${color}`, 12, size[1] + 64);
      scale.legend.forEach((item, i) => {
        ctx.fillStyle = item.color; ctx.fillRect(12, size[1] + 78 + i * 20, 12, 12);
        ctx.fillStyle = '#172c42'; ctx.fillText(item.label, 32, size[1] + 89 + i * 20);
      });
      const blob = await new Promise<Blob>((resolve, reject) => canvas.toBlob((value) => value ? resolve(value) : reject(new Error('PNG konnte nicht erzeugt werden.'))));
      downloadBlob(blob, sanitizeDownloadFilename(`datenportal-${datasetId}-karte.png`, 'png', 'karte.png'));
    } catch (error) {setExportError(error instanceof Error ? error.message : 'Der Browser erlaubt keinen PNG-Export.');}
    finally {setExporting(false);}
  }
  if (result.status === 'running') return <p role="status">SQL-Abfrage läuft.</p>;
  if (result.error) return <p role="alert">{result.error}</p>;
  if (result.status !== 'success') return <p role="status">Für die Karte zuerst eine SQL-Abfrage ausführen.</p>;
  return <section className="dp-explore-map" aria-label="SQL Karte">
    <div className="dp-explore-map__controls">
      <label>Geometrie <select aria-label="Geometriespalte" value={selected?.columnIndex ?? ''} onChange={(e) => onSelect(candidates.find((g) => String(g.columnIndex) === e.target.value))}>
        <option value="">WKB-Spalte wählen</option>{candidates.map((g) => <option key={g.columnIndex} value={g.columnIndex}>{g.column}</option>)}
      </select></label>
      <label>Tooltip <select aria-label="Tooltip-Spalte" value={label} onChange={(e) => setTooltipColumn(e.target.value)}>{attributes.map((a) => <option key={a}>{a}</option>)}</select></label>
      <label>Farbe <select aria-label="Farbspalte" value={color} onChange={(e) => setColorColumn(e.target.value)}><option value="">Einheitlich</option>{attributes.map((a) => <option key={a}>{a}</option>)}</select></label>
      <label><input type="checkbox" checked={background} onChange={(e) => setBackground(e.target.checked)} /> Hintergrundkarte</label>
      <button type="button" className="dp-explore-button dp-explore-button--secondary" onClick={fit} disabled={!prepared?.features.length}>Auf Ergebnis zoomen</button>
      <button type="button" className="dp-explore-button dp-explore-button--secondary" onClick={() => void exportPng()} disabled={!prepared?.features.length || exporting} aria-label="Karte als PNG herunterladen">PNG</button>
    </div>
    {!candidates.length && <p>Das Ergebnis enthält keine Geometrie- oder Binärspalte.</p>}
    {selected && !(result.geometries ?? []).some((g) => g.columnIndex === selected.columnIndex) && <p>Die gewählte Binärspalte wird ausdrücklich als WKB in LV95 interpretiert.</p>}
    {prepared?.error && <p role="alert">{prepared.error}</p>}
    {exportError && <p role="alert">{exportError}</p>}
    {background && backgroundError && <p role="status">Hintergrundkarte nicht vollständig verfügbar. Die Ergebnisgeometrien bleiben sichtbar.</p>}
    {result.maxRowsApplied && <p role="status">SQL-Ergebnis automatisch begrenzt; die Karte kann eine Teilmenge der Abfrage zeigen.</p>}
    <div className="dp-explore-map__viewport" ref={host} tabIndex={0} aria-label="Karte in LV95" />
    {tooltip && <p className="dp-explore-map__tooltip" role="status">{tooltip}</p>}
    {scale.legend.length > 0 && <div className="dp-explore-map__legend" aria-label="Kartenlegende"><strong>{color}</strong>{scale.legend.map((item) => <span key={item.label}><i style={{background: item.color}} />{item.label}</span>)}</div>}
    {prepared?.stats && <p role="status">{prepared.features.length} Geometrien · {prepared.stats.nullCount} NULL · {prepared.stats.emptyCount} leer · EPSG:2056</p>}
    {background && <a href="https://geo.so.ch" target="_blank" rel="noreferrer">{config.attribution}</a>}
  </section>;
}
