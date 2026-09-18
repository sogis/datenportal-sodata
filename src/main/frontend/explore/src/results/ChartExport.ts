import {toPng} from 'html-to-image';
import {downloadBlob, sanitizeDownloadFilename} from './ResultExport';

const CANVAS_EXPORT_ERROR =
  'Der Browser schützt Canvas-Daten vor dem Auslesen. Der PNG-Export ist deshalb nicht verfügbar. '
  + 'Bitte deaktivieren Sie den Fingerprinting-Schutz für diese Seite oder verwenden Sie einen anderen Browser.';

export class ChartExportError extends Error {
  constructor(message = CANVAS_EXPORT_ERROR) {
    super(message);
    this.name = 'ChartExportError';
  }
}

export async function exportChartAsPng(
  chartElement: HTMLElement,
  datasetId: string,
  documentRef: Document = document
): Promise<void> {
  assertCanvasReadbackAvailable(documentRef);

  const clone = chartElement.cloneNode(true) as HTMLElement;
  clone.querySelectorAll('[data-export-ignore]').forEach((element) => element.remove());
  clone.classList.add('dp-explore-chart--export');
  const bounds = chartElement.getBoundingClientRect();
  const width = Math.max(1, Math.ceil(bounds.width || chartElement.offsetWidth || 1));
  let height = Math.max(1, Math.ceil(chartElement.scrollHeight || bounds.height || chartElement.offsetHeight || 1));
  clone.style.width = `${width}px`;
  clone.style.height = 'auto';
  clone.style.minHeight = '0';
  clone.style.overflow = 'visible';
  clone.style.background = '#ffffff';
  const exportHost = documentRef.createElement('div');
  exportHost.style.position = 'absolute';
  exportHost.style.left = '-100000px';
  exportHost.style.top = '0';
  exportHost.style.width = `${width}px`;
  exportHost.style.height = 'auto';
  exportHost.style.overflow = 'hidden';
  exportHost.append(clone);
  documentRef.body.append(exportHost);

  try {
    // Measure the export layout after removing controls, including all wrapped legend entries.
    height = Math.max(1, Math.ceil(clone.scrollHeight || height));
    clone.style.height = `${height}px`;
    exportHost.style.height = `${height}px`;
    const dataUrl = await toPng(clone, {
      backgroundColor: '#ffffff',
      cacheBust: true,
      width,
      height,
      pixelRatio: 2,
      // The page imports vendor font CSS with relative URLs. html-to-image's
      // font inliner cannot resolve those URLs consistently across browsers.
      skipFonts: true
    });
    downloadBlob(dataUrlToBlob(dataUrl), chartExportFilename(datasetId), documentRef);
  } finally {
    exportHost.remove();
  }
}

function assertCanvasReadbackAvailable(documentRef: Document): void {
  const canvas = documentRef.createElement('canvas');
  canvas.width = 4;
  canvas.height = 4;

  const context = canvas.getContext('2d');
  if (!context) {
    throw new ChartExportError();
  }

  const expected = [18, 52, 86, 255];
  context.fillStyle = '#123456';
  context.fillRect(0, 0, canvas.width, canvas.height);

  let pixels: Uint8ClampedArray;
  try {
    pixels = context.getImageData(0, 0, canvas.width, canvas.height).data;
  } catch (_error) {
    throw new ChartExportError();
  }

  for (let index = 0; index < pixels.length; index += 4) {
    if (expected.some((value, channel) => pixels[index + channel] !== value)) {
      throw new ChartExportError();
    }
  }
}

export function chartExportFilename(datasetId: string): string {
  return sanitizeDownloadFilename(`datenportal-${datasetId}-diagramm.png`, 'png', 'datenportal-diagramm.png');
}

function dataUrlToBlob(dataUrl: string): Blob {
  const [header, data] = dataUrl.split(',', 2);
  if (!header || data === undefined) {
    throw new Error('Der PNG-Export hat kein gültiges Bild geliefert.');
  }
  const mimeType = header.match(/^data:([^;]+)/i)?.[1] ?? 'image/png';
  if (/;base64/i.test(header)) {
    const binary = atob(data);
    const bytes = new Uint8Array(binary.length);
    for (let index = 0; index < binary.length; index += 1) {
      bytes[index] = binary.charCodeAt(index);
    }
    return new Blob([bytes], {type: mimeType});
  }
  return new Blob([decodeURIComponent(data)], {type: mimeType});
}
