import type {ExploreColumnDto} from '../app/ExploreContext';

export interface RuntimeColumn {
  name: string;
  type: string;
}

export function mergeRuntimeColumns(
  catalogColumns: ExploreColumnDto[],
  runtimeColumns: RuntimeColumn[]
): ExploreColumnDto[] {
  const catalogByName = new Map(catalogColumns.map((column) => [column.name, column]));

  return runtimeColumns.map((runtimeColumn) => {
    const catalogColumn = catalogByName.get(runtimeColumn.name);
    return {
      name: runtimeColumn.name,
      type: runtimeColumn.type,
      nullable: catalogColumn?.nullable,
      required: catalogColumn?.required,
      description: catalogColumn?.description,
      example: catalogColumn?.example,
      roles: catalogColumn?.roles?.length ? catalogColumn.roles : ['unknown']
    };
  });
}
