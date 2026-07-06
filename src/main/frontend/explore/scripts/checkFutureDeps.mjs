import {readdir, readFile, stat} from 'node:fs/promises';
import {existsSync} from 'node:fs';
import {join, relative} from 'node:path';
import {fileURLToPath} from 'node:url';

const rootDir = fileURLToPath(new URL('..', import.meta.url));
const packageJsonPath = join(rootDir, 'package.json');
const sourceDir = join(rootDir, 'src');
const builtAssetsDir = join(rootDir, '../../../../build/generated-resources/explore/static/explore/assets');

const forbiddenDirectDependencies = [
  '@sqlrooms/ai',
  '@sqlrooms/vega',
  '@sqlrooms/mosaic',
  '@deck.gl/core',
  '@deck.gl/layers',
  'deck.gl',
  'kepler.gl',
  'maplibre-gl',
  'leaflet'
];

const forbiddenImportMarkers = [
  '@sqlrooms/ai',
  '@sqlrooms/vega',
  '@sqlrooms/mosaic',
  '@deck.gl/',
  'deck.gl',
  'kepler.gl',
  'maplibre-gl',
  'leaflet'
];

const forbiddenBuiltAssetMarkers = [
  '@sqlrooms/ai',
  '@sqlrooms/vega',
  '@sqlrooms/mosaic',
  'deck.gl',
  'kepler.gl',
  'maplibre-gl',
  'leaflet'
];

const failures = [];

await checkDirectDependencies();
await checkSourceImports();
await checkBuiltAssets();

if (failures.length > 0) {
  console.error('Future dependency check failed:');
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

console.log('Future dependency check passed: no disabled AI/Vega/Mosaic/geospatial packages are directly loaded.');

async function checkDirectDependencies() {
  const packageJson = JSON.parse(await readFile(packageJsonPath, 'utf8'));
  const direct = {
    ...packageJson.dependencies,
    ...packageJson.devDependencies,
    ...packageJson.optionalDependencies
  };

  for (const dependency of forbiddenDirectDependencies) {
    if (Object.hasOwn(direct, dependency)) {
      failures.push(`Forbidden direct dependency ${dependency} in package.json.`);
    }
  }
}

async function checkSourceImports() {
  const files = await listFiles(sourceDir, (path) => /\.(ts|tsx|js|jsx)$/.test(path));
  const importPattern = /\b(?:import|export)\b[\s\S]*?\bfrom\s+['"]([^'"]+)['"]|import\(\s*['"]([^'"]+)['"]\s*\)/g;

  for (const file of files) {
    const source = await readFile(file, 'utf8');
    for (const match of source.matchAll(importPattern)) {
      const specifier = match[1] ?? match[2] ?? '';
      for (const marker of forbiddenImportMarkers) {
        if (specifier === marker || specifier.startsWith(`${marker}/`)) {
          failures.push(`Forbidden future import ${specifier} in ${relative(rootDir, file)}.`);
        }
      }
    }
  }
}

async function checkBuiltAssets() {
  if (!existsSync(builtAssetsDir)) {
    return;
  }

  const files = await listFiles(builtAssetsDir, (path) => /\.(js|css|map)$/.test(path));
  for (const file of files) {
    const source = await readFile(file, 'utf8');
    for (const marker of forbiddenBuiltAssetMarkers) {
      if (source.includes(marker)) {
        failures.push(`Forbidden future package marker ${marker} found in built asset ${relative(rootDir, file)}.`);
      }
    }
  }
}

async function listFiles(directory, include) {
  const entries = await readdir(directory);
  const files = [];

  for (const entry of entries) {
    const path = join(directory, entry);
    const pathStat = await stat(path);
    if (pathStat.isDirectory()) {
      files.push(...await listFiles(path, include));
    } else if (include(path)) {
      files.push(path);
    }
  }

  return files;
}
