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
        <section className="dp-explore-island dp-explore-island--error" aria-labelledby="explore-error-title">
          <h2 id="explore-error-title">Erkunden konnte nicht geladen werden</h2>
          <p>Die Datensatzseite und die Downloads bleiben weiterhin verfügbar.</p>
        </section>
      );
    }

    return this.props.children;
  }
}
