<%@ page isErrorPage="true" %>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="pageTitle" value="Something went wrong"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container empty-page">
  <p class="err-code">${empty code ? 500 : code}</p>
  <h1>
    <c:choose>
      <c:when test="${code == 404}">We couldn't find that page</c:when>
      <c:when test="${code == 403}">That action was blocked</c:when>
      <c:when test="${code == 405}">That page doesn't work this way</c:when>
      <c:otherwise>Something went wrong on our side</c:otherwise>
    </c:choose>
  </h1>
  <p class="muted">
    <c:choose>
      <c:when test="${code == 404}">The link may be old, or the ticket or event doesn't exist.</c:when>
      <c:when test="${code == 403}">Your session may have expired. Go back, reload the page and try again.</c:when>
      <c:otherwise>Nothing you did caused this. Please try again in a moment. If it keeps happening, tell the organisers.</c:otherwise>
    </c:choose>
  </p>
  <a class="btn btn-ink" href="${ctx}/">Back to events</a>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
