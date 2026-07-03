import {useCallback, useEffect, useRef} from 'react';
import {SqlMonacoEditor, type SqlMonacoEditorProps} from '@sqlrooms/sql-editor';
import type {DataTable} from '@sqlrooms/duckdb';

type MonacoEditorInstance = Parameters<NonNullable<SqlMonacoEditorProps['onMount']>>[0];

export function SqlEditorField({
  value,
  onChange,
  onRun,
  disabled,
  tableSchemas,
  getLatestSchemas
}: {
  value: string;
  onChange: (value: string) => void;
  onRun: () => void;
  disabled: boolean;
  tableSchemas: DataTable[];
  getLatestSchemas: () => {tableSchemas: DataTable[]};
}) {
  const editorRef = useRef<MonacoEditorInstance | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const layoutFrameRef = useRef<number | null>(null);

  const layoutEditor = useCallback(() => {
    if (typeof window === 'undefined') {
      return;
    }
    if (layoutFrameRef.current !== null) {
      window.cancelAnimationFrame(layoutFrameRef.current);
    }
    layoutFrameRef.current = window.requestAnimationFrame(() => {
      layoutFrameRef.current = null;
      editorRef.current?.layout();
    });
  }, []);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) {
      return undefined;
    }

    const observer = new ResizeObserver(() => layoutEditor());
    observer.observe(container);
    layoutEditor();

    return () => {
      observer.disconnect();
      if (layoutFrameRef.current !== null) {
        window.cancelAnimationFrame(layoutFrameRef.current);
        layoutFrameRef.current = null;
      }
    };
  }, [layoutEditor]);

  const handleEditorMount = useCallback<NonNullable<SqlMonacoEditorProps['onMount']>>((editor) => {
    editorRef.current = editor;
    layoutEditor();
  }, [layoutEditor]);

  const handleKeyDown = useCallback((event: React.KeyboardEvent<HTMLDivElement>) => {
    if ((event.metaKey || event.ctrlKey) && event.key === 'Enter') {
      event.preventDefault();
      onRun();
    }
  }, [onRun]);

  return (
    <div className="dp-explore-editor" onKeyDown={handleKeyDown}>
      <label className="dp-visually-hidden" htmlFor="dp-explore-sql-fallback">SQL Fallback bearbeiten</label>
      <div className="dp-explore-editor__monaco" data-testid="sql-monaco-editor" ref={containerRef}>
        <SqlMonacoEditor
          value={value}
          onChange={(nextValue) => onChange(nextValue ?? '')}
          tableSchemas={tableSchemas}
          getLatestSchemas={getLatestSchemas}
          theme="light"
          height="100%"
          readOnly={disabled}
          onMount={handleEditorMount}
          options={{
            minimap: {enabled: false},
            readOnly: disabled,
            fontSize: 13,
            scrollBeyondLastLine: false,
            quickSuggestions: {other: true, comments: false, strings: false},
            quickSuggestionsDelay: 80,
            suggestOnTriggerCharacters: true,
            wordWrap: 'on',
            automaticLayout: true,
            lineNumbers: 'on',
            fontFamily: 'JetBrains Mono',
            lineHeight: 20,
            glyphMargin: false,
            folding: false,
            renderLineHighlight: 'none',
            overviewRulerBorder: false,
            hideCursorInOverviewRuler: true
          }}
        />
      </div>
      <textarea
        id="dp-explore-sql-fallback"
        className="dp-explore-editor__textarea"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        onKeyDown={(event) => {
          if ((event.metaKey || event.ctrlKey) && event.key === 'Enter') {
            event.preventDefault();
            onRun();
          }
        }}
        disabled={disabled}
      />
    </div>
  );
}
