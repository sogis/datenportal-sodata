import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import {ExploreApp} from './app/ExploreApp';
import {ExploreErrorBoundary} from './app/ExploreErrorBoundary';
import {loadEmbeddedExploreContext} from './app/ExploreContextLoader';
import {configureBundledMonaco} from './monaco/configureBundledMonaco';
import './styles/explore.css';

export const EXPLORE_ROOT_ELEMENT_ID = 'datenportal-explore-root';

export function bootstrapExploreApp(documentRef: Document = document): void {
  const rootElement = documentRef.getElementById(EXPLORE_ROOT_ELEMENT_ID);
  if (!rootElement) {
    return;
  }

  try {
    const context = loadEmbeddedExploreContext(documentRef);
    createRoot(rootElement).render(
      <StrictMode>
        <ExploreErrorBoundary>
          <ExploreApp context={context} />
        </ExploreErrorBoundary>
      </StrictMode>
    );
  } catch (error) {
    renderBootstrapError(rootElement, error);
  }
}

function renderBootstrapError(rootElement: HTMLElement, error: unknown): void {
  console.error('Explore island bootstrap failed.', error);
  rootElement.innerHTML = `
    <section class="dp-explore-workbench dp-explore-workbench--unavailable" aria-labelledby="explore-bootstrap-error-title">
      <div class="dp-explore-empty-state">
        <p class="dp-explore-kicker">Erkunden</p>
        <h1 id="explore-bootstrap-error-title">Erkunden konnte nicht geladen werden</h1>
        <p>Der Datenkontext konnte nicht gelesen werden. Die Datensatzseite und die Downloads bleiben weiterhin verfügbar.</p>
      </div>
    </section>
  `;
}

if (typeof document !== 'undefined' && !import.meta.env.VITEST) {
  configureBundledMonaco();
  bootstrapExploreApp();
}
