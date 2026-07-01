import {ZodError} from 'zod';
import {type ExploreContextDto, parseExploreContext} from './ExploreContext';

export const EXPLORE_CONTEXT_ELEMENT_ID = 'datenportal-explore-context';

export class ExploreContextLoadError extends Error {
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = 'ExploreContextLoadError';
  }
}

export function loadEmbeddedExploreContext(documentRef: Document = document): ExploreContextDto {
  const element = documentRef.getElementById(EXPLORE_CONTEXT_ELEMENT_ID);
  if (!element) {
    throw new ExploreContextLoadError('Der Erkunden-Kontext wurde nicht gefunden.');
  }

  const rawJson = element.textContent?.trim();
  if (!rawJson) {
    throw new ExploreContextLoadError('Der Erkunden-Kontext ist leer.');
  }

  try {
    return parseExploreContext(rawJson);
  } catch (error) {
    if (error instanceof SyntaxError) {
      throw new ExploreContextLoadError('Der Erkunden-Kontext ist kein gültiges JSON.', {cause: error});
    }
    if (error instanceof ZodError) {
      throw new ExploreContextLoadError('Der Erkunden-Kontext hat ein unerwartetes Format.', {cause: error});
    }
    throw error;
  }
}
