import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {WebRRuntime} from './WebRRuntime';
import {sampleExploreContext} from '../test/sampleExploreContext';

const mockWebR = vi.hoisted(() => ({
  init: vi.fn(),
  installPackages: vi.fn(),
  close: vi.fn()
}));

vi.mock('webr', () => ({
  ChannelType: {PostMessage: 3},
  WebR: class {
    async init() {
      return mockWebR.init();
    }

    async installPackages(packages: string | string[], options?: Record<string, unknown>) {
      return mockWebR.installPackages(packages, options);
    }

    close() {
      mockWebR.close();
    }
  }
}));

describe('WebRRuntime', () => {
  beforeEach(() => {
    mockWebR.init.mockReset().mockResolvedValue(undefined);
    mockWebR.installPackages.mockReset().mockResolvedValue(undefined);
    mockWebR.close.mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('shares one initialization promise for concurrent callers', async () => {
    const runtime = new WebRRuntime(sampleExploreContext.rLaboratory);
    const first = runtime.initialize();
    const second = runtime.initialize();

    expect(first).toBe(second);
    await first;
    expect(mockWebR.init).toHaveBeenCalledTimes(1);
    expect(mockWebR.installPackages).toHaveBeenCalledTimes(1);
  });

  it('closes the runtime exactly once when close is called repeatedly', async () => {
    const runtime = new WebRRuntime(sampleExploreContext.rLaboratory);
    await runtime.initialize();

    runtime.close();
    runtime.close();

    expect(mockWebR.close).toHaveBeenCalledTimes(1);
    await expect(runtime.initialize()).rejects.toThrow('bereits geschlossen');
  });

  it('closes a runtime that finishes initialization after close', async () => {
    let resolveInit!: () => void;
    mockWebR.init.mockReturnValue(new Promise<void>((resolve) => {
      resolveInit = resolve;
    }));
    const runtime = new WebRRuntime(sampleExploreContext.rLaboratory);
    const pending = runtime.initialize();
    await vi.waitFor(() => expect(mockWebR.init).toHaveBeenCalled());

    runtime.close();
    resolveInit();

    await expect(pending).rejects.toThrow('während des Ladens geschlossen');
    expect(mockWebR.close).toHaveBeenCalledTimes(1);
    expect(mockWebR.installPackages).not.toHaveBeenCalled();
  });

  it('closes the runtime on initialization timeout', async () => {
    vi.useFakeTimers();
    mockWebR.init.mockReturnValue(new Promise<void>(() => undefined));
    const runtime = new WebRRuntime(sampleExploreContext.rLaboratory);
    const pending = runtime.initialize();
    await vi.waitFor(() => expect(mockWebR.init).toHaveBeenCalled());

    await vi.advanceTimersByTimeAsync(120_000);

    await expect(pending).rejects.toThrow('initialisiert werden');
    expect(mockWebR.close).toHaveBeenCalledTimes(1);
  });

  it('closes the runtime on package timeout', async () => {
    vi.useFakeTimers();
    mockWebR.installPackages.mockReturnValue(new Promise<void>(() => undefined));
    const runtime = new WebRRuntime(sampleExploreContext.rLaboratory);
    const pending = runtime.initialize();
    await vi.waitFor(() => expect(mockWebR.installPackages).toHaveBeenCalled());

    await vi.advanceTimersByTimeAsync(180_000);

    await expect(pending).rejects.toThrow('Pakete konnten');
    expect(mockWebR.close).toHaveBeenCalledTimes(1);
  });

  it('allows a controlled retry after a failed initialization', async () => {
    mockWebR.init.mockRejectedValueOnce(new Error('initial failure')).mockResolvedValueOnce(undefined);
    const runtime = new WebRRuntime(sampleExploreContext.rLaboratory);

    await expect(runtime.initialize()).rejects.toThrow('initial failure');
    await expect(runtime.initialize()).resolves.toBeDefined();
    expect(mockWebR.init).toHaveBeenCalledTimes(2);
    expect(mockWebR.close).toHaveBeenCalledTimes(1);
  });

  it('does not publish progress after close', async () => {
    let resolveInit!: () => void;
    mockWebR.init.mockReturnValue(new Promise<void>((resolve) => {
      resolveInit = resolve;
    }));
    const onStep = vi.fn();
    const runtime = new WebRRuntime(sampleExploreContext.rLaboratory, onStep);
    const pending = runtime.initialize();
    await vi.waitFor(() => expect(mockWebR.init).toHaveBeenCalled());
    const stepsBeforeClose = onStep.mock.calls.length;

    runtime.close();
    resolveInit();
    await expect(pending).rejects.toThrow();

    expect(onStep.mock.calls.length).toBe(stepsBeforeClose);
    expect(onStep).not.toHaveBeenCalledWith({step: 'runtime', status: 'done'});
    expect(onStep).not.toHaveBeenCalledWith({step: 'packages', status: 'running'});
  });
});
