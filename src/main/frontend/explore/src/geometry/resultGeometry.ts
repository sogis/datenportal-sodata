import type {Table} from 'apache-arrow';
import type {QueryResultState} from '../results/queryResultTypes';

export interface ResultGeometry {
  columnIndex: number;
  column: string;
  encoding: 'wkb';
  crs: string;
}
export interface GeometryLimits {maxFeatures: number; maxBytes: number; maxCoordinates: number}
export const DEFAULT_GEOMETRY_LIMITS: GeometryLimits = {
  maxFeatures: 10_000, maxBytes: 32 * 1024 * 1024, maxCoordinates: 1_000_000
};
export const DEFAULT_GEOMETRY_CRS = 'EPSG:2056';

export function geometryColumns(table?: Table): ResultGeometry[] {
  return (table?.schema.fields ?? []).flatMap((field, columnIndex) => {
    if (field.metadata.get('ARROW:extension:name') !== 'geoarrow.wkb') return [];
    const metadata = JSON.parse(field.metadata.get('ARROW:extension:metadata') || '{}');
    const id = metadata.crs?.id;
    const crs = typeof metadata.crs === 'string' ? metadata.crs
      : id?.authority && id?.code ? `${id.authority}:${id.code}`
      : metadata.crs ? 'Unbekanntes CRS' : DEFAULT_GEOMETRY_CRS;
    return [{columnIndex, column: field.name, encoding: 'wkb' as const, crs}];
  });
}

export function geometryCandidates(result: QueryResultState): ResultGeometry[] {
  const known = result.geometries ?? geometryColumns(result.arrowTable);
  return result.columns.flatMap((column, columnIndex) => {
    const match = known.find((g) => g.columnIndex === columnIndex);
    if (match) return [match];
    const field = result.arrowTable?.schema.fields[columnIndex];
    const binary = field?.type.toString() === 'Binary'
      || result.rows.some((row) => row[column] instanceof Uint8Array);
    return binary ? [{columnIndex, column, encoding: 'wkb' as const, crs: DEFAULT_GEOMETRY_CRS}] : [];
  });
}

export function geometryValue(result: QueryResultState, geometry: ResultGeometry, rowIndex: number): unknown {
  return result.arrowTable ? result.arrowTable.getChildAt(geometry.columnIndex)?.get(rowIndex)
    : result.rows[rowIndex][geometry.column];
}

export function assertGeometryColumns(result: QueryResultState, geometries: ResultGeometry[]) {
  if (geometries.length && new Set(result.columns).size !== result.columns.length) {
    throw new Error('Geometrien benötigen eindeutige Spaltennamen. Bitte SQL-Aliasnamen vergeben.');
  }
  if (geometries.some((g) => g.crs !== DEFAULT_GEOMETRY_CRS)) {
    throw new Error('Die Karte und R-Übernahme unterstützen ausschliesslich LV95 / EPSG:2056.');
  }
}

/** Validate before handing untrusted bytes to a renderer. Counts are bounded before iteration. */
export function inspectWkb(bytes: Uint8Array, maxCoordinates = DEFAULT_GEOMETRY_LIMITS.maxCoordinates) {
  const view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
  let offset = 0;
  let coordinates = 0;
  let drawable = false;
  function need(n: number) {if (offset + n > view.byteLength) throw new Error('Unvollständiges WKB');}
  function geometry(depth: number): number {
    if (depth > 32) throw new Error('Zu tief verschachteltes WKB');
    need(5);
    const order = view.getUint8(offset++);
    if (order > 1) throw new Error('Ungültige WKB-Byte-Reihenfolge');
    const little = order === 1;
    const uint = () => {need(4); const n = view.getUint32(offset, little); offset += 4; return n;};
    const raw = uint();
    const iso = raw & 0x1fffffff;
    if (Math.floor(iso / 1000) > 3) throw new Error('Nicht unterstützter WKB-Geometrietyp');
    const kind = iso % 1000;
    const dimensions = 2 + Number(Boolean(raw & 0x80000000) || Math.floor(iso / 1000) % 2 === 1)
      + Number(Boolean(raw & 0x40000000) || Math.floor(iso / 1000) >= 2);
    if (raw & 0x20000000) {
      if (uint() !== 2056) throw new Error('WKB enthält ein anderes CRS als EPSG:2056');
    }
    const points = (n: number, allowEmptyPoint = false) => {
      if (coordinates + n > maxCoordinates) throw new Error('Zu viele Koordinaten. Bitte SQL-Ergebnis einschränken.');
      need(n * dimensions * 8);
      coordinates += n;
      for (let i = 0; i < n; i++) {
        const x = view.getFloat64(offset, little), y = view.getFloat64(offset + 8, little);
        if (!(allowEmptyPoint && Number.isNaN(x) && Number.isNaN(y))) {
          if (!Number.isFinite(x) || !Number.isFinite(y)) throw new Error('Ungültige WKB-Koordinaten');
          drawable = true;
        }
        offset += dimensions * 8;
      }
    };
    if (kind === 1) points(1, true);
    else if (kind === 2) points(uint());
    else if (kind === 3) {
      const rings = uint(); need(rings * 4);
      for (let i = 0; i < rings; i++) points(uint());
    } else if (kind >= 4 && kind <= 7) {
      const count = uint(); need(count * 5);
      for (let i = 0; i < count; i++) {
        const child = geometry(depth + 1);
        if (kind !== 7 && child !== kind - 3) throw new Error('Ungültiger WKB-Multi-Geometrietyp');
      }
    } else throw new Error('Nicht unterstützter WKB-Geometrietyp');
    return kind;
  }
  const kind = geometry(0);
  if (offset !== view.byteLength) throw new Error('Überzählige WKB-Bytes');
  return {coordinates, empty: !drawable, kind};
}

export function validateGeometryResult(result: QueryResultState, geometries: ResultGeometry[], limits = DEFAULT_GEOMETRY_LIMITS) {
  assertGeometryColumns(result, geometries);
  let byteCount = 0, coordinateCount = 0, nullCount = 0, emptyCount = 0;
  for (const geometry of geometries) {
    for (let row = 0; row < result.rows.length; row++) {
      const value = geometryValue(result, geometry, row);
      if (value == null) {nullCount++; continue;}
      try {
        if (!(value instanceof Uint8Array)) throw new Error('Kein binäres WKB');
        byteCount += value.byteLength;
        if (byteCount > limits.maxBytes) throw new Error('WKB-Bytebudget überschritten. Bitte SQL-Ergebnis einschränken.');
        const inspected = inspectWkb(value, limits.maxCoordinates - coordinateCount);
        coordinateCount += inspected.coordinates;
        if (inspected.empty) emptyCount++;
      } catch (error) {
        throw new Error(`Spalte «${geometry.column}», Zeile ${row + 1}: ${error instanceof Error ? error.message : String(error)}`);
      }
    }
  }
  return {byteCount, coordinateCount, nullCount, emptyCount};
}

export function wkbBase64(value: unknown): string | null {
  if (value == null) return null;
  if (!(value instanceof Uint8Array)) throw new Error('Kein binäres WKB');
  let binary = '';
  for (let i = 0; i < value.length; i += 8192) binary += String.fromCharCode(...value.subarray(i, i + 8192));
  return btoa(binary);
}
