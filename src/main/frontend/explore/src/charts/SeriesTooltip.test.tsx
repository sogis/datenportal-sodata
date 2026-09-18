import {render, screen} from '@testing-library/react';
import {describe, expect, it} from 'vitest';
import {axisTooltipLabel, formatTooltipNumber} from './chartSeries';
import {SeriesTooltipRow} from './SeriesTooltipContent';

describe('series tooltip', () => {
  it('uses the x-axis value as the tooltip header', () => {
    expect(axisTooltipLabel([{payload: {axisX: 1965}}])).toBe('1965');
    expect(axisTooltipLabel([{payload: {axisX: 'Solothurn'}}])).toBe('Solothurn');
    expect(axisTooltipLabel([{payload: {axisX: 2020}}, {payload: {axisX: 2020}}])).toBe('2020');
  });

  it('keeps missing x values empty', () => {
    expect(axisTooltipLabel(undefined)).toBe('');
    expect(axisTooltipLabel([])).toBe('');
    expect(axisTooltipLabel([{payload: {}}])).toBe('');
    expect(axisTooltipLabel([{payload: {axisX: null}}])).toBe('');
    expect(axisTooltipLabel([{payload: {axisX: ''}}])).toBe('');
  });

  it('formats series values with the swiss number format', () => {
    expect(formatTooltipNumber(1564)).toBe('1’564');
    expect(formatTooltipNumber(0)).toBe('0');
    expect(formatTooltipNumber(1234.5)).toBe('1’234.5');
    expect(formatTooltipNumber(null)).toBe('');
    expect(formatTooltipNumber(undefined)).toBe('');
  });

  it('renders the series name, swiss value and color indicator', () => {
    render(<SeriesTooltipRow value={1564} name="Auslaender" indicatorColor="#104E8B" />);

    expect(screen.getByText('Auslaender')).toBeInTheDocument();
    expect(screen.getByText('1’564')).toBeInTheDocument();
    expect(screen.getByText('Auslaender').closest('.dp-explore-series-tooltip__row')).not.toBeNull();
    expect(screen.getByTestId('chart-series-swatch').style.getPropertyValue('--color-bg')).toBe('#104E8B');
  });

  it('renders a zero value but hides missing values', () => {
    const {rerender} = render(<SeriesTooltipRow value={0} name="Messung.A" indicatorColor="#104E8B" />);

    expect(screen.getByText('0')).toBeInTheDocument();

    rerender(<SeriesTooltipRow value={null} name="Messung.A" indicatorColor="#104E8B" />);

    expect(screen.queryByText('0')).not.toBeInTheDocument();
  });
});
