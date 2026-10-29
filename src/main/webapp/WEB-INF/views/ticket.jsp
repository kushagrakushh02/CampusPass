<c:set var="pageTitle" value="Your ticket"/>
<c:set var="navActive" value="tickets"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container ticket-page">
  <div class="ticket-tools no-print">
    <a class="backlink" href="${ctx}/my-bookings"><svg class="ic"><use href="#i-back"/></svg>My tickets</a>
    <button type="button" class="btn btn-ink btn-sm" data-print><svg class="ic"><use href="#i-print"/></svg>Print ticket</button>
  </div>

  <article class="big-ticket ${reg.confirmed ? '' : 'is-cancelled'}">
    <div class="bt-main">
      <span class="bt-band cat-${reg.categoryClass}"></span>
      <div class="bt-content">
        <p class="bt-kicker">${reg.confirmed ? 'Admit ' : 'Cancelled: '}${reg.quantity}</p>
        <h1>${fn:escapeXml(reg.eventTitle)}</h1>
        <dl class="bt-grid">
          <div><dt>Date</dt><dd><fmt:formatDate value="${reg.eventDate}" pattern="EEE, d MMMM yyyy"/></dd></div>
          <div><dt>Time</dt><dd><fmt:formatDate value="${reg.eventDate}" pattern="h:mm a"/></dd></div>
          <div><dt>Venue</dt><dd>${fn:escapeXml(reg.venue)}</dd></div>
          <div><dt>Ticket</dt><dd>${fn:escapeXml(reg.typeLabel)}</dd></div>
          <div><dt>Name</dt><dd>${fn:escapeXml(reg.userName)}</dd></div>
          <div><dt>Paid</dt><dd>
            <c:choose><c:when test="${reg.free}">Free</c:when>
              <c:otherwise>&#8377;<fmt:formatNumber value="${reg.total}" pattern="#,##0.##"/></c:otherwise></c:choose>
          </dd></div>
        </dl>
      </div>
    </div>
    <div class="bt-perf" aria-hidden="true"></div>
    <div class="bt-stub">
      <span class="bt-label">Ticket code</span>
      <code class="bt-code">${reg.ticketCode}</code>
      <span class="status status-${reg.confirmed ? 'ok' : 'off'}">${reg.confirmed ? 'Confirmed' : 'Cancelled'}</span>
      <span class="bt-label">Booked <fmt:formatDate value="${reg.bookedAt}" pattern="d MMM yyyy, h:mm a"/></span>
    </div>
  </article>

  <p class="fine center no-print">Show this code at the entrance. Each code can be used for the number of people written on the ticket.</p>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
