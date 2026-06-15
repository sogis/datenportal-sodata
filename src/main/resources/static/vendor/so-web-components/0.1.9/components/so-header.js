const DEFAULT_TOP_NAV = [
    { label: "Regierung", href: "https://so.ch/regierung/" },
    { label: "Gerichte", href: "https://so.ch/gerichte/" },
    { label: "Parlament", href: "https://so.ch/parlament/" },
    { label: "Karriere", href: "https://karriere.so.ch/" },
    { label: "my.so.ch", href: "https://my.so.ch/" }
];
const DEFAULT_SECTION_NAV = [
    { label: "Services", href: "https://so.ch/services/" },
    { label: "Verwaltung", href: "https://so.ch/verwaltung/" }
];
function safeParseNav(json, fallback) {
    if (!json)
        return fallback;
    try {
        const v = JSON.parse(json);
        if (!Array.isArray(v))
            return fallback;
        const items = [];
        for (const it of v) {
            if (!it || typeof it !== "object")
                continue;
            const label = it["label"];
            const href = it["href"];
            if (typeof label === "string" && typeof href === "string") {
                items.push({ label, href });
            }
        }
        return items.length ? items : fallback;
    }
    catch {
        return fallback;
    }
}
function el(tag, cls) {
    const n = document.createElement(tag);
    if (cls)
        n.className = cls;
    return n;
}
function buildNavList(items, options = {}) {
    const ul = el("ul", options.className);
    for (const item of items) {
        const li = el("li");
        const a = el("a");
        a.href = item.href;
        const handled = options.setLinkContent?.(a, item);
        if (!handled) {
            a.textContent = item.label;
        }
        options.setLinkAttributes?.(a, item);
        li.appendChild(a);
        ul.appendChild(li);
    }
    return ul;
}
const LOGO_SVG = `<svg xmlns="http://www.w3.org/2000/svg" id="logo" viewBox="0 0 171.086 15.742" aria-hidden="true" focusable="false">
  <g>
    <path d="m20.424.104h.877l-.595,2.634.016.017L23.658.104h1.156l-3.296,2.951,2.43,3.036h-1.105l-2.242-2.864-.613,2.864h-.887L20.424.104Z" fill="#1d1d1b"></path>
    <path d="m31.676,3.878h-2.428l1.852-2.848.576,2.848Zm-4.827,2.213h.972l.981-1.509h3.006l.324,1.509h.904L31.685.104h-.867s-3.969,5.987-3.969,5.987Z" fill="#1d1d1b"></path>
    <path d="m37.714.104h1.157l1.726,5.017h.018L41.701.104h.833l-1.332,5.987h-1.131l-1.726-4.975h-.016l-1.042,4.975h-.843L37.714.104Z" fill="#1d1d1b"></path>
    <path d="m50.965.858h-1.903l-1.147,5.233h-.886l1.149-5.233h-1.902l.167-.754h4.687s-.165.754-.165.754Z" fill="#1d1d1b"></path>
    <path d="m54.55,3.681c0-1.527.998-2.925,2.496-2.925,1.052,0,1.577.644,1.577,1.793,0,1.305-.891,2.892-2.401,2.892-1.105,0-1.671-.695-1.671-1.76h0Zm-.93-.025c0,1.654.884,2.538,2.567,2.538,2.218,0,3.367-1.87,3.367-3.765,0-1.527-.92-2.429-2.481-2.429-2.242,0-3.453,1.956-3.453,3.656Z" fill="#1d1d1b"></path>
    <path d="m64.057.104h1.157l1.726,5.017h.018l1.087-5.017h.832l-1.332,5.987h-1.13l-1.726-4.975h-.018l-1.043,4.975h-.842s1.271-5.987,1.271-5.987Z" fill="#1d1d1b"></path>
    <path d="m69.661,12.986c.847.367,1.765.551,2.684.551.532,0,1.855-.054,1.855-.846,0-1.359-3.952-.661-3.952-3.824,0-2.242,2.611-3.142,4.834-3.142.828,0,1.987.182,3.181.57l-.699,2.168c-.846-.348-1.654-.533-2.648-.533-.734,0-1.543.166-1.543.827,0,1.065,4.136.993,4.136,3.4,0,2.372-2.005,3.548-4.485,3.584-1.453.019-2.997-.201-4.027-.495l.663-2.261h0Z" fill="#1d1d1b"></path>
    <path d="m87.97,9.971c0,2.17-.974,3.457-2.519,3.457-1.12.018-1.8-.847-1.8-2.133,0-1.526.992-3.254,2.592-3.254,1.288,0,1.728.939,1.728,1.93h0Zm3.419.037c0-2.408-1.728-4.282-4.982-4.282-3.75,0-6.176,2.278-6.176,5.642,0,2.407,1.434,4.374,4.982,4.374,3.583,0,6.176-1.745,6.176-5.735h0Z" fill="#1d1d1b"></path>
    <path d="m95.708,1.737h3.346l-2.868,13.786h-3.345l2.868-13.786h0Z" fill="#1d1d1b"></path>
    <path d="m107.455,9.971c0,2.17-.976,3.457-2.519,3.457-1.12.018-1.802-.847-1.802-2.133,0-1.526.994-3.254,2.593-3.254,1.286,0,1.728.939,1.728,1.93Zm3.418.037c0-2.408-1.728-4.282-4.98-4.282-3.751,0-6.178,2.278-6.178,5.642,0,2.407,1.434,4.374,4.983,4.374,3.583,0,6.174-1.745,6.174-5.735h.001Z" fill="#1d1d1b"></path>
    <path d="m113.372,5.946h1.894l.458-2.059,3.495-.938-.663,2.997h2.28l-.405,2.317h-2.408l-.571,2.665c-.146.716-.273,1.194-.273,1.653,0,.644.421,1.066,1.083,1.066.349,0,.717-.147,1.066-.276l-.366,2.187c-.699.111-1.398.183-2.059.183-1.691,0-3.034-.827-3.034-2.83,0-.478.072-.956.294-1.966l.57-2.684h-1.765l.404-2.317v.002Z" fill="#1d1d1b"></path>
    <path d="m125.247,1.737h3.346l-1.177,5.606h.037c.68-.899,1.783-1.617,3.328-1.617,1.929,0,2.975,1.434,2.975,3.308,0,.643-.053,1.01-.255,2.022l-.882,4.467h-3.419l.918-4.485c.093-.479.222-.955.222-1.453,0-.57-.366-1.14-1.286-1.103-1.397,0-2.225,1.215-2.464,2.628l-.936,4.412h-3.31l2.905-13.786h-.002Z" fill="#1d1d1b"></path>
    <path d="m146.496,13.023c-.165.828-.312,1.636-.405,2.5h-3.069l.258-1.563h-.037c-.956,1.066-1.948,1.782-3.696,1.782-1.929,0-2.975-1.433-2.975-3.308,0-.644.053-1.011.257-2.021l.882-4.468h3.418l-.918,4.485c-.093.478-.222.955-.222,1.452,0,.571.368,1.14,1.286,1.104,1.398,0,2.226-1.213,2.464-2.628l.937-4.412h3.308l-1.489,7.077h.001Z" fill="#1d1d1b"></path>
    <path d="m154.952,5.946l-.368,1.967h.037c.534-1.121,1.599-2.186,2.959-2.186.44,0,.956.054,1.397.238l-.754,2.923c-.423-.333-.9-.405-1.525-.405-1.397,0-2.223,1.215-2.463,2.628l-.937,4.412h-3.31l1.489-7.078c.167-.827.315-1.635.405-2.499h3.07,0Z" fill="#1d1d1b"></path>
    <path d="m161.161,8.445c.166-.827.312-1.635.405-2.499h3.069l-.257,1.563h.035c.956-1.065,1.95-1.782,3.696-1.782,1.93,0,2.977,1.434,2.977,3.308,0,.643-.055,1.01-.257,2.022l-.882,4.467h-3.418l.918-4.485c.094-.479.222-.955.222-1.453,0-.57-.368-1.14-1.288-1.103-1.397,0-2.225,1.215-2.463,2.628l-.939,4.412h-3.307s1.489-7.078,1.489-7.078Z" fill="#1d1d1b"></path>
  </g>
  <g>
    <path d="m2.364.109h13.713s-1.474,6.483-2.234,9.833c-.771,3.393-4.621,5.774-8.159,5.774-3.496,0-6.321-2.402-5.559-5.752C.886,6.613,2.364.109,2.364.109Z" fill="#fff"></path>
    <path d="m4.602,15.073l-.88.375c.613.174,1.274.268,1.961.268,3.017,0,6.261-1.732,7.629-4.347l-.858.362c-1.473,2.159-4.339,3.436-6.771,3.436-.37,0-.73-.036-1.081-.093h0Z" fill="#1d1d1b"></path>
    <path d="m2.364.109s-.682,2.998-1.357,5.972h13.713c.676-2.972,1.357-5.972,1.357-5.972,0,0-13.713,0-13.713,0Z" fill="#e01f26"></path>
  </g>
</svg>`;
export class SoHeader extends HTMLElement {
    constructor() {
        super();
        this.topNav = DEFAULT_TOP_NAV;
        this.sectionNav = DEFAULT_SECTION_NAV;
        this.activeSection = "Services";
        this.logoHref = "https://so.ch";
        this.siteName = "Kanton Solothurn";
        this.root = this.attachShadow({ mode: "open" });
        this.updateFromAttributes();
        this.render();
        this.bindEvents();
    }
    attributeChangedCallback() {
        this.updateFromAttributes();
        this.render();
    }
    updateFromAttributes() {
        this.topNav = safeParseNav(this.getAttribute("top-nav"), DEFAULT_TOP_NAV);
        this.sectionNav = safeParseNav(this.getAttribute("section-nav"), DEFAULT_SECTION_NAV);
        this.activeSection = this.getAttribute("active-section") ?? this.activeSection;
        this.logoHref = this.getAttribute("logo-href") ?? this.logoHref;
        this.siteName = this.getAttribute("site-name") ?? this.siteName;
    }
    bindEvents() {
        this.root.addEventListener("click", (e) => {
            const t = e.target;
            const btn = t?.closest("button[data-action]");
            if (btn?.dataset.action === "toggle-menu") {
                this.toggleMobileMenu();
            }
            const a = t?.closest("a[data-section]");
            if (a) {
                const label = a.dataset.section ?? "";
                this.dispatchEvent(new CustomEvent("so-section-select", {
                    detail: { label, href: a.getAttribute("href") ?? "" },
                    bubbles: true,
                    composed: true
                }));
            }
        });
    }
    toggleMobileMenu() {
        const panel = this.root.querySelector(".mobile-panel-inner");
        const btn = this.root.querySelector('button[data-action="toggle-menu"]');
        if (!panel || !btn)
            return;
        const open = panel.getAttribute("data-open") === "true";
        const nextOpen = !open;
        panel.setAttribute("data-open", nextOpen ? "true" : "false");
        btn.setAttribute("aria-expanded", nextOpen ? "true" : "false");
        btn.setAttribute("data-open", nextOpen ? "true" : "false");
        btn.innerHTML = nextOpen ? closeIcon() : burgerIcon();
    }
    render() {
        this.root.innerHTML = "";
        const styleEl = document.createElement("style");
        styleEl.textContent = this.styles();
        this.root.appendChild(styleEl);
        const header = el("header", "so-header");
        header.setAttribute("role", "banner");
        // Row 1: full-width, internal container for gutters
        const top = el("div", "topbar");
        const topInner = el("div", "topbar-inner so-header-container");
        //topInner.style.setProperty("--so-container-size", "var(--so-container-size-full)");
        const left = el("div", "left");
        const logoLink = el("a", "logo");
        logoLink.href = this.logoHref;
        logoLink.setAttribute("aria-label", this.siteName);
        logoLink.title = this.siteName;
        logoLink.innerHTML = LOGO_SVG;
        left.appendChild(logoLink);
        const right = el("div", "right");
        const nav = el("nav", "topnav");
        nav.setAttribute("aria-label", "Hauptnavigation");
        const ul = buildNavList(this.topNav, {
            setLinkContent: (a, item) => {
                if (item.label === "my.so.ch") {
                    const label = el("span");
                    label.textContent = item.label;
                    a.append(label);
                    a.insertAdjacentHTML("beforeend", ` <svg class="icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><title>my.so.ch</title><path d="M20.39 18.71c1.48-1.84 2.36-4.17 2.36-6.71 0-5.93-4.82-10.75-10.75-10.75S1.25 6.07 1.25 12 6.07 22.75 12 22.75c3.39 0 6.41-1.58 8.38-4.04ZM12 2.75c5.1 0 9.25 4.15 9.25 9.25 0 1.93-.6 3.72-1.61 5.21-1.08-.92-3.49-2.46-7.64-2.46s-6.56 1.54-7.64 2.46A9.156 9.156 0 0 1 2.75 12c0-5.1 4.15-9.25 9.25-9.25Zm0 18.5c-2.63 0-5.01-1.11-6.69-2.88.84-.74 2.93-2.12 6.69-2.12s5.85 1.38 6.69 2.12C17 20.14 14.63 21.25 12 21.25Z"/><path d="M12 12.75c2.07 0 3.75-1.68 3.75-3.75S14.07 5.25 12 5.25 8.25 6.93 8.25 9s1.68 3.75 3.75 3.75Zm0-6c1.24 0 2.25 1.01 2.25 2.25s-1.01 2.25-2.25 2.25S9.75 10.24 9.75 9 10.76 6.75 12 6.75Z"/></svg>`);
                    return true;
                }
                return false;
            }
        });
        nav.appendChild(ul);
        const actions = el("div", "actions");
        const btnMenu = el("button", "iconbtn menubtn");
        btnMenu.type = "button";
        btnMenu.dataset.action = "toggle-menu";
        btnMenu.setAttribute("aria-label", "Menü");
        btnMenu.setAttribute("aria-expanded", "false");
        btnMenu.setAttribute("data-open", "false");
        btnMenu.innerHTML = burgerIcon();
        actions.append(btnMenu);
        right.append(nav, actions);
        topInner.append(left, right);
        top.appendChild(topInner);
        // Row 2: section nav
        const second = el("div", "secondbar");
        const secondInner = el("div", "secondbar-inner so-header-container");
        //secondInner.style.setProperty("--so-container-size", "var(--so-container-size-full)");
        const sectionNav = el("nav", "sectionnav");
        sectionNav.setAttribute("aria-label", "Bereiche");
        const sul = buildNavList(this.sectionNav, {
            setLinkAttributes: (a, item) => {
                a.dataset.section = item.label;
                if (item.label === this.activeSection)
                    a.setAttribute("aria-current", "page");
            }
        });
        sectionNav.appendChild(sul);
        secondInner.appendChild(sectionNav);
        second.appendChild(secondInner);
        // Mobile panel (drawer-ish)
        const mobilePanelWrap = el("div", "mobile-panel");
        const mobilePanel = el("div", "mobile-panel-inner so-container");
        //mobilePanel.style.setProperty("--so-container-size", "var(--so-container-size-full)");
        mobilePanel.setAttribute("data-open", "false");
        const mpTop = el("div", "mobile-group");
        mpTop.appendChild(el("div", "mobile-title")).textContent = "Navigation";
        const mpUl = buildNavList(this.topNav, { className: "mobile-list" });
        mpTop.appendChild(mpUl);
        const mpSections = el("div", "mobile-group");
        mpSections.appendChild(el("div", "mobile-title")).textContent = "Bereiche";
        const msUl = buildNavList(this.sectionNav, { className: "mobile-list" });
        mpSections.appendChild(msUl);
        mobilePanel.append(mpTop, mpSections);
        mobilePanelWrap.appendChild(mobilePanel);
        header.append(top, second, mobilePanelWrap);
        this.root.appendChild(header);
    }
    styles() {
        return (`
      :host{
        display: block;
        background: var(--so-bg, #fff);
        color: var(--so-fg, rgb(47, 72, 88));
        font-family: var(--so-font-family, Frutiger, sans-serif);
        font-size: var(--so-font-size, 16px);
        line-height: var(--so-line-height, 1.5);
      }

      .so-header{
        background: var(--so-bg, #fff);
        color: var(--so-fg, rgb(47, 72, 88));
        border-bottom: 1px solid var(--so-border-color);
      }

      .so-container{
        width: 100%;
        max-width: var(--so-container-size, var(--so-container-size-full, 120rem));
        margin-inline: auto;
        padding-inline: var(--so-container-padding, var(--so-space-6, 1.5rem));
        box-sizing: border-box;
      }

      .so-header-container{
        width: 100%;
        margin-inline: auto;
        padding-top: var(--so-container-padding, var(--so-space-6, 1.5rem));
        padding-inline: var(--so-container-padding, var(--so-space-6, 1.5rem));
        box-sizing: border-box;
      }

      .topbar-inner{
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1.25rem;
      }

      .logo{
        display: inline-flex;
        align-items: center;
        text-decoration: none;
      }
      .logo svg#logo{
        width: 10.125rem;
        height: 1rem;
        /*width: 10.7rem;*/ /* close to screenshot */
        height: auto;
        display: block;
      }

      /* Top links */
      .right{ display: flex; align-items: center; gap: 1rem; }
      .topnav ul{
        list-style: none;
        display: flex;
        gap: 2.5rem;
        margin: 0;
        padding: 0;
        align-items: center;
        white-space: nowrap;
      }
      .topnav a{
        text-decoration: none;
        font-weight: 900;
        color: var(--so-fg, rgb(47, 72, 88));
        display: inline-flex;
        align-items: center;
        gap: 0.4rem;
        line-height: 1;
      }
      .topnav a span{
        line-height: 1;
      }
      .topnav .icon{
        width: 1.5rem;
        height: 1.5rem;
        fill: currentColor;
        display: block;
        align-self: center;
        transform: translateY(-2px);
      }
      .topnav a:hover{ color: rgb(204, 0, 0); }

      .actions{ display: flex; align-items: center; }
      .iconbtn{
        border: 1px solid var(--so-border-color);
        background: transparent;
        border-radius: 9999px;
        width: 2.75rem;
        height: 2.75rem;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: rgba(0,0,0,0.8);
      }
      .iconbtn:hover{ background: rgba(0,0,0,0.04); }
      .menubtn{
        display: none;
        color: rgb(204, 0, 0);
      }
      .menubtn[data-open="true"]{
        background: rgb(204, 0, 0);
        border-color: rgb(204, 0, 0);
        color: #fff;
      }
      .menubtn[data-open="true"]:hover{
        background: rgb(204, 0, 0);
      }

      /* Second row */
      .secondbar{
        width: 100%;
        /*margin-block: 0.4rem;*/
      }

      .sectionnav ul{
        list-style: none;
        display: flex;
        gap: 1.5rem;
        margin: 0;
        padding: 0;
        padding-top: var(--so-space-2, 0.5rem);
        align-items: baseline;
        /*border-bottom: 1px solid rgba(0,0,0,0.08);*/
      }
      .sectionnav a{
        text-decoration: none;
        font-weight: 900;
        font-size: 1.5rem;
        letter-spacing: -0.01em;
        color: var(--so-fg, rgb(47, 72, 88));
        padding-bottom: 0.45rem;
        border-bottom: 1px solid transparent;
        margin-bottom: -1px;
        display: inline-block;
      }
      .sectionnav a:hover{
        color: rgb(204, 0, 0);
      }
      .sectionnav a[aria-current="page"]{
        color: rgb(204, 0, 0);
        border-bottom: 1px solid rgb(204, 0, 0);
      }

      /* Responsive: hide links + show menu */
      @media (max-width: 992px){
        .topnav ul{ gap: 2rem; }
      }
      @media (max-width: 768px){
        .topnav{ display: none; }
        .sectionnav a{ font-size: 1.35rem; }
        .menubtn{ display: inline-flex; }
      }

      /* Mobile panel */
      .mobile-panel{ display: none; border-top: 1px solid var(--so-border-color); }
      .mobile-panel-inner{ padding-block: 0.75rem 1.25rem; }
      @media (max-width: 768px){
        .mobile-panel{
          display: block;
          border: 0;
        }
        .mobile-panel-inner[data-open="false"]{ display: none; }
      }

      .mobile-group{ margin-top: 0.75rem; }
      .mobile-title{ font-weight: 900; margin-bottom: 0.5rem; }

      .mobile-list{
        list-style: none;
        margin: 0;
        padding: 0;
        display: grid;
        gap: 0;

        border-top: 1px solid var(--so-border-color);
        border-bottom: 1px solid var(--so-border-color);
        /*border-radius: 0.6rem;*/
        overflow: hidden; /* clips inner items to rounded corners */
      }

      .mobile-list a{
        display: block;
        padding: 0.65rem 0.75rem;
        text-decoration: none;
        font-size: 1rem;

        border: 0; /* remove per-item border */
        border-radius: 0; /* only the container rounds */
        background: rgba(255,255,255,0.75);
        color: var(--so-fg, rgb(47, 72, 88));
        font-weight: 400;
      }

      /* add separators (1px) between items */
      .mobile-list li + li a{
        border-top: 1px solid var(--so-border-color);
      }

      .mobile-list a:hover{
        color: rgb(204, 0, 0);
        background: rgba(255,255,255,0.95);
      }
    `).replace(/^[\t ]+/gm, "").trim() + "\n";
    }
}
SoHeader.observedAttributes = ["top-nav", "section-nav", "active-section", "logo-href", "site-name"];
function burgerIcon() {
    return `
  <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path fill="currentColor" d="M4 7h16v2H4V7zm0 6h16v2H4v-2zm0 6h16v2H4v-2z"/>
  </svg>`;
}
function closeIcon() {
    return `
  <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path fill="currentColor" d="M18.3 5.71 12 12l6.3 6.29-1.41 1.42L10.59 13.4l-6.3 6.31-1.41-1.42L9.17 12 2.88 5.71l1.41-1.42 6.3 6.31 6.3-6.31 1.41 1.42Z"/>
  </svg>`;
}
