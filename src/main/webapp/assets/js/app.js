/* CampusPass - small progressive enhancements. The site still works without JavaScript. */
(function () {
  'use strict';

  // mobile menu
  var toggle = document.querySelector('.nav-toggle');
  var menu = document.getElementById('nav-menu');
  if (toggle && menu) {
    toggle.addEventListener('click', function () {
      var open = menu.classList.toggle('open');
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    });
  }

  // toast: close button and auto-hide
  var toast = document.querySelector('.toast');
  if (toast) {
    var hide = function () { toast.classList.add('hide'); setTimeout(function () { toast.remove(); }, 350); };
    toast.querySelector('.toast-x').addEventListener('click', hide);
    setTimeout(hide, 6000);
  }

  // confirm before destructive actions (forms with data-confirm, or buttons with data-confirm)
  document.addEventListener('submit', function (e) {
    var form = e.target;
    var msg = form.getAttribute('data-confirm') || (e.submitter && e.submitter.getAttribute('data-confirm'));
    if (msg && !window.confirm(msg)) e.preventDefault();
  });

  // print button on the ticket page
  var printBtn = document.querySelector('[data-print]');
  if (printBtn) printBtn.addEventListener('click', function () { window.print(); });

  // show / hide password
  document.querySelectorAll('.pw-toggle').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var input = btn.parentElement.querySelector('input');
      var show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
    });
  });

  // password strength meter on the sign-up form
  var pw = document.querySelector('.auth-form input[name=password][autocomplete=new-password]');
  var bar = document.querySelector('.strength span');
  var hint = document.getElementById('pw-hint');
  if (pw && bar) {
    pw.addEventListener('input', function () {
      var v = pw.value, score = 0;
      if (v.length >= 8) score++;
      if (/[A-Za-z]/.test(v) && /\d/.test(v)) score++;
      if (v.length >= 12) score++;
      if (/[^A-Za-z0-9]/.test(v)) score++;
      var colours = ['#c73636', '#c73636', '#e29d00', '#17835a', '#17835a'];
      var words = ['Too short', 'Too short', 'Okay', 'Good', 'Strong'];
      bar.style.width = (v ? Math.max(score, 1) * 25 : 0) + '%';
      bar.style.background = colours[score];
      if (hint) hint.textContent = v ? words[score] : 'At least 8 characters, with letters and numbers.';
    });
  }

  // booking panel: pick a ticket type, set quantity, see the total
  var form = document.getElementById('booking-form');
  if (form) {
    var radios = form.querySelectorAll('input[name=typeId]');
    var qty = document.getElementById('qty');
    var total = document.getElementById('total');
    var btn = document.getElementById('book-btn');
    var max = parseInt(form.getAttribute('data-max'), 10) || 5;

    var selected = function () { return form.querySelector('input[name=typeId]:checked'); };
    var limit = function () {
      var r = selected();
      return r ? Math.max(1, Math.min(max, parseInt(r.getAttribute('data-left'), 10))) : max;
    };
    var money = function (n) { return n === 0 ? 'Free' : '\u20B9' + n.toLocaleString('en-IN', { maximumFractionDigits: 2 }); };
    var refresh = function () {
      var r = selected();
      var q = Math.min(Math.max(parseInt(qty.value, 10) || 1, 1), limit());
      qty.value = q;
      qty.max = limit();
      total.textContent = r ? money(parseFloat(r.getAttribute('data-price')) * q) : '-';
      btn.disabled = !r;
    };

    var firstOpen = null;
    radios.forEach(function (r) { if (!firstOpen && !r.disabled) firstOpen = r; r.addEventListener('change', refresh); });
    if (firstOpen) firstOpen.checked = true;
    form.querySelectorAll('[data-step]').forEach(function (b) {
      b.addEventListener('click', function () {
        qty.value = (parseInt(qty.value, 10) || 1) + parseInt(b.getAttribute('data-step'), 10);
        refresh();
      });
    });
    qty.addEventListener('input', refresh);
    refresh();
  }

  // countdown on the event page
  var cd = document.querySelector('.countdown');
  if (cd) {
    var start = parseInt(cd.getAttribute('data-start'), 10);
    var tick = function () {
      var ms = start - Date.now();
      if (ms <= 0) { cd.textContent = 'This event has started.'; return; }
      var d = Math.floor(ms / 864e5), h = Math.floor(ms % 864e5 / 36e5), m = Math.floor(ms % 36e5 / 6e4);
      cd.textContent = d > 0 ? 'Starts in ' + d + (d === 1 ? ' day ' : ' days ') + h + ' h'
                             : 'Starts in ' + h + ' h ' + m + ' min';
    };
    tick();
    setInterval(tick, 30000);
  }

  // admin: add / remove ticket-type rows while creating an event
  var rows = document.getElementById('type-rows');
  var addBtn = document.getElementById('add-type');
  if (rows && addBtn) {
    addBtn.addEventListener('click', function () {
      var clone = rows.querySelector('.type-row').cloneNode(true);
      clone.querySelectorAll('input').forEach(function (i) { i.value = ''; });
      rows.appendChild(clone);
      clone.querySelector('input').focus();
    });
    rows.addEventListener('click', function (e) {
      var rm = e.target.closest('[data-remove-row]');
      if (rm && rows.querySelectorAll('.type-row').length > 1) rm.closest('.type-row').remove();
    });
  }

  // admin: live filter for the registrations table
  var filter = document.getElementById('reg-filter');
  var table = document.getElementById('reg-table');
  if (filter && table) {
    filter.addEventListener('input', function () {
      var q = filter.value.trim().toLowerCase();
      table.querySelectorAll('tbody tr').forEach(function (tr) {
        tr.style.display = !q || tr.textContent.toLowerCase().indexOf(q) >= 0 ? '' : 'none';
      });
    });
  }
})();
