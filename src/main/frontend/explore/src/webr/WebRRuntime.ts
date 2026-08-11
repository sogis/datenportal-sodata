import type {ExploreRLaboratoryDto} from '../app/ExploreContext';

export type WebRLoadStep = 'runtime' | 'packages' | 'data';
export type WebRStepStatus = 'pending' | 'running' | 'done' | 'error';

export interface WebRStepState {
  step: WebRLoadStep;
  status: WebRStepStatus;
}

export interface WebRShelterLike {
  captureR: (code: string, options?: Record<string, unknown>) => Promise<{
    result: unknown;
    output: Array<{type: string; data: unknown}>;
    images: ImageBitmap[];
  }>;
  purge: () => Promise<void>;
}

export interface WebRLike {
  init: () => Promise<unknown>;
  close: () => void;
  installPackages: (packages: string | string[], options?: {repos?: string | string[]; quiet?: boolean; mount?: boolean}) => Promise<void>;
  evalRVoid: (code: string, options?: Record<string, unknown>) => Promise<void>;
  evalRBoolean: (code: string, options?: Record<string, unknown>) => Promise<boolean>;
  evalRString: (code: string, options?: Record<string, unknown>) => Promise<string>;
  FS: {
    writeFile: (path: string, data: ArrayBufferView, flags?: string) => Promise<void>;
    readFile: (path: string, flags?: string) => Promise<Uint8Array>;
    unlink: (path: string) => Promise<void>;
  };
  Shelter: new () => Promise<WebRShelterLike>;
}

type WebRModule = typeof import('webr');

export class WebRRuntime {
  private readonly config: ExploreRLaboratoryDto;
  private readonly onStep?: (state: WebRStepState) => void;
  private webRPromise?: Promise<WebRLike>;
  private webR?: WebRLike;
  private closed = false;

  constructor(config: ExploreRLaboratoryDto, onStep?: (state: WebRStepState) => void) {
    this.config = config;
    this.onStep = onStep;
  }

  initialize(): Promise<WebRLike> {
    if (this.closed) {
      return Promise.reject(new Error('Die WebR-Runtime wurde bereits geschlossen.'));
    }
    if (!this.webRPromise) {
      const pending = this.load();
      this.webRPromise = pending;
      pending.catch(() => {
        if (this.webRPromise === pending) {
          this.webRPromise = undefined;
        }
      });
    }
    return this.webRPromise;
  }

  close(): void {
    if (this.closed) {
      return;
    }
    this.closed = true;
    this.closeWebR(this.webR);
    void this.webRPromise?.then((webR) => this.closeWebR(webR)).catch(() => undefined);
  }

  private async load(): Promise<WebRLike> {
    let webR: WebRLike | undefined;
    try {
      this.report('runtime', 'running');
      const webRModule: WebRModule = import.meta.env.VITEST
        ? await import('webr')
        : await import(/* @vite-ignore */ `${this.config.runtimeBaseUrl}webr.js`);
      this.ensureOpen();
      webR = new webRModule.WebR({
        baseUrl: this.config.runtimeBaseUrl,
        repoUrl: this.config.packageRepoUrl,
        interactive: false,
        channelType: webRModule.ChannelType.PostMessage
      }) as unknown as WebRLike;
      this.webR = webR;
      this.ensureOpen();
      await withTimeout(
        webR.init(),
        120_000,
        'Die WebR-Runtime konnte nicht initialisiert werden.',
        () => this.closeWebR(webR)
      );
      this.ensureOpen();
      this.report('runtime', 'done');

      this.report('packages', 'running');
      await withTimeout(
        webR.installPackages(this.config.packages, {
          repos: this.config.packageRepoUrl,
          quiet: true,
          mount: true
        }),
        180_000,
        'Die R-Pakete konnten nicht vollständig vorbereitet werden.',
        () => this.closeWebR(webR)
      );
      this.ensureOpen();
      this.report('packages', 'done');
      return webR;
    } catch (error) {
      this.closeWebR(webR);
      throw error;
    }
  }

  private report(step: WebRLoadStep, status: WebRStepStatus): void {
    if (!this.closed) {
      this.onStep?.({step, status});
    }
  }

  private ensureOpen(): void {
    if (this.closed) {
      throw new Error('Die WebR-Runtime wurde während des Ladens geschlossen.');
    }
  }

  private closeWebR(webR: WebRLike | undefined): void {
    if (!webR || this.webR !== webR) {
      return;
    }
    this.webR = undefined;
    try {
      webR.close();
    } catch {
      // Cleanup must not replace the original load or timeout error.
    }
  }
}

function withTimeout<T>(promise: Promise<T>, timeoutMs: number, message: string, onTimeout?: () => void): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timeoutId = window.setTimeout(() => {
      try {
        onTimeout?.();
      } catch {
        // Cleanup errors must not hide the primary timeout.
      }
      reject(new Error(message));
    }, timeoutMs);
    promise.then(
      (value) => {
        window.clearTimeout(timeoutId);
        resolve(value);
      },
      (error: unknown) => {
        window.clearTimeout(timeoutId);
        reject(error);
      }
    );
  });
}

export function initialWebRSteps(): WebRStepState[] {
  return [
    {step: 'runtime', status: 'pending'},
    {step: 'packages', status: 'pending'},
    {step: 'data', status: 'pending'}
  ];
}

export function stepLabel(step: WebRLoadStep): string {
  switch (step) {
    case 'runtime':
      return 'Runtime';
    case 'packages':
      return 'Pakete';
    case 'data':
      return 'Daten übernehmen';
  }
}
