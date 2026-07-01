import '@testing-library/jest-dom/vitest';
import React from 'react';
import {vi} from 'vitest';

vi.mock('@sqlrooms/sql-editor', () => ({
  SqlMonacoEditor: ({
    value,
    onChange,
    readOnly,
    options
  }: {
    value?: string;
    onChange?: (value: string) => void;
    readOnly?: boolean;
    options?: {readOnly?: boolean};
  }) => React.createElement('textarea', {
    'aria-label': 'SQL bearbeiten',
    value: value ?? '',
    disabled: readOnly ?? options?.readOnly ?? false,
    onChange: (event: React.ChangeEvent<HTMLTextAreaElement>) => onChange?.(event.target.value)
  })
}));

const chartComponent = (tag: string) => ({children}: {children?: React.ReactNode; [key: string]: unknown}) =>
  React.createElement(tag, {}, children);

vi.mock('@sqlrooms/recharts', () => ({
  Bar: chartComponent('div'),
  BarChart: chartComponent('div'),
  CartesianGrid: chartComponent('div'),
  ChartContainer: ({children, className}: {children?: React.ReactNode; className?: string}) =>
    React.createElement('div', {className, 'data-testid': 'chart-container'}, children),
  ChartTooltip: chartComponent('div'),
  ChartTooltipContent: chartComponent('div'),
  Line: chartComponent('div'),
  LineChart: chartComponent('div'),
  Scatter: chartComponent('div'),
  ScatterChart: chartComponent('div'),
  XAxis: chartComponent('div'),
  YAxis: chartComponent('div')
}));
