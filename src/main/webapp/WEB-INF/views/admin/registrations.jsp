<c:set var="pageTitle" value="Registrations"/>
<c:set var="navActive" value="admin"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container page-head head-row">
  <div>
    <a class="backlink" href="${ctx}/admin"><svg class="ic"><use href="#i-back"/></svg>Dashboard</a>
    <h1>${fn:escapeXml(event.title)}</h1>
    <p class="muted"><fmt:formatDate value="${event.eventDate}" pattern="EEE, d MMM yyyy, h:mm a"/>, ${fn:escapeXml(event.venue)}</p>
  </div>
  <a class="btn btn-ink" href="${ctx}/admin/registrations?eventId=${event.id}&amp;format=csv"><svg class="ic"><use href="#i-download"/></svg>Download CSV</a>
</section>

<section class="container">
  <div class="summary" role="group" aria-label="Summary">
    <div><b>${event.ticketsSold}</b><span>tickets sold</span></div>
    <div><b>${event.seatsLeft}</b><span>seats left</span></div>
    <div><b>${fn:length(registrations)}</b><span>bookings, including cancelled</span></div>
    <div><b>&#8377;<fmt:formatNumber value="${event.revenue}" pattern="#,##0.##"/></b><span>revenue</span></div>
  </div>

  <div class="table-tools">
    <svg class="ic"><use href="#i-search"/></svg>
    <input type="search" id="reg-filter" placeholder="Filter by name, email or ticket code" aria-label="Filter registrations">
  </div>

  <div class="table-wrap">
    <table class="table" id="reg-table">
      <thead>
        <tr><th>Ticket code</th><th>Attendee</th><th>Ticket</th><th class="num">Qty</th><th class="num">Amount</th><th>Status</th><th>Booked</th></tr>
      </thead>
      <tbody>
        <c:forEach var="r" items="${registrations}">
          <tr class="${r.confirmed ? '' : 'is-cancelled'}">
            <td data-label="Code"><code class="code">${r.ticketCode}</code></td>
            <td data-label="Attendee"><span class="row-title">${fn:escapeXml(r.userName)}</span><small class="muted">${fn:escapeXml(r.userEmail)}</small></td>
            <td data-label="Ticket">${fn:escapeXml(r.typeLabel)}</td>
            <td class="num" data-label="Qty">${r.quantity}</td>
            <td class="num" data-label="Amount"><c:choose><c:when test="${r.free}">Free</c:when><c:otherwise>&#8377;<fmt:formatNumber value="${r.total}" pattern="#,##0.##"/></c:otherwise></c:choose></td>
            <td data-label="Status"><span class="status status-${r.confirmed ? 'ok' : 'off'}">${r.confirmed ? 'Confirmed' : 'Cancelled'}</span></td>
            <td data-label="Booked"><fmt:formatDate value="${r.bookedAt}" pattern="d MMM, h:mm a"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty registrations}">
          <tr><td colspan="7" class="center muted pad">Nobody has registered yet.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
