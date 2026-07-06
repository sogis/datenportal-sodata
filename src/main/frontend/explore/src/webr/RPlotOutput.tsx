import {useEffect, useRef} from 'react';

export function RPlotOutput({
  image,
  onCanvasReady
}: {
  image?: ImageBitmap;
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
        <h3>Plot</h3>
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
