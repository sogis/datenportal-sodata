const ALLOWED_START_PATTERN = /^(select|with|describe|show|pragma\s+table_info)\b/i;
const BLOCKED_PATTERN =
  /\b(insert|update|delete|drop|alter|attach|install|load|call|set)\b|\bcreate\s+table\b|\bcopy\b[\s\S]*\bto\b/i;

export function isReadOnlyQuery(sql: string): boolean {
  const statements = splitSqlStatements(sql);
  if (statements.length !== 1) {
    return false;
  }
  const statement = statements[0]?.trim() ?? '';
  return ALLOWED_START_PATTERN.test(statement) && !BLOCKED_PATTERN.test(statement);
}

export function applyResultLimit(sql: string, maxRows: number): string {
  const statement = stripTrailingSemicolon(sql.trim());
  if (!isSelectLike(statement) || hasTopLevelLimit(statement)) {
    return statement;
  }
  return `select *
from (
  ${statement}
) as q
limit ${maxRows}`;
}

export function normalizeSqlForExecution(sql: string, maxRows: number): string {
  const statements = splitSqlStatements(sql);
  if (statements.length !== 1) {
    throw new Error('Bitte genau eine SQL-Anweisung ausführen.');
  }
  const statement = statements[0]?.trim() ?? '';
  if (!isReadOnlyQuery(statement)) {
    throw new Error('Diese Abfrage ist im lokalen SQL-Labor nicht erlaubt.');
  }
  return applyResultLimit(statement, maxRows);
}

function stripTrailingSemicolon(sql: string): string {
  return sql.replace(/;\s*$/, '');
}

function isSelectLike(sql: string): boolean {
  return /^(select|with)\b/i.test(sql);
}

function hasTopLevelLimit(sql: string): boolean {
  let depth = 0;
  let quote: "'" | '"' | null = null;

  for (let index = 0; index < sql.length; index++) {
    const char = sql[index];
    const next = sql[index + 1];

    if (quote) {
      if (char === quote && next === quote) {
        index++;
      } else if (char === quote) {
        quote = null;
      }
      continue;
    }

    if (char === "'" || char === '"') {
      quote = char;
      continue;
    }
    if (char === '(') {
      depth++;
      continue;
    }
    if (char === ')') {
      depth = Math.max(0, depth - 1);
      continue;
    }
    if (depth === 0 && /^limit\b/i.test(sql.slice(index))) {
      return true;
    }
  }

  return false;
}

function splitSqlStatements(input: string): string[] {
  const statements: string[] = [];
  let current = '';
  let quote: "'" | '"' | null = null;
  let lineComment = false;
  let blockComment = false;

  for (let index = 0; index < input.length; index++) {
    const char = input[index];
    const next = input[index + 1];

    if (lineComment) {
      if (char === '\n') {
        lineComment = false;
        current += char;
      }
      continue;
    }

    if (blockComment) {
      if (char === '*' && next === '/') {
        blockComment = false;
        index++;
      }
      continue;
    }

    if (quote) {
      current += char;
      if (char === quote && next === quote) {
        current += next;
        index++;
      } else if (char === quote) {
        quote = null;
      }
      continue;
    }

    if (char === '-' && next === '-') {
      lineComment = true;
      index++;
      continue;
    }
    if (char === '/' && next === '*') {
      blockComment = true;
      index++;
      continue;
    }
    if (char === "'" || char === '"') {
      quote = char;
      current += char;
      continue;
    }
    if (char === ';') {
      const statement = current.trim();
      if (statement) {
        statements.push(statement);
      }
      current = '';
      continue;
    }
    current += char;
  }

  const lastStatement = current.trim();
  if (lastStatement) {
    statements.push(lastStatement);
  }
  return statements;
}
