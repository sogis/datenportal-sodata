import {configureMonacoLoader} from '@sqlrooms/monaco-editor';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api.js';
import 'monaco-editor/esm/vs/editor/contrib/suggest/browser/suggestController.js';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';

export function configureBundledMonaco(): void {
  configureMonacoLoader({
    monaco,
    workers: {
      default: editorWorker
    }
  });
}
