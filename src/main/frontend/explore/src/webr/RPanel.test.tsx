import {render, screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {RPanel} from './RPanel';
import {sampleExploreContext} from '../test/sampleExploreContext';
import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';

const mockWebR = vi.hoisted(() => ({
  writeFile: vi.fn(),
  readFile: vi.fn(),
  unlink: vi.fn(),
  init: vi.fn(),
  installPackages: vi.fn(),
  evalRVoid: vi.fn(),
  evalRBoolean: vi.fn(),
  captureR: vi.fn(),
  close: vi.fn()
}));

vi.mock('webr', () => ({
  ChannelType: {PostMessage: 3},
  WebR: class {
    FS = {
      writeFile: mockWebR.writeFile,
      readFile: mockWebR.readFile,
      unlink: mockWebR.unlink
    };

    Shelter = class {
      async captureR(code: string, options?: Record<string, unknown>) {
        return mockWebR.captureR(code, options);
      }

      async purge() {}
    };

    async init() {
      return mockWebR.init();
    }

    async installPackages(packages: string | string[], options?: Record<string, unknown>) {
      return mockWebR.installPackages(packages, options);
    }

    async evalRVoid(code: string, options?: Record<string, unknown>) {
      return mockWebR.evalRVoid(code, options);
    }

    async evalRBoolean(code: string) {
      return mockWebR.evalRBoolean(code);
    }

    close() {
      mockWebR.close();
    }
  }
}));

describe('RPanel', () => {
  beforeEach(() => {
    mockWebR.writeFile.mockReset().mockResolvedValue(undefined);
    mockWebR.readFile.mockReset().mockResolvedValue(new TextEncoder().encode('"a";"b"\r\n"1";"2"\r\n'));
    mockWebR.unlink.mockReset().mockResolvedValue(undefined);
    mockWebR.init.mockReset().mockResolvedValue(undefined);
    mockWebR.installPackages.mockReset().mockResolvedValue(undefined);
    mockWebR.evalRVoid.mockReset().mockResolvedValue(undefined);
    mockWebR.evalRBoolean.mockReset().mockResolvedValue(true);
    mockWebR.captureR.mockReset().mockResolvedValue({
      output: [{type: 'stdout', data: 'str output'}],
      images: []
    });
    mockWebR.close.mockReset();
  });

  it('loads WebR, transfers daten and runs R code through captureR', async () => {
    const user = userEvent.setup();
    const onInfo = vi.fn();

    render(<RPanel context={sampleExploreContext} snapshot={smallSnapshot()} onDataFrameInfoChange={onInfo} onBackToSql={vi.fn()} />);

    expect(screen.getByRole('status', {name: 'WebR Status'})).toHaveTextContent('WebR wird geladen');
    expect(await screen.findByText(/daten ist bereit/)).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.queryByLabelText('WebR Status')).not.toBeInTheDocument();
    });
    expect(mockWebR.installPackages).toHaveBeenCalledWith(sampleExploreContext.rLaboratory.packages, expect.objectContaining({
      repos: '/webr-packages/'
    }));
    expect(onInfo).toHaveBeenLastCalledWith(expect.objectContaining({name: 'daten', rowCount: 2, columnCount: 2}));

    const rActions = screen.getByLabelText('R Aktionen');
    expect(within(rActions).queryByRole('button', {name: 'Resultat exportieren'})).not.toBeInTheDocument();
    expect(within(rActions).queryByRole('button', {name: 'Plot exportieren'})).not.toBeInTheDocument();

    const consoleOutput = screen.getByLabelText('R Konsole');
    const plotOutput = screen.getByLabelText('R Plot');
    expect(screen.getByLabelText('R-Konsole und R-Plot Grösse anpassen')).toBeInTheDocument();
    expect(within(consoleOutput).queryByRole('heading', {name: /Konsole/i})).not.toBeInTheDocument();
    expect(within(plotOutput).queryByRole('heading', {name: /Plot/i})).not.toBeInTheDocument();
    expect(within(consoleOutput).getByRole('button', {name: 'Resultat exportieren'})).toBeDisabled();
    expect(within(plotOutput).getByRole('button', {name: 'Plot exportieren'})).toBeDisabled();

    await user.click(screen.getByRole('button', {name: 'R ausführen'}));

    expect(await screen.findByText('str output')).toBeInTheDocument();
    await waitFor(() => {
      expect(within(consoleOutput).getByRole('button', {name: 'Resultat exportieren'})).toBeEnabled();
    });
    expect(mockWebR.captureR).toHaveBeenCalledWith(expect.stringContaining('str(daten)'), expect.objectContaining({
      captureGraphics: expect.objectContaining({width: 700, height: 420})
    }));
  });

  it('starts WebR and runs standalone R code without a transferred SQL result', async () => {
    const user = userEvent.setup();
    const onInfo = vi.fn();

    render(<RPanel context={sampleExploreContext} onDataFrameInfoChange={onInfo} onBackToSql={vi.fn()} />);

    expect(screen.getByRole('status', {name: 'WebR Status'})).toHaveTextContent('WebR wird geladen');
    expect(await screen.findByText(/R ist bereit/)).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.queryByText('Kein Data Frame übernommen')).not.toBeInTheDocument();
    });
    expect(onInfo).toHaveBeenLastCalledWith(undefined);
    expect(screen.getByDisplayValue(/data\.frame\(wert = werte/)).toBeEnabled();

    await user.click(screen.getByRole('button', {name: 'R ausführen'}));

    expect(await screen.findByText('str output')).toBeInTheDocument();
    expect(mockWebR.captureR).toHaveBeenCalledWith(expect.stringContaining('data.frame(wert = werte'), expect.any(Object));
  });

  it('warns for oversized data and transfers the recommended slice on request', async () => {
    const user = userEvent.setup();
    const large = {...smallSnapshot(), rowCount: 6000, rows: Array.from({length: 6000}, (_, index) => [index, `name-${index}`])};

    render(<RPanel context={sampleExploreContext} snapshot={large} onDataFrameInfoChange={vi.fn()} onBackToSql={vi.fn()} />);

    expect(screen.getByRole('alert')).toHaveTextContent('Data Frame zu gross');
    await user.click(screen.getByRole('button', {name: /Auf 5.000 Zeilen begrenzen|Auf 5’000 Zeilen begrenzen|Auf 5'000 Zeilen begrenzen/}));

    expect(await screen.findByText(/daten ist bereit \(5/)).toBeInTheDocument();
    const payload = JSON.parse(new TextDecoder().decode(mockWebR.writeFile.mock.calls.at(-1)?.[1] as Uint8Array));
    expect(payload.rows).toHaveLength(5000);
  });

  it('closes WebR and ignores a late initialization after unmount', async () => {
    let resolveInit!: () => void;
    mockWebR.init.mockReturnValue(new Promise<void>((resolve) => {
      resolveInit = resolve;
    }));
    const {unmount} = render(<RPanel context={sampleExploreContext} onDataFrameInfoChange={vi.fn()} onBackToSql={vi.fn()} />);

    await waitFor(() => expect(mockWebR.init).toHaveBeenCalled());
    unmount();
    resolveInit();

    await waitFor(() => expect(mockWebR.close).toHaveBeenCalledTimes(1));
    expect(mockWebR.installPackages).not.toHaveBeenCalled();
  });

  it('does not let an older transfer overwrite a newer transfer', async () => {
    let resolveFirstTransfer!: () => void;
    mockWebR.evalRVoid
      .mockImplementationOnce(() => new Promise<void>((resolve) => {
        resolveFirstTransfer = resolve;
      }))
      .mockResolvedValue(undefined);
    const onInfo = vi.fn();
    const firstSnapshot = smallSnapshot();
    const secondSnapshot = {
      ...firstSnapshot,
      sourceSql: 'select second',
      executedSql: 'select second limit 1000'
    };
    const {rerender} = render(
      <RPanel context={sampleExploreContext} snapshot={firstSnapshot} onDataFrameInfoChange={onInfo} onBackToSql={vi.fn()} />
    );

    await waitFor(() => expect(mockWebR.evalRVoid).toHaveBeenCalledTimes(1));
    rerender(
      <RPanel context={sampleExploreContext} snapshot={secondSnapshot} onDataFrameInfoChange={onInfo} onBackToSql={vi.fn()} />
    );
    await waitFor(() => expect(mockWebR.evalRVoid).toHaveBeenCalledTimes(2));
    resolveFirstTransfer();

    await waitFor(() => expect(onInfo).toHaveBeenLastCalledWith(expect.objectContaining({sourceSql: 'select second'})));
    expect(onInfo).not.toHaveBeenCalledWith(expect.objectContaining({sourceSql: firstSnapshot.sourceSql}));
  });
});

function smallSnapshot(): SqlResultSnapshot {
  return {
    sourceSql: 'select anzahl, name from daten',
    executedSql: 'select anzahl, name from daten limit 1000',
    rowCount: 2,
    columns: [
      {name: 'anzahl', duckdbType: 'INTEGER', nullable: false, roles: ['measure'], rType: 'integer'},
      {name: 'name', duckdbType: 'VARCHAR', nullable: true, roles: ['category'], rType: 'character'}
    ],
    rows: [
      [1, 'Solothurn'],
      [2, 'Olten']
    ]
  };
}
