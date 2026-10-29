<c:set var="pageTitle" value="My tickets"/>
<c:set var="navActive" value="tickets"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container page-head">
  <h1>My tickets</h1>
  <p class="muted">Every booking you've made, including cancelled ones, so you always have a record.</p>
</section>

<section class="container stubs">
  <c:choose>
    <c:when test="${empty bookings}">
      <div class="empty">
        <h2>No tickets yet</h2>
        <p>When you book an event, your ticket will show up here.</p>
        <a class="btn btn-gold" href="${ctx}/">Find an event</a>
      </div>
    </c:when>
    <c:otherwise>
      <c:forEach var="b" items="${bookings}">
        <article class="stub ${b.confirmed ? '' : 'is-cancelled'}">
          <div class="stub-band cat-${b.categoryClass}" aria-hidden="true">
            <span class="stub-day"><fmt:formatDate value="${b.eventDate}" pattern="d"/></span>
            <span class="stub-month"><fmt:formatDate value="${b.eventDate}" pattern="MMM"/></span>
          </div>
          <div class="stub-main">
            <h2><a href="${ctx}/event?id=${b.eventId}">${fn:escapeXml(b.eventTitle)}</a></h2>
            <p class="stub-line"><svg class="ic"><use href="#i-clock"/></svg><fmt:formatDate value="${b.eventDate}" pattern="EEE, d MMM yyyy, h:mm a"/></p>
            <p class="stub-line"><svg class="ic"><use href="#i-pin"/></svg>${fn:escapeXml(b.venue)}</p>
            <p class="stub-line">
              <svg class="ic"><use href="#i-ticket"/></svg>
              ${b.quantity} &times; ${fn:escapeXml(b.typeLabel)},
              <c:choose><c:when test="${b.free}">free</c:when><c:otherwise>&#8377;<fmt:formatNumber value="${b.total}" pattern="#,##0.##"/></c:otherwise></c:choose>
            </p>
          </div>
          <div class="stub-side">
            <span class="status status-${b.confirmed ? 'ok' : 'off'}">${b.confirmed ? 'Confirmed' : 'Cancelled'}</span>
            <code class="code">${b.ticketCode}</code>
            <div class="stub-actions">
              <a class="btn btn-ink btn-sm" href="${ctx}/ticket?code=${b.ticketCode}">View ticket</a>
              <c:if test="${b.cancellable}">
                <form method="post" action="${ctx}/cancel" class="inline" data-confirm="Cancel this booking? Your seats will be released.">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
                  <input type="hidden" name="regId" value="${b.id}">
                  <button type="submit" class="btn btn-quiet btn-sm danger">Cancel</button>
                </form>
              </c:if>
            </div>
          </div>
        </article>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
