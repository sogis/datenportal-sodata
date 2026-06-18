(() => {
  const doc = document;
  const root = doc.documentElement;
  const body = doc.body;

  root.classList.add("dp-js");

  const popoverHost = () => doc.getElementById("filter-popover-host");
  const sheetHost = () => doc.getElementById("mobile-filter-panel-host");
  const searchForm = () => doc.getElementById("catalog-search-form");
  const searchInput = () => doc.getElementById("catalog-search");
  const searchClearButton = () => doc.getElementById("catalog-search-clear");
  const triggerSelector = "[data-filter-trigger], [data-mobile-filter-trigger]";
  const SEARCH_DELAY_MS = 300;

  const state = {
    activeTrigger: null,
    activeKind: null,
    pendingTrigger: null,
    pendingKind: null,
    searchTimer: null,
    submittedSearchQuery: normalizedSearchValue(searchInput()?.value ?? ""),
  };

  function hosts() {
    return {
      popover: popoverHost(),
      sheet: sheetHost(),
    };
  }

  function setExpanded(trigger, expanded) {
    if (trigger) {
      trigger.setAttribute("aria-expanded", String(expanded));
    }
  }

  function normalizedSearchValue(value) {
    const trimmed = value.trim();
    return trimmed.length >= 3 ? trimmed : "";
  }

  function clearPendingSearch() {
    if (state.searchTimer !== null) {
      window.clearTimeout(state.searchTimer);
      state.searchTimer = null;
    }
  }

  function submitSearch(immediate = false) {
    const form = searchForm();
    const input = searchInput();
    if (!form || !input) {
      return;
    }

    const effectiveQuery = normalizedSearchValue(input.value);
    const dispatch = () => {
      state.searchTimer = null;
      state.submittedSearchQuery = effectiveQuery;
      if (window.htmx) {
        window.htmx.trigger(form, "submit");
        return;
      }
      form.requestSubmit();
    };

    clearPendingSearch();
    if (immediate) {
      dispatch();
      return;
    }

    state.searchTimer = window.setTimeout(dispatch, SEARCH_DELAY_MS);
  }

  function scheduleSearch() {
    const input = searchInput();
    if (!input) {
      return;
    }

    const effectiveQuery = normalizedSearchValue(input.value);
    if (effectiveQuery === state.submittedSearchQuery) {
      clearPendingSearch();
      return;
    }

    submitSearch(false);
  }

  function clearExpanded() {
    doc.querySelectorAll(triggerSelector).forEach((trigger) => setExpanded(trigger, false));
  }

  function focusFirst(host) {
    const candidate = host?.querySelector(
      "input:not([type='hidden']):not([disabled]), button:not([disabled]), select:not([disabled]), a[href]"
    );
    candidate?.focus();
  }

  function positionPopover() {
    const host = popoverHost();
    const panel = host?.querySelector("[data-filter-panel]");
    if (!host || !panel || !state.activeTrigger) {
      return;
    }

    const triggerRect = state.activeTrigger.getBoundingClientRect();
    const maxWidth = Math.min(360, window.innerWidth - 24);
    const minWidth = Math.min(triggerRect.width, maxWidth);
    panel.style.minWidth = `${minWidth}px`;
    panel.style.maxWidth = `${maxWidth}px`;

    const panelRect = panel.getBoundingClientRect();
    const left = Math.max(
      12,
      Math.min(triggerRect.left, window.innerWidth - panelRect.width - 12)
    );
    const top = Math.min(triggerRect.bottom + 8, window.innerHeight - panelRect.height - 12);

    host.style.setProperty("--dp-popover-left", `${left}px`);
    host.style.setProperty("--dp-popover-top", `${top}px`);
  }

  function closeKind(kind, restoreFocus = true) {
    const host = hosts()[kind];
    if (!host) {
      return;
    }

    host.innerHTML = "";
    host.hidden = true;
    host.removeAttribute("data-open");
    if (kind === "sheet") {
      body.classList.remove("dp-filter-sheet-open");
    }

    if (state.activeKind === kind) {
      setExpanded(state.activeTrigger, false);
      const trigger = state.activeTrigger;
      state.activeTrigger = null;
      state.activeKind = null;
      if (restoreFocus) {
        trigger?.focus();
      }
    }
  }

  function closeAll(restoreFocus = true) {
    closeKind("popover", restoreFocus);
    closeKind("sheet", restoreFocus);
    clearExpanded();
  }

  function openKind(kind) {
    const host = hosts()[kind];
    if (!host) {
      return;
    }

    if (kind === "popover") {
      closeKind("sheet", false);
    } else {
      closeKind("popover", false);
    }
    clearExpanded();

    state.activeTrigger = state.pendingTrigger;
    state.activeKind = state.pendingKind ?? kind;
    state.pendingTrigger = null;
    state.pendingKind = null;

    host.hidden = false;
    host.dataset.open = "true";
    setExpanded(state.activeTrigger, true);

    if (kind === "sheet") {
      body.classList.add("dp-filter-sheet-open");
    } else {
      positionPopover();
    }

    focusFirst(host);
  }

  function triggerForEvent(event) {
    return event.target instanceof Element ? event.target.closest(triggerSelector) : null;
  }

  doc.addEventListener(
    "click",
    (event) => {
      const trigger = triggerForEvent(event);
      if (trigger) {
        const kind = trigger.hasAttribute("data-mobile-filter-trigger") ? "sheet" : "popover";
        const host = hosts()[kind];
        const alreadyOpen =
          state.activeTrigger === trigger &&
          state.activeKind === kind &&
          host &&
          !host.hidden &&
          host.childElementCount > 0;

        if (alreadyOpen) {
          event.preventDefault();
          closeKind(kind);
          return;
        }

        state.pendingTrigger = trigger;
        state.pendingKind = kind;
        return;
      }

      const closeButton = event.target instanceof Element ? event.target.closest("[data-filter-close]") : null;
      if (closeButton) {
        event.preventDefault();
        closeAll(true);
        return;
      }

      const popHost = popoverHost();
      const popPanel = popHost?.querySelector("[data-filter-panel]");
      if (
        state.activeKind === "popover" &&
        popHost &&
        !popHost.hidden &&
        popPanel &&
        event.target instanceof Node &&
        !popPanel.contains(event.target)
      ) {
        closeKind("popover", true);
      }
    },
    true
  );

  doc.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && state.activeKind) {
      event.preventDefault();
      closeAll(true);
    }
  });

  searchInput()?.addEventListener("input", () => {
    scheduleSearch();
  });

  searchClearButton()?.addEventListener("click", () => {
    const input = searchInput();
    if (!input) {
      return;
    }
    input.value = "";
    submitSearch(true);
    input.focus();
  });

  doc.body.addEventListener("htmx:beforeRequest", (event) => {
    const trigger = triggerForEvent(event);
    if (trigger) {
      state.pendingTrigger = trigger;
      state.pendingKind = trigger.hasAttribute("data-mobile-filter-trigger") ? "sheet" : "popover";
    }
  });

  doc.body.addEventListener("htmx:afterSwap", (event) => {
    const target = event.detail.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }

    if (target.id === "filter-popover-host") {
      openKind("popover");
      return;
    }

    if (target.id === "mobile-filter-panel-host") {
      openKind("sheet");
      return;
    }

    if (target.id === "dataset-results-shell") {
      closeAll(false);
    }
  });

  window.addEventListener("resize", positionPopover);
  window.addEventListener("scroll", positionPopover, true);
})();
