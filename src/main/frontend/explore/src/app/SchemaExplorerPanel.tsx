import {useEffect, useMemo, useState, type CSSProperties} from 'react';
import type {DbSchemaNode} from '@sqlrooms/duckdb';
import type {ExploreCatalogDatabaseDto, ExploreTableDto} from './ExploreContext';

interface SchemaExplorerPanelProps {
  schemaTrees: DbSchemaNode[];
  catalogDatabase: ExploreCatalogDatabaseDto;
  activeTable?: ExploreTableDto;
  refreshing: boolean;
  onRefresh: () => Promise<void>;
}

export function SchemaExplorerPanel({
  schemaTrees,
  catalogDatabase,
  activeTable,
  refreshing,
  onRefresh
}: SchemaExplorerPanelProps) {
  const defaultOpenKeys = useMemo(
    () => collectDefaultOpenKeys(schemaTrees, catalogDatabase, activeTable),
    [schemaTrees, catalogDatabase, activeTable]
  );
  const defaultOpenKey = useMemo(() => Array.from(defaultOpenKeys).sort().join('|'), [defaultOpenKeys]);
  const [openKeys, setOpenKeys] = useState<Set<string>>(() => defaultOpenKeys);
  const [menuKey, setMenuKey] = useState<string | null>(null);

  useEffect(() => {
    setOpenKeys((current) => {
      const next = new Set(current);
      let changed = false;
      defaultOpenKeys.forEach((key) => {
        if (!next.has(key)) {
          next.add(key);
          changed = true;
        }
      });
      return changed ? next : current;
    });
  }, [defaultOpenKey, defaultOpenKeys]);

  function toggle(path: string) {
    setOpenKeys((current) => {
      const next = new Set(current);
      if (next.has(path)) {
        next.delete(path);
      } else {
        next.add(path);
      }
      return next;
    });
  }

  return (
    <div className="dp-schema-explorer">
      <header className="dp-schema-explorer__header">
        <h2>SCHEMA EXPLORER</h2>
        <button
          type="button"
          className="dp-schema-explorer__refresh"
          aria-label="Schema Explorer aktualisieren"
          title="Schema Explorer aktualisieren"
          disabled={refreshing}
          onClick={() => void onRefresh()}
        >
          <RefreshIcon />
        </button>
      </header>
      {schemaTrees.length > 0 ? (
        <div className="dp-schema-explorer__tree" role="tree" aria-label="Schema Explorer">
          {schemaTrees.map((node, index) => (
            <SchemaTreeNode
              key={nodePath('', node, index)}
              node={node}
              path={nodePath('', node, index)}
              depth={0}
              openKeys={openKeys}
              menuKey={menuKey}
              catalogDatabase={catalogDatabase}
              activeTable={activeTable}
              onToggle={toggle}
              onMenuToggle={setMenuKey}
            />
          ))}
        </div>
      ) : (
        <p className="dp-schema-explorer__empty">Schema wird geladen</p>
      )}
    </div>
  );
}

function SchemaTreeNode({
  node,
  path,
  depth,
  openKeys,
  menuKey,
  catalogDatabase,
  activeTable,
  onToggle,
  onMenuToggle
}: {
  node: DbSchemaNode;
  path: string;
  depth: number;
  openKeys: Set<string>;
  menuKey: string | null;
  catalogDatabase: ExploreCatalogDatabaseDto;
  activeTable?: ExploreTableDto;
  onToggle: (path: string) => void;
  onMenuToggle: (path: string | null) => void;
}) {
  const object = node.object;
  const children = node.children ?? [];
  const hasChildren = children.length > 0;
  const isOpen = openKeys.has(path);
  const isActiveTable = object.type === 'table' && activeTable?.name === object.name;
  const rowCount = isActiveTable ? activeTable?.rowCountEstimate : undefined;

  return (
    <div className="dp-schema-explorer__branch">
      <div
        className={`dp-schema-explorer__node dp-schema-explorer__node--${object.type}${isActiveTable ? ' is-active' : ''}`}
        role="treeitem"
        aria-expanded={hasChildren ? isOpen : undefined}
        aria-selected={isActiveTable || undefined}
        style={{paddingLeft: `${0.15 + depth * 1.05}rem`} as CSSProperties}
      >
        <button
          type="button"
          className="dp-schema-explorer__toggle"
          aria-label={isOpen ? `${object.name} einklappen` : `${object.name} ausklappen`}
          disabled={!hasChildren}
          onClick={() => onToggle(path)}
        >
          {hasChildren && <ChevronIcon open={isOpen} />}
        </button>
        <NodeIcon type={object.type} />
        <NodeLabel node={node} rowCount={rowCount} />
        {object.type === 'table' && (
          <TableActions
            node={node}
            path={path}
            open={menuKey === path}
            catalogDatabase={catalogDatabase}
            onMenuToggle={onMenuToggle}
          />
        )}
      </div>
      {hasChildren && isOpen && (
        <div role="group">
          {children.map((child, index) => {
            const childPath = nodePath(path, child, index);
            return (
              <SchemaTreeNode
                key={childPath}
                node={child}
                path={childPath}
                depth={depth + 1}
                openKeys={openKeys}
                menuKey={menuKey}
                catalogDatabase={catalogDatabase}
                activeTable={activeTable}
                onToggle={onToggle}
                onMenuToggle={onMenuToggle}
              />
            );
          })}
        </div>
      )}
    </div>
  );
}

function NodeLabel({node, rowCount}: {node: DbSchemaNode; rowCount?: number}) {
  const object = node.object;
  if (object.type === 'column') {
    return (
      <span className="dp-schema-explorer__column">
        <span className={`dp-schema-explorer__type dp-schema-explorer__type--${typeClass(object.columnType)}`}>
          {object.columnType.toLowerCase()}
        </span>
        <span className="dp-schema-explorer__name" title={object.name}>{object.name}</span>
      </span>
    );
  }

  return (
    <span className="dp-schema-explorer__name-wrap">
      <span className="dp-schema-explorer__name" title={object.name}>{object.name}</span>
      {object.type === 'table' && typeof rowCount === 'number' && (
        <span className="dp-schema-explorer__rows">{formatSwissNumber(rowCount)} rows</span>
      )}
    </span>
  );
}

function TableActions({
  node,
  path,
  open,
  catalogDatabase,
  onMenuToggle
}: {
  node: DbSchemaNode;
  path: string;
  open: boolean;
  catalogDatabase: ExploreCatalogDatabaseDto;
  onMenuToggle: (path: string | null) => void;
}) {
  const object = node.object;
  if (object.type !== 'table') {
    return null;
  }
  const tableName = object.name;
  const qualifiedName = `${catalogDatabase.schema}.${tableName}`;

  return (
    <div className="dp-schema-explorer__actions">
      <button
        type="button"
        className="dp-schema-explorer__menu-button"
        aria-label={`Aktionen für ${tableName}`}
        aria-expanded={open}
        onClick={() => onMenuToggle(open ? null : path)}
      >
        <KebabIcon />
      </button>
      {open && (
        <div className="dp-schema-explorer__menu" role="menu">
          <button type="button" role="menuitem" onClick={() => void copyText(tableName)}>
            <CopyIcon /> View-Name kopieren
          </button>
          <button type="button" role="menuitem" onClick={() => void copyText(qualifiedName)}>
            <CopyIcon /> Qualifizierten Namen kopieren
          </button>
          <button type="button" role="menuitem" onClick={() => void copyText(`SELECT * FROM ${qualifiedName};`)}>
            <CopyIcon /> SELECT kopieren
          </button>
        </div>
      )}
    </div>
  );
}

function NodeIcon({type}: {type: DbSchemaNode['object']['type']}) {
  switch (type) {
    case 'database':
      return <DatabaseIcon />;
    case 'schema':
      return <FolderIcon />;
    case 'table':
      return <TableIcon />;
    case 'column':
      return null;
  }
}

function collectDefaultOpenKeys(
  nodes: DbSchemaNode[],
  catalogDatabase: ExploreCatalogDatabaseDto,
  activeTable?: ExploreTableDto
): Set<string> {
  const keys = new Set<string>();
  nodes.forEach((node, index) => collectDefaultOpenKeysForNode(node, nodePath('', node, index), keys, catalogDatabase, activeTable));
  return keys;
}

function collectDefaultOpenKeysForNode(
  node: DbSchemaNode,
  path: string,
  keys: Set<string>,
  catalogDatabase: ExploreCatalogDatabaseDto,
  activeTable?: ExploreTableDto
): boolean {
  const object = node.object;
  const children = node.children ?? [];
  const childContainsActive = children
    .map((child, index) => collectDefaultOpenKeysForNode(child, nodePath(path, child, index), keys, catalogDatabase, activeTable))
    .some(Boolean);
  const isCatalogDatabase = object.type === 'database' && object.name === catalogDatabase.database;
  const isCatalogSchema = object.type === 'schema' && object.name === catalogDatabase.schema;
  const isActiveTable = object.type === 'table' && object.name === activeTable?.name;
  if (node.isInitialOpen || isCatalogDatabase || isCatalogSchema || isActiveTable || childContainsActive) {
    keys.add(path);
  }
  return isActiveTable || childContainsActive;
}

function nodePath(parentPath: string, node: DbSchemaNode, index: number): string {
  const object = node.object;
  const segment = `${object.type}:${node.key || object.name}:${index}`;
  return parentPath ? `${parentPath}/${segment}` : segment;
}

function typeClass(type: string): string {
  const normalized = type.toLowerCase();
  if (normalized.includes('bool')) {
    return 'boolean';
  }
  if (normalized.includes('int') || normalized.includes('double') || normalized.includes('decimal') || normalized.includes('float')) {
    return 'numeric';
  }
  if (normalized.includes('char') || normalized.includes('text') || normalized.includes('string')) {
    return 'text';
  }
  return 'other';
}

async function copyText(text: string): Promise<void> {
  await navigator.clipboard?.writeText(text);
}

function formatSwissNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}

function ChevronIcon({open}: {open: boolean}) {
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <path d={open ? 'M3.2 5.6 8 10.4l4.8-4.8' : 'M5.6 3.2 10.4 8l-4.8 4.8'} />
    </svg>
  );
}

function RefreshIcon() {
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <path d="M8 3a5 5 0 0 1 4.55 2.94l-1.3.56A3.58 3.58 0 0 0 8 4.4a3.6 3.6 0 0 0-3.58 3.28H6.2L3.6 10.3 1 7.68h2A5 5 0 0 1 8 3Zm4.4 2.7L15 8.32h-2A5 5 0 0 1 3.45 10.1l1.3-.58A3.58 3.58 0 0 0 8 11.6a3.6 3.6 0 0 0 3.58-3.28H9.8l2.6-2.62Z" />
    </svg>
  );
}

function DatabaseIcon() {
  return (
    <svg className="dp-schema-explorer__icon dp-schema-explorer__icon--database" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <ellipse cx="8" cy="3.5" rx="5" ry="2.2" />
      <path d="M3 3.5v8.2C3 13 5.24 14 8 14s5-1 5-2.3V3.5" />
      <path d="M3 7c0 1.25 2.24 2.25 5 2.25S13 8.25 13 7" />
      <path d="M3 10.2c0 1.25 2.24 2.25 5 2.25s5-1 5-2.25" />
    </svg>
  );
}

function FolderIcon() {
  return (
    <svg className="dp-schema-explorer__icon dp-schema-explorer__icon--schema" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <path d="M1.5 4.3c0-.7.55-1.25 1.25-1.25h3.2l1.2 1.45h6.1c.7 0 1.25.55 1.25 1.25v6.7c0 .7-.55 1.25-1.25 1.25H2.75c-.7 0-1.25-.55-1.25-1.25Z" />
    </svg>
  );
}

function TableIcon() {
  return (
    <svg className="dp-schema-explorer__icon dp-schema-explorer__icon--table" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <rect x="2" y="2" width="12" height="12" rx="1.2" />
      <path d="M2 6h12M2 10h12M6 2v12M10 2v12" />
    </svg>
  );
}

function KebabIcon() {
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <circle cx="8" cy="3.2" r="1.25" />
      <circle cx="8" cy="8" r="1.25" />
      <circle cx="8" cy="12.8" r="1.25" />
    </svg>
  );
}

function CopyIcon() {
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <path d="M5 1.5h7.5V11H5z" />
      <path d="M3.5 4H2v10.5h8V13" />
    </svg>
  );
}
