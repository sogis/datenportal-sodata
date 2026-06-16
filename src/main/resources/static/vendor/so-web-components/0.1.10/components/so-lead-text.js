export class SoLeadText extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }
    connectedCallback() {
        this.render();
    }
    render() {
        if (!this.shadowRoot)
            return;
        this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
          margin-top: var(--so-lead-text-margin-top, 24px);
          color: inherit;
        }

        ::slotted(p) {
          margin: 0 0 18px;
          font-size: var(--so-lead-text-font-size, 1.125rem);
          line-height: var(--so-lead-text-line-height, 1.55);
        }

        ::slotted(p:last-child) {
          margin-bottom: 0;
        }
      </style>

      <slot></slot>
    `;
    }
}
