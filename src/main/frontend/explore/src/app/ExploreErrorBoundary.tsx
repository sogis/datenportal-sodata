import {Component, type ErrorInfo, type ReactNode} from 'react';

interface ExploreErrorBoundaryProps {
  children: ReactNode;
}

interface ExploreErrorBoundaryState {
  error?: Error;
}

export class ExploreErrorBoundary extends Component<ExploreErrorBoundaryProps, ExploreErrorBoundaryState> {
  override state: ExploreErrorBoundaryState = {};

  static getDerivedStateFromError(error: Error): ExploreErrorBoundaryState {
    return {error};
  }

  override componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('Explore island failed to render.', error, errorInfo);
  }

  override render(): ReactNode {
    if (this.state.error) {
      return (
        <section className="dp-explore-workbench dp-explore-workbench--unavailable" aria-labelledby="explore-error-title">
          <div className="dp-explore-empty-state">
            <p className="dp-explore-kicker">Erkunden</p>
            <h1 id="explore-error-title">Erkunden konnte nicht geladen werden</h1>
            <p>Die Datensatzseite und die Downloads bleiben weiterhin verfügbar.</p>
          </div>
        </section>
      );
    }

    return this.props.children;
  }
}
