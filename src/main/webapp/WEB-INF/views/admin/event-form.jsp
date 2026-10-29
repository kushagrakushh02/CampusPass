<c:set var="editing" value="${event.id > 0}"/>
<c:set var="pageTitle" value="${editing ? 'Edit event' : 'New event'}"/>
<c:set var="navActive" value="admin"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container page-head">
  <a class="backlink" href="${ctx}/admin"><svg class="ic"><use href="#i-back"/></svg>Dashboard</a>
  <h1>${editing ? 'Edit event' : 'New event'}</h1>
</section>

<section class="container form-layout">
  <form class="panel form-panel" method="post" action="${ctx}/admin/event" id="event-form">
    <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
    <input type="hidden" name="id" value="${event.id}">

    <c:if test="${not empty error}"><p class="alert" role="alert">${fn:escapeXml(error)}</p></c:if>

    <div class="field">
      <label for="title">Event title</label>
      <input id="title" type="text" name="title" value="${fn:escapeXml(event.title)}" maxlength="150" required>
    </div>

    <div class="two">
      <div class="field">
        <label for="category">Category</label>
        <select id="category" name="category">
          <c:forEach var="c" items="${categories}">
            <option value="${fn:escapeXml(c)}" ${event.category == c ? 'selected' : ''}>${fn:escapeXml(c)}</option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="eventDate">Date and time</label>
        <input id="eventDate" type="datetime-local" name="eventDate" required
               value="<fmt:formatDate value="${event.eventDate}" pattern="yyyy-MM-dd'T'HH:mm"/>">
      </div>
    </div>

    <div class="field">
      <label for="venue">Venue</label>
      <input id="venue" type="text" name="venue" value="${fn:escapeXml(event.venue)}" maxlength="150" required>
    </div>

    <div class="field">
      <label for="description">Description</label>
      <textarea id="description" name="description" rows="5" placeholder="What will happen, who it is for, what to bring">${fn:escapeXml(event.description)}</textarea>
    </div>

    <c:if test="${editing}">
      <div class="field">
        <label for="status">Status</label>
        <select id="status" name="status">
          <option value="ACTIVE" ${event.status == 'ACTIVE' ? 'selected' : ''}>Active, visible and bookable</option>
          <option value="CANCELLED" ${event.status == 'CANCELLED' ? 'selected' : ''}>Cancelled, hidden from the public list</option>
        </select>
      </div>
    </c:if>

    <c:if test="${not editing}">
      <fieldset class="types-new">
        <legend>Ticket types</legend>
        <p class="hint">Each type has its own price and seat count. Use a price of 0 for free entry.</p>
        <div id="type-rows">
          <c:forEach var="t" items="${event.ticketTypes}">
            <div class="type-row">
              <input type="text" name="typeLabel" value="${fn:escapeXml(t.label)}" placeholder="Name, e.g. Student" maxlength="60" aria-label="Ticket name">
              <input type="number" name="typePrice" value="${t.price}" min="0" step="0.01" placeholder="Price" aria-label="Price in rupees">
              <input type="number" name="typeSeats" value="${t.totalSeats}" min="1" placeholder="Seats" aria-label="Seats">
              <button type="button" class="icon-btn danger" data-remove-row aria-label="Remove ticket type"><svg class="ic"><use href="#i-trash"/></svg></button>
            </div>
          </c:forEach>
          <c:if test="${empty event.ticketTypes}">
            <div class="type-row">
              <input type="text" name="typeLabel" value="General" placeholder="Name, e.g. Student" maxlength="60" aria-label="Ticket name">
              <input type="number" name="typePrice" value="0" min="0" step="0.01" placeholder="Price" aria-label="Price in rupees">
              <input type="number" name="typeSeats" value="100" min="1" placeholder="Seats" aria-label="Seats">
              <button type="button" class="icon-btn danger" data-remove-row aria-label="Remove ticket type"><svg class="ic"><use href="#i-trash"/></svg></button>
            </div>
          </c:if>
        </div>
        <button type="button" class="btn btn-quiet btn-sm" id="add-type"><svg class="ic"><use href="#i-plus"/></svg>Add another type</button>
      </fieldset>
    </c:if>

    <div class="form-actions">
      <button type="submit" class="btn btn-ink">${editing ? 'Save changes' : 'Publish event'}</button>
      <a class="btn btn-quiet" href="${ctx}/admin">Cancel</a>
    </div>
  </form>

  <c:if test="${editing}">
    <div class="panel types-panel">
      <h2>Ticket types</h2>
      <p class="hint">Raising the seat count adds seats. It can't go lower than the tickets already sold.</p>

      <c:forEach var="t" items="${event.ticketTypes}">
        <form class="tt-edit" method="post" action="${ctx}/admin/ticket-type">
          <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
          <input type="hidden" name="eventId" value="${event.id}">
          <input type="hidden" name="typeId" value="${t.id}">
          <input type="text" name="label" value="${fn:escapeXml(t.label)}" maxlength="60" aria-label="Ticket name">
          <input type="number" name="price" value="${t.price}" min="0" step="0.01" aria-label="Price in rupees">
          <input type="number" name="seats" value="${t.totalSeats}" min="1" aria-label="Total seats">
          <small class="muted">${t.sold} sold</small>
          <button type="submit" name="action" value="update" class="btn btn-ink btn-sm">Save</button>
          <button type="submit" name="action" value="delete" class="icon-btn danger" data-confirm="Remove this ticket type?" aria-label="Remove ${fn:escapeXml(t.label)}"><svg class="ic"><use href="#i-trash"/></svg></button>
        </form>
      </c:forEach>

      <form class="tt-edit tt-add" method="post" action="${ctx}/admin/ticket-type">
        <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="eventId" value="${event.id}">
        <input type="text" name="label" placeholder="New ticket name" maxlength="60" aria-label="New ticket name" required>
        <input type="number" name="price" placeholder="Price" min="0" step="0.01" aria-label="Price in rupees">
        <input type="number" name="seats" placeholder="Seats" min="1" aria-label="Seats" required>
        <span></span>
        <button type="submit" name="action" value="add" class="btn btn-gold btn-sm">Add</button>
        <span></span>
      </form>

      <a class="btn btn-quiet btn-block" href="${ctx}/admin/registrations?eventId=${event.id}"><svg class="ic"><use href="#i-users"/></svg>See who has registered</a>
    </div>
  </c:if>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
