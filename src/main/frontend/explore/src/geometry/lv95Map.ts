import Projection from 'ol/proj/Projection.js';
import WMTS from 'ol/source/WMTS.js';
import WMTSTileGrid from 'ol/tilegrid/WMTS.js';
import type {ExploreMapDto} from '../app/ExploreContext';
export const LV95_EXTENT = [2420000, 1030000, 2900000, 1350000];
export const LV95 = new Projection({code: 'EPSG:2056', units: 'm', extent: LV95_EXTENT});
export const WMTS_RESOLUTIONS = [4000, 2000, 1000, 500, 250, 100, 50, 20, 10, 5, 2.5, 1, 0.5, 0.25, 0.1];
// Capabilities: min row, max row, min column, max column. TileMatrixLimits use
// "2056:n", while the matrix identifiers and REST requests use plain "n".
export const WMTS_LIMITS = [[0,0,0,0],[0,0,0,0],[0,1,0,1],[0,2,0,3],[0,4,0,7],
  [0,10,0,14],[1,16,6,24],[11,32,24,53],[27,60,53,101],[59,115,112,197],
  [123,226,229,390],[315,559,580,969],[635,1114,1166,1934],[1276,2223,2338,3864],[3198,5551,5854,9653]];
export function createBackground(config: ExploreMapDto) {
  const source = new WMTS({url: config.wmtsUrl, layer: config.layer, matrixSet: '2056',
    format: 'image/png', style: 'default', requestEncoding: 'REST', projection: LV95,
    crossOrigin: 'anonymous', wrapX: false,
    tileGrid: new WMTSTileGrid({origin: [2420000,1350000], resolutions: WMTS_RESOLUTIONS,
      matrixIds: WMTS_RESOLUTIONS.map((_, i) => String(i)), tileSize: 256})});
  const url = source.getTileUrlFunction();
  source.setTileUrlFunction((coord, ratio, projection) => {
    const limits = WMTS_LIMITS[coord[0]];
    if (!limits || coord[2] < limits[0] || coord[2] > limits[1] || coord[1] < limits[2] || coord[1] > limits[3]) return undefined;
    return url(coord, ratio, projection);
  });
  return source;
}
