@import ch.so.agi.datenportal.web.view.HeaderVm
@param HeaderVm header
<header class="dp-site-header" role="banner">
  <div class="dp-site-header__inner">
    <div class="dp-site-brand">
      <a class="dp-site-brand__link" href="${header.logoHref()}">
        <span class="dp-site-brand__name">${header.siteName()}</span>
        <span class="dp-site-brand__claim">${header.siteClaim()}</span>
      </a>
    </div>
    <div class="dp-site-nav">
      <nav class="dp-site-nav__utility" aria-label="Kantonale Navigation">
        <ul class="dp-nav-list">
          @for (var item : header.utilityNav())
            <li>
              <a class="dp-nav-link" href="${item.href()}">${item.label()}</a>
            </li>
          @endfor
        </ul>
      </nav>
      <nav class="dp-site-nav__primary" aria-label="Hauptnavigation">
        <ul class="dp-nav-list">
          @for (var item : header.primaryNav())
            <li>
              @if(item.active())
                <a class="dp-nav-link is-active" href="${item.href()}" aria-current="page">${item.label()}</a>
              @else
                <a class="dp-nav-link" href="${item.href()}">${item.label()}</a>
              @endif
            </li>
          @endfor
        </ul>
      </nav>
    </div>
  </div>
</header>
