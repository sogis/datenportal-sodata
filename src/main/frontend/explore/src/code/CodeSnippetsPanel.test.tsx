import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {CodeSnippetsPanel} from './CodeSnippetsPanel';
import type {ExploreCodeSnippetDto} from '../app/ExploreContext';

const mocks = vi.hoisted(() => ({
  copyTextToClipboard: vi.fn()
}));

vi.mock('../sql/clipboard', () => ({
  copyTextToClipboard: mocks.copyTextToClipboard
}));

describe('CodeSnippetsPanel', () => {
  beforeEach(() => {
    mocks.copyTextToClipboard.mockReset().mockResolvedValue(true);
  });

  it('renders snippets as tabs and switches selected code', async () => {
    const user = userEvent.setup();
    render(<CodeSnippetsPanel snippets={snippets} />);

    expect(screen.getByRole('tab', {name: 'DuckDB CLI'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByLabelText('DuckDB CLI Code')).toHaveTextContent('install httpfs');

    await user.click(screen.getByRole('tab', {name: 'Python mit DuckDB'}));

    expect(screen.getByRole('tab', {name: 'Python mit DuckDB'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByLabelText('Python mit DuckDB Code')).toHaveTextContent('import duckdb');
  });

  it('copies the selected snippet with visible feedback', async () => {
    const user = userEvent.setup();
    render(<CodeSnippetsPanel snippets={snippets} />);

    await user.click(screen.getByRole('button', {name: 'Code kopieren'}));

    expect(mocks.copyTextToClipboard).toHaveBeenCalledWith(snippets[0].code);
    expect(await screen.findByRole('button', {name: 'Code kopiert'})).toBeInTheDocument();
  });

  it('renders an empty state without snippets', () => {
    render(<CodeSnippetsPanel snippets={[]} />);

    expect(screen.getByText('Für dieses Datenthema sind keine Codebeispiele verfügbar.')).toBeInTheDocument();
  });
});

const snippets: ExploreCodeSnippetDto[] = [
  {
    id: 'duckdb-cli',
    title: 'DuckDB CLI',
    language: 'sql',
    code: "install httpfs;\nload httpfs;\nselect * from read_parquet('https://data.so.ch/example.parquet');"
  },
  {
    id: 'python-duckdb',
    title: 'Python mit DuckDB',
    language: 'python',
    code: 'import duckdb'
  },
  {
    id: 'r-duckdb',
    title: 'R mit duckdb',
    language: 'r',
    code: 'library(duckdb)'
  }
];
