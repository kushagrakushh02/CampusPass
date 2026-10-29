<c:set var="pageTitle" value="Organiser dashboard"/>
<c:set var="navActive" value="admin"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container page-head head-row">
  <div>
    <h1>Organiser dashboard</h1>
    <p class="muted">Everything you've published, and how fast the seats are going.</p>
  </div>
  <a class="btn btn-gold" href="${ctx}/admin/event"><svg class="ic"><use href="#i-plus"/></svg>New event</a>
</section>

<section class="container">
  <div class="summary" role="group" aria-label="Summary">
    <div><b>${fn:length(events)}</b><span>events created</span></div>
    <div><b>${ticketsSold}</b><span>tickets sold</span></div>
    <div><b>${seatsOpen}</b><span>seats still open</span></div>
    <div><b>&#8377;<fmt:formatNumber value="${revenue}" pattern="#,##0.##"/></b><span>ticket revenue</span></div>
    <div><b>${userCount}</b><span>registered users</span></div>
  </div>
</section>

<section class="container">
  <div class="table-wrap">
    <table class="table">
      <thead>
        <tr><th>Event</th><th>Date</th><th>Status</th><th>Seats sold</th><th class="num">Revenue</th><th class="actions-col">Actions</th></tr>
      </thead>
      <tbody>
        <c:forEach var="e" items="${events}">
          <tr>
            <td data-label="Event">
              <a class="row-title" href="${ctx}/admin/event?id=${e.id}">${fn:escapeXml(e.title)}</a>
              <small class="muted">${fn:escapeXml(e.category)}, ${fn:escapeXml(e.venue)}</small>
            </td>
            <td data-label="Date"><fmt:formatDate value="${e.eventDate}" pattern="d MMM yyyy, h:mm a"/></td>
            <td data-label="Status"><span class="badge badge-${e.availability}">${e.availabilityLabel}</span></td>
            <td data-label="Seats sold">
              <span class="seatbar wide"><span style="width:${e.percentSold}%"></span></span>
              <small>${e.totalSeats - e.seatsLeft} of ${e.totalSeats}</small>
            </td>
            <td class="num" data-label="Revenue">&#8377;<fmt:formatNumber value="${e.revenue}" pattern="#,##0.##"/></td>
            <td class="actions-col">
              <a class="icon-btn" href="${ctx}/admin/registrations?eventId=${e.id}" title="Registrations" aria-label="Registrations for ${fn:escapeXml(e.title)}"><svg class="ic"><use href="#i-users"/></svg></a>
              <a class="icon-btn" href="${ctx}/admin/event?id=${e.id}" title="Edit" aria-label="Edit ${fn:escapeXml(e.title)}"><svg class="ic"><use href="#i-edit"/></svg></a>
              <form method="post" action="${ctx}/admin/event/delete" class="inline"
                    data-confirm="Delete this event? If people have booked, it will be marked as cancelled instead.">
                <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="id" value="${e.id}">
                <button type="submit" class="icon-btn danger" title="Delete" aria-label="Delete ${fn:escapeXml(e.title)}"><svg class="ic"><use href="#i-trash"/></svg></button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty events}">
          <tr><td colspan="6" class="center muted pad">No events yet. Use New event to publish the first one.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
