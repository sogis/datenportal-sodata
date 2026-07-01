import {createDuckDbSlice, createWasmDuckDbConnector, type DuckDbSliceState} from '@sqlrooms/duckdb';
import {createBaseRoomSlice, createRoomStore, type BaseRoomStoreState} from '@sqlrooms/room-store';
import type {ExploreContextDto} from '../app/ExploreContext';
import {createLocalDuckDbBundles} from './duckdbBundles';

export type ExploreRoomStoreState = BaseRoomStoreState & DuckDbSliceState;
export type ExploreRoomStore = ReturnType<typeof createExploreRoomStore>;

export function createExploreRoomStore(_context: ExploreContextDto) {
  const connector = createWasmDuckDbConnector({
    bundles: createLocalDuckDbBundles(),
    initializationQuery: `set custom_extension_repository='${extensionRepositoryUrl()}';`,
    path: ':memory:'
  });

  return createRoomStore<ExploreRoomStoreState>((set, get, store) => ({
    ...createBaseRoomSlice()(set, get, store),
    ...createDuckDbSlice({connector})(set, get, store)
  }));
}

function extensionRepositoryUrl(): string {
  return `${globalThis.location.origin}/explore-extensions`.replace(/'/g, "''");
}
