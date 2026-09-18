import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {chartExportFilename, exportChartAsPng} from './ChartExport';

const toPng = vi.hoisted(() => vi.fn());

vi.mock('html-to-image', () => ({toPng}));

describe('ChartExport', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('URL', {
      createObjectURL: vi.fn().mockReturnValue('blob:chart'),
      revokeObjectURL: vi.fn()
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('creates a PNG with the chart title and legend while removing UI controls', async () => {
    const {documentRef, clickedDownloads} = downloadHarness();
    const chart = chartElement(documentRef);
    toPng.mockImplementationOnce(async (clone: HTMLElement, options: object) => {
      expect(clone).not.toBe(chart);
      expect(clone.querySelector('h4')?.textContent).toBe('Diagramm aus Resultat');
      expect(clone.querySelector('.dp-explore-chart__legend')?.textContent).toContain('Solothurn');
      expect(clone.querySelector('[data-export-ignore]')).toBeNull();
      expect(options).toMatchObject({backgroundColor: '#ffffff', cacheBust: true, pixelRatio: 2, skipFonts: true});
      return 'data:image/png;base64,UE5H';
    });

    await exportChartAsPng(chart, 'ch.so/bau inventar', documentRef);

    expect(clickedDownloads).toEqual(['datenportal-ch.so-bau-inventar-diagramm.png']);
    expect(documentRef.body.querySelector('.dp-explore-chart--export')).toBeNull();
  });

  it('includes every series legend entry and excludes an open selector', async () => {
    const {documentRef} = downloadHarness();
    const chart = chartElement(documentRef);
    const legend = chart.querySelector('.dp-explore-chart__legend')!;
    legend.classList.add('dp-explore-chart__legend--series');
    legend.innerHTML = '<li>Messung.A</li><li>Messung B</li><li>Messung C</li>';
    const popup = documentRef.createElement('div');
    popup.setAttribute('role', 'dialog');
    popup.textContent = 'Y-Attribute auswählen';
    documentRef.body.append(popup);
    toPng.mockImplementationOnce(async (clone: HTMLElement) => {
      expect(clone.querySelectorAll('.dp-explore-chart__legend--series li')).toHaveLength(3);
      expect(clone.textContent).toContain('Messung C');
      expect(clone.querySelector('[role="dialog"]')).toBeNull();
      return 'data:image/png;base64,UE5H';
    });
    await exportChartAsPng(chart, 'fixture', documentRef);
  });

  it('removes the temporary clone when PNG creation fails', async () => {
    const {documentRef} = downloadHarness();
    const chart = chartElement(documentRef);
    toPng.mockRejectedValueOnce(new Error('PNG failed'));

    await expect(exportChartAsPng(chart, 'fixture', documentRef)).rejects.toThrow('PNG failed');

    expect(documentRef.body.querySelector('.dp-explore-chart--export')).toBeNull();
  });

  it('reports protected canvas readback before starting a download', async () => {
    const {documentRef, clickedDownloads} = downloadHarness(new Uint8ClampedArray(4 * 4 * 4));
    const chart = chartElement(documentRef);

    await expect(exportChartAsPng(chart, 'fixture', documentRef)).rejects.toThrow('Canvas-Daten');

    expect(toPng).not.toHaveBeenCalled();
    expect(clickedDownloads).toEqual([]);
    expect(documentRef.body.querySelector('.dp-explore-chart--export')).toBeNull();
  });

  it('sanitizes the chart filename', () => {
    expect(chartExportFilename('ch.so/bau inventar')).toBe('datenportal-ch.so-bau-inventar-diagramm.png');
  });
});

function chartElement(documentRef: Document): HTMLElement {
  const chart = documentRef.createElement('div');
  chart.className = 'dp-explore-chart';
  chart.innerHTML = `
    <div class="dp-explore-chart__header">
      <h4>Diagramm aus Resultat</h4>
      <p data-export-ignore="true">Nur UI-Hinweis</p>
    </div>
    <div class="dp-explore-chart__controls" data-export-ignore="true">Steuerung</div>
    <div class="dp-explore-chart__figure"><svg aria-label="Diagramm"></svg></div>
    <ul class="dp-explore-chart__legend"><li>Solothurn</li></ul>`;
  documentRef.body.append(chart);
  return chart;
}

function downloadHarness(canvasPixels = expectedCanvasPixels()) {
  const documentRef = document.implementation.createHTMLDocument();
  const clickedDownloads: string[] = [];
  const originalCreateElement = documentRef.createElement.bind(documentRef);
  vi.spyOn(documentRef, 'createElement').mockImplementation((tagName: string) => {
    const element = originalCreateElement(tagName);
    if (tagName === 'a') {
      Object.defineProperty(element, 'click', {
        value: () => clickedDownloads.push((element as HTMLAnchorElement).download)
      });
    }
    if (tagName === 'canvas') {
      const context = {
        fillStyle: '',
        fillRect: vi.fn(),
        getImageData: vi.fn(() => ({data: canvasPixels}))
      } as unknown as CanvasRenderingContext2D;
      Object.defineProperty(element, 'getContext', {value: () => context});
    }
    return element;
  });
  return {documentRef, clickedDownloads};
}

function expectedCanvasPixels(): Uint8ClampedArray {
  const pixels = new Uint8ClampedArray(4 * 4 * 4);
  for (let index = 0; index < pixels.length; index += 4) {
    pixels[index] = 18;
    pixels[index + 1] = 52;
    pixels[index + 2] = 86;
    pixels[index + 3] = 255;
  }
  return pixels;
}
