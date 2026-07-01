import {describe, expect, it, vi} from 'vitest';
import {copyTextToClipboard} from './clipboard';

describe('copyTextToClipboard', () => {
  it('writes text to the provided clipboard', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined);

    await expect(copyTextToClipboard('select 1', {writeText})).resolves.toBe(true);

    expect(writeText).toHaveBeenCalledWith('select 1');
  });

  it('returns false when no clipboard is available', async () => {
    await expect(copyTextToClipboard('select 1', undefined)).resolves.toBe(false);
  });
});

