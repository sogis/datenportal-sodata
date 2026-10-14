import {describe, expect, it} from 'vitest';
import {dependencyNames, formatPackageIndex, parsePackageIndex, resolvePackageClosure, selectLockedRecords} from './mirrorWebRPackages.mjs';

const packageIndex = `Package: ggplot2
Version: 1.0.0
Imports: rlang, scales, stats

Package: rlang
Version: 1.2.0

Package: scales
Version: 1.4.0
Depends: R (>= 4.0), cli

Package: cli
Version: 3.6.6
`;

describe('mirrorWebRPackages helpers', () => {
  it('parses PACKAGES stanzas and resolves dependency closure without base R packages', () => {
    const records = parsePackageIndex(packageIndex);

    expect(records).toHaveLength(4);
    expect(dependencyNames(records[0])).toEqual(['rlang', 'scales']);
    expect(resolvePackageClosure(records, ['ggplot2'])).toEqual(['cli', 'ggplot2', 'rlang', 'scales']);
  });

  it('validates the lock and writes a filtered PACKAGES index', () => {
    const records = parsePackageIndex(packageIndex);
    const selected = selectLockedRecords(records, {
      repoBaseUrl: 'https://example.test',
      rootPackages: ['ggplot2'],
      packages: [
        {name: 'ggplot2', version: '1.0.0'},
        {name: 'rlang', version: '1.2.0'},
        {name: 'scales', version: '1.4.0'},
        {name: 'cli', version: '3.6.6'}
      ]
    });

    expect(selected.map((record) => record.Package)).toEqual(['cli', 'ggplot2', 'rlang', 'scales']);
    expect(formatPackageIndex(selected)).toContain('Package: ggplot2');
    expect(formatPackageIndex(selected)).not.toContain('Package: missing');
  });

  it('fails when a locked dependency is missing', () => {
    expect(() => selectLockedRecords(parsePackageIndex(packageIndex), {
      repoBaseUrl: 'https://example.test',
      rootPackages: ['ggplot2'],
      packages: [{name: 'ggplot2', version: '1.0.0'}]
    })).toThrow('Lock file is missing resolved WebR dependency');
  });

  it('requires the exact locked KernSmooth version when the upstream index changes', () => {
    const records = parsePackageIndex('Package: KernSmooth\nVersion: 2.23-27\nDepends: R (>= 2.5.0), stats\n');
    const lock = {
      repoBaseUrl: 'https://example.test',
      rootPackages: ['KernSmooth'],
      packages: [{name: 'KernSmooth', version: '2.23-26'}]
    };

    expect(() => selectLockedRecords(records, lock))
      .toThrow('Locked WebR package KernSmooth expected 2.23-26, found 2.23-27.');
    expect(selectLockedRecords(records, {
      ...lock,
      packages: [{name: 'KernSmooth', version: '2.23-27'}]
    })).toEqual(records);
  });
});
