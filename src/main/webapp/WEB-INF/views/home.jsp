<c:set var="pageTitle" value="Discover events"/>
<c:set var="navActive" value="events"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="hero">
  <div class="container hero-grid">
    <div class="hero-copy">
      <h1>Your seat is waiting.</h1>
      <p class="lead">Browse what's on across campus, pick a ticket and you're in. Seats are counted live, so what you see is what's left.</p>
      <form class="searchbar" action="${ctx}/events" method="get" role="search">
        <svg class="ic"><use href="#i-search"/></svg>
        <input type="search" name="q" value="${fn:escapeXml(q)}" placeholder="Search by event, venue or topic" aria-label="Search events">
        <c:if test="${not empty category}">
          <input type="hidden" name="category" value="${fn:escapeXml(category)}">
        </c:if>
        <button type="submit" class="btn btn-gold">Search</button>
      </form>
      <p class="hero-note">
        ${upcomingCount} upcoming ${upcomingCount == 1 ? 'event is' : 'events are'} open for registration,
        and ${ticketsBooked} ${ticketsBooked == 1 ? 'ticket has' : 'tickets have'} been booked so far.
      </p>
    </div>

    <div class="hero-ticket-wrap">
      <c:choose>
        <c:when test="${not empty heroEvent}">
          <a class="hero-ticket" href="${ctx}/event?id=${heroEvent.id}">
            <span class="ht-band cat-${heroEvent.categoryClass}"><span class="ht-icon">${heroEvent.icon}</span></span>
            <span class="ht-body">
              <span class="ht-kicker">Up next</span>
              <span class="ht-title">${fn:escapeXml(heroEvent.title)}</span>
              <span class="ht-meta"><svg class="ic"><use href="#i-clock"/></svg><fmt:formatDate value="${heroEvent.eventDate}" pattern="h:mm a"/></span>
              <span class="ht-meta"><svg class="ic"><use href="#i-pin"/></svg>${fn:escapeXml(heroEvent.venue)}</span>
              <span class="seatbar"><span style="width:${heroEvent.percentSold}%"></span></span>
              <span class="ht-seats">${heroEvent.seatsLeft} of ${heroEvent.totalSeats} seats left</span>
            </span>
            <span class="ht-stub">
              <span class="ht-day"><fmt:formatDate value="${heroEvent.eventDate}" pattern="d"/></span>
              <span class="ht-month"><fmt:formatDate value="${heroEvent.eventDate}" pattern="MMM"/></span>
            </span>
          </a>
        </c:when>
        <c:otherwise>
          <div class="hero-ticket">
            <span class="ht-band cat-general"><span class="ht-icon">&#127903;&#65039;</span></span>
            <span class="ht-body">
              <span class="ht-kicker">Up next</span>
              <span class="ht-title">No events yet</span>
              <span class="ht-meta">Organisers can publish the first one from the Organiser page.</span>
            </span>
            <span class="ht-stub"><span class="ht-day">--</span></span>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>

<section class="container listing" id="events">
  <c:url var="upUrl" value="/events">
    <c:param name="q" value="${q}"/>
    <c:param name="category" value="${category}"/>
  </c:url>
  <c:url var="pastUrl" value="/events">
    <c:param name="q" value="${q}"/>
    <c:param name="category" value="${category}"/>
    <c:param name="show" value="past"/>
  </c:url>

  <div class="listing-head">
    <div class="tabs" role="tablist">
      <a class="tab ${show == 'upcoming' ? 'active' : ''}" href="${upUrl}#events">Upcoming</a>
      <a class="tab ${show == 'past' ? 'active' : ''}" href="${pastUrl}#events">Past events</a>
    </div>
    <div class="chips" aria-label="Filter by category">
      <c:url var="allUrl" value="/events">
        <c:param name="q" value="${q}"/>
        <c:param name="show" value="${show}"/>
      </c:url>
      <a class="chip ${empty category ? 'active' : ''}" href="${allUrl}#events">All</a>
      <c:forEach var="cat" items="${categories}">
        <c:url var="catUrl" value="/events">
          <c:param name="q" value="${q}"/>
          <c:param name="show" value="${show}"/>
          <c:param name="category" value="${cat}"/>
        </c:url>
        <a class="chip ${category == cat ? 'active' : ''}" href="${catUrl}#events">${fn:escapeXml(cat)}</a>
      </c:forEach>
    </div>
  </div>

  <c:choose>
    <c:when test="${empty events}">
      <div class="empty">
        <h2>Nothing matches yet</h2>
        <p>
          <c:choose>
            <c:when test="${not empty q or not empty category}">Try a different search word, or clear the category filter.</c:when>
            <c:otherwise>No ${show == 'past' ? 'past' : 'upcoming'} events are listed right now. Check back soon.</c:otherwise>
          </c:choose>
        </p>
        <a class="btn btn-ink" href="${ctx}/events">Show all events</a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="grid">
        <c:forEach var="e" items="${events}">
          <a class="ticket-card" href="${ctx}/event?id=${e.id}">
            <span class="tc-banner cat-${e.categoryClass}">
              <span class="tc-icon">${e.icon}</span>
              <span class="badge badge-${e.availability}">${e.availabilityLabel}</span>
            </span>
            <span class="tc-perf" aria-hidden="true"></span>
            <span class="tc-body">
              <span class="tc-date">
                <b><fmt:formatDate value="${e.eventDate}" pattern="d"/></b>
                <i><fmt:formatDate value="${e.eventDate}" pattern="MMM"/></i>
              </span>
              <span class="tc-main">
                <span class="tc-cat">${fn:escapeXml(e.category)}</span>
                <span class="tc-title">${fn:escapeXml(e.title)}</span>
                <span class="tc-meta"><svg class="ic"><use href="#i-clock"/></svg><fmt:formatDate value="${e.eventDate}" pattern="EEE, h:mm a"/></span>
                <span class="tc-meta"><svg class="ic"><use href="#i-pin"/></svg>${fn:escapeXml(e.venue)}</span>
              </span>
            </span>
            <span class="tc-foot">
              <span class="tc-price">
                <c:choose>
                  <c:when test="${e.totalSeats == 0}">Not on sale</c:when>
                  <c:when test="${e.free}">Free</c:when>
                  <c:otherwise>From &#8377;<fmt:formatNumber value="${e.minPrice}" pattern="#,##0.##"/></c:otherwise>
                </c:choose>
              </span>
              <span class="tc-seats">
                <span class="seatbar"><span style="width:${e.percentSold}%"></span></span>
                ${e.seatsLeft} left
              </span>
            </span>
          </a>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
