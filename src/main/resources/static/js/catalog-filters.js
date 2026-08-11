(() => {
  const doc = document;
  const root = doc.documentElement;
  const body = doc.body;

  root.classList.add("dp-js");

  const sheetHost = () => doc.getElementById("mobile-filter-panel-host");
  const searchForm = () => doc.getElementById("catalog-search-form");
  const searchInput = () => doc.getElementById("catalog-search");
  const searchClearButton = () => doc.getElementById("catalog-search-clear");
  const triggerSelector = "[data-filter-trigger], [data-mobile-filter-trigger]";
  const SEARCH_DELAY_MS = 300;
  const COPY_FEEDBACK_MS = 2200;
  const copyTimers = new WeakMap();

  const state = {
    activeKind: null,
    activePanelHost: null,
    activeTrigger: null,
    pendingKind: null,
    pendingTrigger: null,
    searchTimer: null,
    submittedSearchQuery: normalizedSearchValue(searchInput()?.value ?? ""),
  };

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

  function focusFirst(container) {
    const candidate = container?.querySelector(
      "input:not([type='hidden']):not([disabled]), button:not([disabled]), select:not([disabled]), a[href]"
    );
    candidate?.focus();
  }

  function triggerForEvent(event) {
    return event.target instanceof Element ? event.target.closest(triggerSelector) : null;
  }

  function dropdownForTrigger(trigger) {
    return trigger?.closest("[data-filter-dropdown]") ?? null;
  }

  function panelHostForTrigger(trigger) {
    return dropdownForTrigger(trigger)?.querySelector("[data-filter-panel-host]") ?? null;
  }

  function isDesktopOpen(trigger, host) {
    return (
      state.activeKind === "popover" &&
      state.activeTrigger === trigger &&
      host &&
      !host.hidden &&
      host.childElementCount > 0
    );
  }

  function isSheetOpen(trigger, host) {
    return (
      state.activeKind === "sheet" &&
      state.activeTrigger === trigger &&
      host &&
      !host.hidden &&
      host.childElementCount > 0
    );
  }

  function closeDesktop(restoreFocus = true) {
    if (state.activeKind !== "popover") {
      return;
    }

    const trigger = state.activeTrigger;
    const dropdown = dropdownForTrigger(trigger);
    const host = state.activePanelHost;

    if (host) {
      host.innerHTML = "";
      host.hidden = true;
    }
    dropdown?.removeAttribute("data-open");
    setExpanded(trigger, false);

    state.activeKind = null;
    state.activePanelHost = null;
    state.activeTrigger = null;

    if (restoreFocus) {
      trigger?.focus();
    }
  }

  function closeSheet(restoreFocus = true) {
    if (state.activeKind !== "sheet") {
      return;
    }

    const trigger = state.activeTrigger;
    const host = sheetHost();

    if (host) {
      host.innerHTML = "";
      host.hidden = true;
      host.removeAttribute("data-open");
    }
    body.classList.remove("dp-filter-sheet-open");
    setExpanded(trigger, false);

    state.activeKind = null;
    state.activePanelHost = null;
    state.activeTrigger = null;

    if (restoreFocus) {
      trigger?.focus();
    }
  }

  function closeAll(restoreFocus = true) {
    closeDesktop(restoreFocus);
    closeSheet(restoreFocus);
  }

  function openDesktop(host) {
    const trigger = state.pendingTrigger;
    if (!host || !trigger) {
      return;
    }

    closeSheet(false);
    closeDesktop(false);

    const dropdown = dropdownForTrigger(trigger);
    host.hidden = false;
    dropdown?.setAttribute("data-open", "true");
    setExpanded(trigger, true);

    state.activeKind = "popover";
    state.activePanelHost = host;
    state.activeTrigger = trigger;
    state.pendingKind = null;
    state.pendingTrigger = null;

    focusFirst(host);
  }

  function openSheet() {
    const host = sheetHost();
    const trigger = state.pendingTrigger;
    if (!host || !trigger) {
      return;
    }

    closeDesktop(false);
    closeSheet(false);

    host.hidden = false;
    host.dataset.open = "true";
    body.classList.add("dp-filter-sheet-open");
    setExpanded(trigger, true);

    state.activeKind = "sheet";
    state.activePanelHost = host;
    state.activeTrigger = trigger;
    state.pendingKind = null;
    state.pendingTrigger = null;

    focusFirst(host);
  }

  function resetPanelInputs(resetButton) {
    const panel = resetButton.closest("[data-filter-panel]");
    if (!panel) {
      return;
    }

    panel.querySelectorAll("input[type='checkbox']").forEach((checkbox) => {
      checkbox.checked = false;
    });

    const radioButtons = [...panel.querySelectorAll("input[type='radio']")];
    if (radioButtons.length > 0) {
      const emptyOption = radioButtons.find((radio) => radio.value === "");
      radioButtons.forEach((radio) => {
        radio.checked = radio === emptyOption;
      });
    }
  }

  doc.addEventListener(
    "click",
    (event) => {
      const trigger = triggerForEvent(event);
      if (trigger) {
        const kind = trigger.hasAttribute("data-mobile-filter-trigger") ? "sheet" : "popover";
        const host = kind === "sheet" ? sheetHost() : panelHostForTrigger(trigger);
        const alreadyOpen = kind === "sheet" ? isSheetOpen(trigger, host) : isDesktopOpen(trigger, host);

        if (alreadyOpen) {
          event.preventDefault();
          if (kind === "sheet") {
            closeSheet(true);
          } else {
            closeDesktop(true);
          }
          return;
        }

        state.pendingKind = kind;
        state.pendingTrigger = trigger;
        return;
      }

      const closeButton = event.target instanceof Element ? event.target.closest("[data-filter-close]") : null;
      if (closeButton) {
        event.preventDefault();
        closeAll(true);
        return;
      }

      const resetButton = event.target instanceof Element ? event.target.closest("[data-filter-reset]") : null;
      if (resetButton) {
        event.preventDefault();
        resetPanelInputs(resetButton);
        return;
      }

      if (
        state.activeKind === "popover" &&
        event.target instanceof Node &&
        state.activeTrigger
      ) {
        const dropdown = dropdownForTrigger(state.activeTrigger);
        if (dropdown && !dropdown.contains(event.target)) {
          closeDesktop(true);
        }
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

  function rowForEvent(event) {
    const target = event.target instanceof Element ? event.target : null;
    if (!target || target.closest("a, button, input, select, textarea, label, option, form, [role='button'], [data-no-row-toggle]")) {
      return null;
    }
    return target.closest("tr[data-series-expand-href]");
  }

  doc.addEventListener("click", (event) => {
    const row = rowForEvent(event);
    if (!(row instanceof HTMLElement)) {
      return;
    }

    const expandButton = row.querySelector(".dp-expand-button");
    if (!(expandButton instanceof HTMLElement)) {
      return;
    }

    event.preventDefault();
    expandButton.click();
  });

  doc.body.addEventListener("htmx:beforeRequest", (event) => {
    const trigger = triggerForEvent(event);
    if (!trigger) {
      return;
    }

    state.pendingKind = trigger.hasAttribute("data-mobile-filter-trigger") ? "sheet" : "popover";
    state.pendingTrigger = trigger;
  });

  doc.body.addEventListener("htmx:afterSwap", (event) => {
    const target = event.detail.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }

    if (target.matches("[data-filter-panel-host]")) {
      openDesktop(target);
      return;
    }

    if (target.id === "mobile-filter-panel-host") {
      openSheet();
      return;
    }

    if (target.id === "dataset-results-shell") {
      closeAll(false);
    }
  });

  doc.addEventListener("click", async (event) => {
    const copyButton = event.target instanceof Element ? event.target.closest("[data-copy-value]") : null;
    if (copyButton instanceof HTMLElement) {
      event.preventDefault();
      const value = copyButton.dataset.copyValue ?? "";
      if (!value) {
        return;
      }
      try {
        await navigator.clipboard.writeText(value);
        if (copyTimers.has(copyButton)) {
          window.clearTimeout(copyTimers.get(copyButton));
        }
        copyButton.dataset.copyState = "copied";
        copyButton.setAttribute("aria-label", copyButton.dataset.copySuccessLabel || "Kopiert");
        const timer = window.setTimeout(() => {
          delete copyButton.dataset.copyState;
          copyButton.setAttribute("aria-label", copyButton.dataset.copyLabel || "Kopieren");
          copyTimers.delete(copyButton);
        }, COPY_FEEDBACK_MS);
        copyTimers.set(copyButton, timer);
      } catch {
        copyButton.dataset.copyState = "failed";
      }
      return;
    }

    const tab = event.target instanceof Element ? event.target.closest("[data-usage-tab]") : null;
    if (!(tab instanceof HTMLElement)) {
      return;
    }

    event.preventDefault();
    const tabId = tab.dataset.usageTab;
    const section = tab.closest(".dp-usage-code");
    if (!tabId || !section) {
      return;
    }

    section.querySelectorAll("[data-usage-tab]").forEach((candidate) => {
      const selected = candidate === tab;
      candidate.classList.toggle("dp-usage-tab--active", selected);
      candidate.setAttribute("aria-selected", String(selected));
    });

    section.querySelectorAll("[data-usage-panel]").forEach((panel) => {
      if (panel instanceof HTMLElement) {
        panel.hidden = panel.dataset.usagePanel !== tabId;
      }
    });
  });
})();
