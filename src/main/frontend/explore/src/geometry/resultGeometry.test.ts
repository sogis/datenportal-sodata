import {describe, expect, it} from 'vitest';
import WKB from 'ol/format/WKB.js';
import Point from 'ol/geom/Point.js';
import LineString from 'ol/geom/LineString.js';
import MultiLineString from 'ol/geom/MultiLineString.js';
import MultiPoint from 'ol/geom/MultiPoint.js';
import Polygon from 'ol/geom/Polygon.js';
import MultiPolygon from 'ol/geom/MultiPolygon.js';
import {DEFAULT_GEOMETRY_LIMITS, geometryCandidates, inspectWkb, validateGeometryResult, wkbBase64, type ResultGeometry} from './resultGeometry';
import {sqlResultSnapshotFromQueryResult} from '../results/sqlResultSnapshot';
import type {QueryResultState} from '../results/queryResultTypes';
import {mapColorScale} from './mapStyle';
import {createBackground, LV95} from './lv95Map';
import {sampleExploreContext} from '../test/sampleExploreContext';
const writer = new WKB({hex: false});
const point = new Uint8Array(writer.writeGeometry(new Point([2600000,1200000])) as ArrayBuffer);
const geom: ResultGeometry = {columnIndex: 0, column: 'renamed', encoding: 'wkb', crs: 'EPSG:2056'};
const result = (values: unknown[]): QueryResultState => ({status: 'success', sourceSql: 'select geometry', columns: ['renamed'], rows: values.map(renamed => ({renamed})), rowCount: values.length, geometries: [geom]});
describe('WKB result contract', () => {
  it.each([
    [new Point([2600000,1200000]), 1, 1],
    [new MultiPoint([[2600000,1200000],[2600010,1200010]]), 4, 2],
    [new LineString([[2600000,1200000],[2600010,1200010]]), 2, 2],
    [new MultiLineString([[[2600000,1200000],[2600010,1200010]],[[2600010,1200020],[2600030,1200020]]]), 5, 4]
  ] as const)('reads simple and multi point/line WKB (%s)', (geometry, kind, coordinates) => {
    const bytes = new Uint8Array(writer.writeGeometry(geometry) as ArrayBuffer);
    expect(inspectWkb(bytes)).toEqual({kind, coordinates, empty: false});
    expect((writer.readGeometry(bytes) as typeof geometry).getCoordinates()).toEqual(geometry.getCoordinates());
  });

  it('retains exact bytes, NULL, CRS and a computed alias without catalog metadata', () => {
    const snapshot = sqlResultSnapshotFromQueryResult(result([point, null]), []);
    expect(snapshot.activeGeometryColumn).toBe('renamed');
    expect(snapshot.columns[0]).toMatchObject({rType: 'sfc', geometry: {encoding: 'wkb', crs: 'EPSG:2056', transportEncoding: 'base64'}});
    expect(snapshot.rows).toEqual([[wkbBase64(point)], [null]]);
    expect(Uint8Array.from(atob(snapshot.rows[0][0] as string), c => c.charCodeAt(0))).toEqual(point);
  });
  it('supports multipart polygons with holes and counts EMPTY separately from NULL', () => {
    const rings = [[[2600000,1200000],[2600010,1200000],[2600010,1200010],[2600000,1200000]],[[2600002,1200002],[2600003,1200002],[2600003,1200003],[2600002,1200002]]];
    const multi = new Uint8Array(writer.writeGeometry(new MultiPolygon([rings, [rings[0]]])) as ArrayBuffer);
    expect(inspectWkb(multi)).toEqual({coordinates: 12, empty: false, kind: 6});
    const empty = new Uint8Array(writer.writeGeometry(new Polygon([])) as ArrayBuffer);
    expect(validateGeometryResult(result([null, empty, multi]), [geom])).toMatchObject({nullCount: 1, emptyCount: 1, coordinateCount: 12});
  });
  it('rejects truncated bytes, foreign CRS, duplicate names and resource excess with row context', () => {
    expect(() => inspectWkb(point.slice(0,10))).toThrow('Unvollständiges WKB');
    expect(() => validateGeometryResult(result([point]), [{...geom, crs:'EPSG:4326'}])).toThrow('LV95');
    expect(() => validateGeometryResult({...result([point]), columns:['renamed','renamed']}, [geom])).toThrow('Aliasnamen');
    expect(() => validateGeometryResult(result([point,point]), [geom], {...DEFAULT_GEOMETRY_LIMITS,maxCoordinates:1})).toThrow('Zeile 2');
    expect(() => validateGeometryResult(result([point]), [geom], {...DEFAULT_GEOMETRY_LIMITS,maxBytes:1})).toThrow('Bytebudget');
    expect(() => inspectWkb(new Uint8Array([1,2,0,0,0,255,255,255,255]))).toThrow('Koordinaten');
  });
  it('offers raw binary only as an explicit WKB candidate', () => {
    const binary = {...result([point]), geometries: []};
    expect(geometryCandidates(binary)).toEqual([geom]);
    expect(sqlResultSnapshotFromQueryResult(binary, []).columns[0].geometry).toBeUndefined();
    expect(sqlResultSnapshotFromQueryResult(binary, [], geom).columns[0].rType).toBe('sfc');
  });
});
describe('LV95 map', () => {
  it('matches the supplied WMTS row/column example and excludes unavailable tiles', () => {
    const background = createBackground(sampleExploreContext.map);
    expect(background.getTileUrlFunction()([8,66,35], 1, LV95)).toBe('https://geo.so.ch/api/wmts/1.0.0/ch.so.agi.hintergrundkarte_sw/default/2056/8/35/66.png');
    expect(background.getTileUrlFunction()([8,1,1], 1, LV95)).toBeUndefined();
    expect(background.getTileGrid()!.getOrigin(8)).toEqual([2420000,1350000]);
    expect(background.getTileGrid()!.getResolution(8)).toBe(10);
  });
  it('uses continuous numerical colors and stable categorical colors including missing values', () => {
    const numerical = mapColorScale([1,5,9,null], true);
    expect(numerical.color(1)).not.toBe(numerical.color(9));
    expect(numerical.legend.map(x => x.label)).toEqual(['1','5','9','Keine Angabe']);
    expect(mapColorScale(['B','A'],true).color('A')).toBe(mapColorScale(['A','B'],true).color('A'));
    expect(mapColorScale([],false).legend).toEqual([]);
  });
});
