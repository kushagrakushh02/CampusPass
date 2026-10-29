<c:set var="pageTitle" value="${event.title}"/>
<c:set var="navActive" value="events"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="event-hero cat-${event.categoryClass}">
  <div class="container">
    <a class="backlink" href="${ctx}/"><svg class="ic"><use href="#i-back"/></svg>All events</a>
    <div class="eh-row">
      <span class="eh-icon" aria-hidden="true">${event.icon}</span>
      <div>
        <span class="badge badge-${event.availability}">${event.availabilityLabel}</span>
        <h1>${fn:escapeXml(event.title)}</h1>
        <p class="eh-meta">
          <span><svg class="ic"><use href="#i-cal"/></svg><fmt:formatDate value="${event.eventDate}" pattern="EEEE, d MMMM yyyy"/></span>
          <span><svg class="ic"><use href="#i-clock"/></svg><fmt:formatDate value="${event.eventDate}" pattern="h:mm a"/></span>
          <span><svg class="ic"><use href="#i-pin"/></svg>${fn:escapeXml(event.venue)}</span>
        </p>
      </div>
    </div>
  </div>
</section>

<section class="container event-layout">
  <article class="panel about">
    <h2>About this event</h2>
    <c:choose>
      <c:when test="${empty event.description}"><p class="muted">The organisers have not added a description yet.</p></c:when>
      <c:otherwise><p class="prewrap">${fn:escapeXml(event.description)}</p></c:otherwise>
    </c:choose>

    <dl class="facts">
      <div><dt>Category</dt><dd>${fn:escapeXml(event.category)}</dd></div>
      <div><dt>Venue</dt><dd>${fn:escapeXml(event.venue)}</dd></div>
      <div><dt>Capacity</dt><dd>${event.totalSeats} seats in ${fn:length(event.ticketTypes)} ${fn:length(event.ticketTypes) == 1 ? 'category' : 'categories'}</dd></div>
      <div><dt>Still available</dt><dd>${event.seatsLeft} seats</dd></div>
    </dl>

    <c:if test="${not event.past and not event.cancelled}">
      <p class="countdown" data-start="${event.eventDate.time}" aria-live="polite"></p>
    </c:if>
  </article>

  <aside class="panel book-panel" id="book">
    <h2>Get your ticket</h2>
    <c:choose>
      <c:when test="${event.cancelled}">
        <p class="notice notice-bad">This event has been cancelled, so no tickets are on sale.</p>
      </c:when>
      <c:when test="${event.past}">
        <p class="notice">This event has already taken place.</p>
      </c:when>
      <c:when test="${empty event.ticketTypes}">
        <p class="notice">Tickets are not on sale yet. Check back soon.</p>
      </c:when>
      <c:when test="${not empty myBooking}">
        <p class="notice notice-good">
          <svg class="ic"><use href="#i-check"/></svg>
          You're registered: ${myBooking.quantity} &times; ${fn:escapeXml(myBooking.typeLabel)}.
        </p>
        <a class="btn btn-ink btn-block" href="${ctx}/ticket?code=${myBooking.ticketCode}">View your ticket</a>
        <p class="fine">Want to change it? Cancel it from My tickets and book again.</p>
      </c:when>
      <c:when test="${event.seatsLeft le 0}">
        <p class="notice notice-bad">Sold out. If someone cancels, seats will show up here again.</p>
      </c:when>
      <c:when test="${empty sessionScope.user}">
        <ul class="tt-readonly">
          <c:forEach var="t" items="${event.ticketTypes}">
            <li>
              <span>${fn:escapeXml(t.label)}<small><c:choose><c:when test="${t.soldOut}">Sold out</c:when><c:otherwise>${t.seatsLeft} left</c:otherwise></c:choose></small></span>
              <b><c:choose><c:when test="${t.free}">Free</c:when><c:otherwise>&#8377;<fmt:formatNumber value="${t.price}" pattern="#,##0.##"/></c:otherwise></c:choose></b>
            </li>
          </c:forEach>
        </ul>
        <c:url var="loginUrl" value="/login"><c:param name="next" value="/event?id=${event.id}"/></c:url>
        <a class="btn btn-gold btn-block" href="${loginUrl}">Log in to book</a>
        <p class="fine">New here? <a href="${ctx}/register">Create a free account</a>, it takes a minute.</p>
      </c:when>
      <c:otherwise>
        <form method="post" action="${ctx}/book" id="booking-form" data-max="${maxQty}">
          <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
          <input type="hidden" name="eventId" value="${event.id}">

          <fieldset class="tt-list">
            <legend>Choose a ticket</legend>
            <c:forEach var="t" items="${event.ticketTypes}">
              <label class="tt ${t.soldOut ? 'is-off' : ''}">
                <input type="radio" name="typeId" value="${t.id}" data-price="${t.price}" data-left="${t.seatsLeft}" ${t.soldOut ? 'disabled' : ''}>
                <span class="tt-main">
                  <b>${fn:escapeXml(t.label)}</b>
                  <small><c:choose><c:when test="${t.soldOut}">Sold out</c:when><c:otherwise>${t.seatsLeft} seats left</c:otherwise></c:choose></small>
                </span>
                <span class="tt-price">
                  <c:choose><c:when test="${t.free}">Free</c:when><c:otherwise>&#8377;<fmt:formatNumber value="${t.price}" pattern="#,##0.##"/></c:otherwise></c:choose>
                </span>
              </label>
            </c:forEach>
          </fieldset>

          <div class="qty-row">
            <label for="qty">How many?</label>
            <div class="stepper">
              <button type="button" data-step="-1" aria-label="One fewer">&minus;</button>
              <input id="qty" type="number" name="qty" value="1" min="1" max="${maxQty}" inputmode="numeric">
              <button type="button" data-step="1" aria-label="One more">+</button>
            </div>
          </div>

          <div class="total-row"><span>Total</span><b id="total">Free</b></div>
          <button type="submit" class="btn btn-gold btn-block" id="book-btn" disabled>Confirm booking</button>
          <p class="fine">Up to ${maxQty} tickets per booking. You can cancel any time before the event starts.</p>
        </form>
      </c:otherwise>
    </c:choose>
  </aside>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
