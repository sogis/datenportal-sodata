import type {ExploreFeatureFlagsDto} from './ExploreContext';

const futureSlots: Array<{flag: keyof Pick<ExploreFeatureFlagsDto, 'aiAssistant' | 'vega' | 'mosaic' | 'geospatial'>; label: string}> = [
  {flag: 'aiAssistant', label: 'AI SQL-Vorschlaege'},
  {flag: 'vega', label: 'Vega-Lite'},
  {flag: 'mosaic', label: 'Mosaic Crossfilter'},
  {flag: 'geospatial', label: 'Geodaten-Erkundung'}
];

export function FutureExtensionSlots({flags}: {flags: ExploreFeatureFlagsDto}) {
  const enabledSlots = futureSlots.filter((slot) => flags[slot.flag]);

  if (enabledSlots.length === 0) {
    return null;
  }

  return (
    <section className="dp-explore-future-slots" aria-label="Vorbereitete Erweiterungen">
      <h3>Vorbereitete Erweiterungen</h3>
      <ul>
        {enabledSlots.map((slot) => (
          <li key={slot.flag}>{slot.label}: deaktiviert, bis die Erweiterung implementiert ist.</li>
        ))}
      </ul>
    </section>
  );
}
