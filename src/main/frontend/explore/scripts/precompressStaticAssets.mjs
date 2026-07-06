import {createGzip, constants} from 'node:zlib';
import {createReadStream, createWriteStream} from 'node:fs';
import {mkdir, readdir, rm, stat} from 'node:fs/promises';
import {dirname, join, relative} from 'node:path';
import {pipeline} from 'node:stream/promises';
import {createBrotliCompress} from 'node:zlib';
import {fileURLToPath} from 'node:url';

const projectDir = fileURLToPath(new URL('../../../../..', import.meta.url));
const exploreStaticDir = join(projectDir, 'build/generated-resources/explore/static/explore');
const extensionStaticDir = join(projectDir, 'src/main/resources/static/explore-extensions');
const webRRuntimeStaticDir = join(projectDir, 'build/generated-resources/webr/static/webr');
const webRPackageStaticDir = join(projectDir, 'build/generated-resources/webr/static/webr-packages');
const outputRoot = join(projectDir, 'build/generated-resources/precompressed-static');
const compressibleExtensions = new Set(['.css', '.js', '.mjs', '.wasm', '.data', '.so']);
const brotliQuality = 9;

await resetOutput();
await compressTree(exploreStaticDir, 'static/explore');
await compressTree(extensionStaticDir, 'static/explore-extensions');
await compressTree(webRRuntimeStaticDir, 'static/webr');
await compressTree(webRPackageStaticDir, 'static/webr-packages');

console.log(`Precompressed static assets written to ${relative(projectDir, outputRoot)}.`);

async function resetOutput() {
  await rm(join(outputRoot, 'static/explore'), {force: true, recursive: true});
  await rm(join(outputRoot, 'static/explore-extensions'), {force: true, recursive: true});
  await rm(join(outputRoot, 'static/webr'), {force: true, recursive: true});
  await rm(join(outputRoot, 'static/webr-packages'), {force: true, recursive: true});
}

async function compressTree(sourceDir, targetPrefix) {
  const files = await listFiles(sourceDir);
  for (const file of files) {
    if (!shouldCompress(file)) {
      continue;
    }

    const target = join(outputRoot, targetPrefix, relative(sourceDir, file));
    await mkdir(dirname(target), {recursive: true});
    await Promise.all([
      compressBrotli(file, `${target}.br`),
      compressGzip(file, `${target}.gz`)
    ]);
  }
}

async function listFiles(directory) {
  const entries = await readdir(directory);
  const files = [];

  for (const entry of entries) {
    const path = join(directory, entry);
    const pathStat = await stat(path);
    if (pathStat.isDirectory()) {
      files.push(...await listFiles(path));
    } else if (pathStat.isFile()) {
      files.push(path);
    }
  }

  return files;
}

function shouldCompress(file) {
  return [...compressibleExtensions].some((extension) => file.endsWith(extension));
}

async function compressBrotli(source, target) {
  await pipeline(
    createReadStream(source),
    createBrotliCompress({
      params: {
        [constants.BROTLI_PARAM_QUALITY]: brotliQuality
      }
    }),
    createWriteStream(target)
  );
}

async function compressGzip(source, target) {
  await pipeline(
    createReadStream(source),
    createGzip({level: constants.Z_BEST_COMPRESSION}),
    createWriteStream(target)
  );
}
