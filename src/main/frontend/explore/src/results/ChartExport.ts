import {toPng} from 'html-to-image';
import {downloadBlob, sanitizeDownloadFilename} from './ResultExport';

export async function exportChartAsPng(
  chartElement: HTMLElement,
  datasetId: string,
  documentRef: Document = document
): Promise<void> {
  const clone = chartElement.cloneNode(true) as HTMLElement;
  clone.querySelectorAll('[data-export-ignore]').forEach((element) => element.remove());
  clone.classList.add('dp-explore-chart--export');
  const width = Math.max(1, Math.ceil(chartElement.getBoundingClientRect().width || chartElement.offsetWidth || 1));
  clone.style.width = `${width}px`;
  clone.style.height = 'auto';
  clone.style.minHeight = '0';
  clone.style.overflow = 'visible';
  clone.style.position = 'fixed';
  clone.style.left = '-100000px';
  clone.style.top = '0';
  clone.style.zIndex = '-1';
  clone.style.background = '#ffffff';
  documentRef.body.append(clone);

  try {
    const dataUrl = await toPng(clone, {
      backgroundColor: '#ffffff',
      cacheBust: true,
      pixelRatio: 2
    });
    downloadBlob(dataUrlToBlob(dataUrl), chartExportFilename(datasetId), documentRef);
  } finally {
    clone.remove();
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
