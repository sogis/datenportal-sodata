import {useCallback} from 'react';
import {SqlMonacoEditor} from '@sqlrooms/sql-editor';
import type {DuckDbConnector} from '@sqlrooms/duckdb';

export function SqlEditorField({
  value,
  onChange,
  onRun,
  disabled,
  connector,
  tableNames
}: {
  value: string;
  onChange: (value: string) => void;
  onRun: () => void;
  disabled: boolean;
  connector?: DuckDbConnector;
  tableNames: string[];
}) {
  const handleKeyDown = useCallback((event: React.KeyboardEvent<HTMLDivElement>) => {
    if ((event.metaKey || event.ctrlKey) && event.key === 'Enter') {
      event.preventDefault();
      onRun();
    }
  }, [onRun]);

  return (
    <div className="dp-explore-editor" onKeyDown={handleKeyDown}>
      <label htmlFor="dp-explore-sql-fallback">SQL</label>
      <div className="dp-explore-editor__monaco" data-testid="sql-monaco-editor">
        <SqlMonacoEditor
          value={value}
          onChange={(nextValue) => onChange(nextValue ?? '')}
          connector={connector}
          customKeywords={tableNames}
          theme="light"
          height="240px"
          options={{
            minimap: {enabled: false},
            readOnly: disabled,
            fontSize: 15,
            scrollBeyondLastLine: false,
            wordWrap: 'on',
            automaticLayout: true,
            lineNumbers: 'on'
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
        aria-label="SQL Fallback bearbeiten"
      />
    </div>
  );
}
