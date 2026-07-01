import {render, screen} from '@testing-library/react';
import {describe, expect, it} from 'vitest';
import {FutureExtensionSlots} from './FutureExtensionSlots';
import {sampleExploreContext} from '../test/sampleExploreContext';

describe('FutureExtensionSlots', () => {
  it('renders nothing while all future feature flags are disabled', () => {
    const {container} = render(<FutureExtensionSlots flags={sampleExploreContext.featureFlags} />);

    expect(container).toBeEmptyDOMElement();
    expect(screen.queryByLabelText('Vorbereitete Erweiterungen')).not.toBeInTheDocument();
  });

  it('renders a quiet prepared-state list only when future flags are enabled', () => {
    render(<FutureExtensionSlots flags={{...sampleExploreContext.featureFlags, aiAssistant: true, geospatial: true}} />);

    expect(screen.getByLabelText('Vorbereitete Erweiterungen')).toBeInTheDocument();
    expect(screen.getByText('AI SQL-Vorschlaege: deaktiviert, bis die Erweiterung implementiert ist.')).toBeInTheDocument();
    expect(screen.getByText('Geodaten-Erkundung: deaktiviert, bis die Erweiterung implementiert ist.')).toBeInTheDocument();
    expect(screen.queryByText(/WebR/)).not.toBeInTheDocument();
  });
});
