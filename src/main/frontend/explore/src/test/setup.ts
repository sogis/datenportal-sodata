import '@testing-library/jest-dom/vitest';
import React from 'react';
import {vi} from 'vitest';

class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

if (!globalThis.ResizeObserver) {
  globalThis.ResizeObserver = ResizeObserverMock as typeof ResizeObserver;
}

if (!window.matchMedia) {
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    writable: true,
    value: vi.fn().mockImplementation((query: string) => ({
      matches: false,
      media: query,
      onchange: null,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      addListener: vi.fn(),
      removeListener: vi.fn(),
      dispatchEvent: vi.fn()
    }))
  });
}

vi.mock('@sqlrooms/sql-editor', () => ({
  SqlMonacoEditor: ({
    value,
    onChange,
    readOnly,
    options,
    onMount,
    connector,
    tableSchemas,
    getLatestSchemas,
    customKeywords
  }: {
    value?: string;
    onChange?: (value: string) => void;
    readOnly?: boolean;
    options?: {readOnly?: boolean};
    onMount?: (editor: {
      layout: () => void;
    }) => void;
    connector?: unknown;
    tableSchemas?: Array<{tableName?: string; columns?: Array<{name: string; type: string}>}>;
    getLatestSchemas?: () => {tableSchemas?: Array<{tableName?: string; columns?: Array<{name: string; type: string}>}>};
    customKeywords?: string[];
  }) => {
    React.useEffect(() => {
      onMount?.({
        layout: vi.fn()
      });
    }, [onMount]);
    return React.createElement('textarea', {
      'aria-label': 'SQL bearbeiten',
      'data-has-connector': connector ? 'true' : 'false',
      'data-table-schemas': tableSchemas?.map((table) => table.tableName).join(',') ?? '',
      'data-table-columns': tableSchemas?.flatMap((table) => table.columns?.map((column) => column.name) ?? []).join(',') ?? '',
      'data-latest-schemas': getLatestSchemas?.().tableSchemas?.map((table) => table.tableName).join(',') ?? '',
      'data-custom-keywords': customKeywords?.join(',') ?? '',
      value: value ?? '',
      disabled: readOnly ?? options?.readOnly ?? false,
      onChange: (event: React.ChangeEvent<HTMLTextAreaElement>) => onChange?.(event.target.value)
    });
  }
}));

vi.mock('@sqlrooms/duckdb', () => ({
  escapeVal: (value: unknown) => `'${String(value).replace(/'/g, "''")}'`,
  isWasmDuckDbConnector: (connector: {type?: string} | undefined) => connector?.type === 'wasm',
  makeQualifiedTableName: ({
    database,
    schema,
    table
  }: {
    database?: string;
    schema?: string;
    table: string;
  }) => ({
    database,
    schema,
    table,
    toString: () => [database, schema, table].filter(Boolean).join('.')
  })
}));

const chartComponent = (tag: string) => ({children}: {children?: React.ReactNode; [key: string]: unknown}) =>
  React.createElement(tag, {}, children);

vi.mock('@sqlrooms/recharts', () => ({
  Bar: ({children, fill}: {children?: React.ReactNode; fill?: string}) =>
    React.createElement('div', {'data-testid': 'chart-bar', 'data-fill': fill}, children),
  BarChart: chartComponent('div'),
  CartesianGrid: chartComponent('div'),
  Cell: ({fill}: {fill?: string}) => React.createElement('span', {'data-testid': 'chart-cell', 'data-fill': fill}),
  ChartContainer: ({children, className}: {children?: React.ReactNode; className?: string}) =>
    React.createElement('div', {className, 'data-testid': 'chart-container'}, children),
  ChartTooltip: chartComponent('div'),
  ChartTooltipContent: chartComponent('div'),
  Line: ({stroke}: {stroke?: string}) => React.createElement('div', {'data-testid': 'chart-line', 'data-stroke': stroke}),
  LineChart: chartComponent('div'),
  Pie: chartComponent('div'),
  PieChart: chartComponent('div'),
  Scatter: ({fill}: {fill?: string}) => React.createElement('div', {'data-testid': 'chart-scatter', 'data-fill': fill}),
  ScatterChart: chartComponent('div'),
  XAxis: chartComponent('div'),
  YAxis: chartComponent('div')
}));
