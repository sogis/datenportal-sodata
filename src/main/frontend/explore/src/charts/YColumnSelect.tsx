import {useId, useLayoutEffect, useRef, useState, type CSSProperties} from 'react';
import {createPortal} from 'react-dom';

export function YColumnSelect({columns, selected, onChange}: {
  columns: string[]; selected: string[]; onChange: (values: string[]) => void;
}) {
  const id = useId();
  const trigger = useRef<HTMLButtonElement>(null);
  const popup = useRef<HTMLDivElement>(null);
  const [open, setOpen] = useState(false);
  const [position, setPosition] = useState<CSSProperties>({});

  useLayoutEffect(() => {
    if (!open) return;
    const place = () => {
      const rect = trigger.current!.getBoundingClientRect();
      const below = window.innerHeight - rect.bottom - 8;
      const above = rect.top - 8;
      const upwards = below < 160 && above > below;
      const width = Math.min(Math.max(rect.width, 220), window.innerWidth - 16);
      setPosition({
        width,
        left: Math.max(8, Math.min(rect.left, window.innerWidth - width - 8)),
        top: upwards ? undefined : rect.bottom + 4,
        bottom: upwards ? window.innerHeight - rect.top + 4 : undefined,
        maxHeight: Math.max(40, Math.min(280, upwards ? above : below))
      });
    };
    const outside = (event: Event) => {
      const target = event.target as Node;
      if (!trigger.current?.contains(target) && !popup.current?.contains(target)) setOpen(false);
    };
    place();
    (popup.current?.querySelector<HTMLInputElement>('input:not(:disabled)') ?? popup.current)?.focus();
    const observer = new ResizeObserver(place);
    observer.observe(trigger.current!);
    window.addEventListener('resize', place);
    window.addEventListener('scroll', place, true);
    document.addEventListener('pointerdown', outside);
    document.addEventListener('focusin', outside);
    return () => {
      observer.disconnect();
      window.removeEventListener('resize', place);
      window.removeEventListener('scroll', place, true);
      document.removeEventListener('pointerdown', outside);
      document.removeEventListener('focusin', outside);
    };
  }, [open]);

  return <div className="dp-explore-y-select">
    <span id={`${id}-label`}>Y (Zahl)</span>
    <span id={`${id}-value`} className="dp-visually-hidden">{selected.join(', ')}</span>
    <button ref={trigger} type="button" className="dp-explore-y-select__trigger"
      aria-labelledby={`${id}-label`} aria-describedby={`${id}-value`}
      aria-haspopup="dialog" aria-expanded={open} aria-controls={open ? id : undefined}
      disabled={columns.length === 0} onClick={() => setOpen(!open)}>
      <span title={selected.join(', ')}>{selected[0] ?? 'Keine Zahlenspalte'}</span>
      {selected.length > 1 && <span>+{selected.length - 1}</span>}
      <span aria-hidden="true">▾</span>
    </button>
    {open && createPortal(<div ref={popup} id={id} role="dialog" tabIndex={-1} aria-label="Y-Attribute auswählen"
      className="dp-explore-y-select__popup" style={position} data-export-ignore="true"
      onKeyDown={(event) => {
        const inputs = Array.from(popup.current!.querySelectorAll<HTMLInputElement>('input:not(:disabled)'));
        const index = inputs.indexOf(document.activeElement as HTMLInputElement);
        if (event.key === 'Escape') {
          event.preventDefault(); setOpen(false); trigger.current?.focus();
        } else if (['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) {
          event.preventDefault();
          const next = event.key === 'Home' ? 0 : event.key === 'End' ? inputs.length - 1
            : (index + (event.key === 'ArrowDown' ? 1 : -1) + inputs.length) % inputs.length;
          inputs[next]?.focus();
        } else if (event.key === 'Tab' && (!inputs.length || (event.shiftKey ? index === 0 : index === inputs.length - 1))) {
          event.preventDefault(); setOpen(false);
          const controls = Array.from(trigger.current!.closest('[aria-label="Diagrammsteuerung"]')!
            .querySelectorAll<HTMLElement>('button, select'));
          (event.shiftKey ? trigger.current : controls[controls.indexOf(trigger.current!) + 1])?.focus();
        }
      }}>
      {columns.map((column) => <label key={column}>
        <input type="checkbox" checked={selected.includes(column)}
          disabled={selected.length === 1 && selected[0] === column}
          onChange={() => onChange(selected.includes(column)
            ? selected.filter((name) => name !== column) : [...selected, column])} />
        <span>{column}</span>
      </label>)}
    </div>, document.body)}
  </div>;
}
