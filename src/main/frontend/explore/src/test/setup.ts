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
