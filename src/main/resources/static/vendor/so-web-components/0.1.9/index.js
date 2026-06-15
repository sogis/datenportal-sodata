import { SoBreadcrumb, SoBreadcrumbItem } from "./components/so-breadcrumb.js";
import { SoHeader } from "./components/so-header.js";
import { SoLeadText } from "./components/so-lead-text.js";
if (!customElements.get("so-header")) {
    customElements.define("so-header", SoHeader);
}
if (!customElements.get("so-breadcrumb")) {
    customElements.define("so-breadcrumb", SoBreadcrumb);
}
if (!customElements.get("so-breadcrumb-item")) {
    customElements.define("so-breadcrumb-item", SoBreadcrumbItem);
}
if (!customElements.get("so-lead-text")) {
    customElements.define("so-lead-text", SoLeadText);
}
export { SoBreadcrumb, SoBreadcrumbItem, SoHeader, SoLeadText };
