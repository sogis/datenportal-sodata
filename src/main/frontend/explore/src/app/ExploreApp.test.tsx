import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {describe, expect, it} from 'vitest';
import {ExploreApp} from './ExploreApp';
import {sampleExploreContext} from '../test/sampleExploreContext';

describe('ExploreApp', () => {
  it('renders the phase 2 island skeleton', () => {
    render(<ExploreApp context={sampleExploreContext} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('1 Tabelle')).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Vorschau'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Diagramm'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Code'})).toBeInTheDocument();
    expect(screen.getByText('DuckDB wird vorbereitet')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL Vorschau')).toHaveTextContent('select *');
  });

  it('switches placeholder text when tabs change', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    await user.click(screen.getByRole('tab', {name: 'SQL-Labor'}));

    expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByText('Der SQL-Editor wird hier eingebunden, sobald DuckDB-Wasm initialisiert wird.')).toBeInTheDocument();
  });

  it('renders an unavailable state without Parquet tables', () => {
    render(<ExploreApp context={{...sampleExploreContext, tables: [], recipes: [], codeSnippets: []}} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: 'Downloads und Metadaten auf der Datensatzseite anzeigen'})).toHaveAttribute('href', '/datasets/ch.so.bauinventar');
  });
});
