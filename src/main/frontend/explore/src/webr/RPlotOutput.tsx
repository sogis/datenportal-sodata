import {useEffect, useRef} from 'react';

export function RPlotOutput({
  image,
  canExport,
  onExport,
  onCanvasReady
}: {
  image?: ImageBitmap;
  canExport: boolean;
  onExport: () => void;
  onCanvasReady?: (canvas: HTMLCanvasElement | null) => void;
}) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || !image) {
      onCanvasReady?.(null);
      return;
    }
    canvas.width = image.width;
    canvas.height = image.height;
    const context = canvas.getContext('2d');
    context?.clearRect(0, 0, canvas.width, canvas.height);
    context?.drawImage(image, 0, 0);
    onCanvasReady?.(canvas);
  }, [image, onCanvasReady]);

  return (
    <section className="dp-explore-r-output dp-explore-r-plot" aria-label="R Plot">
      <div className="dp-explore-r-output__header">
        <div className="dp-explore-r-output__actions">
          <button type="button" className="dp-explore-button" disabled={!canExport} onClick={onExport}>
            Plot exportieren
          </button>
        </div>
      </div>
      <div className="dp-explore-r-plot__body">
        {image ? (
          <canvas ref={canvasRef} className="dp-explore-r-plot__canvas" aria-label="Aktueller R Plot" />
        ) : (
          <p className="dp-explore-muted">Noch kein Plot erzeugt.</p>
        )}
      </div>
    </section>
  );
}
