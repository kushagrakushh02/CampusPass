<c:set var="pageTitle" value="Log in"/>
<c:set var="navActive" value="login"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container auth">
  <div class="auth-card">
    <div class="auth-side">
      <h1>Welcome back</h1>
      <p>Log in to book seats, see your tickets and cancel a booking if plans change.</p>
    </div>
    <form class="auth-form" method="post" action="${ctx}/login" novalidate>
      <input type="hidden" name="_csrf" value="${sessionScope.csrf}">
      <input type="hidden" name="next" value="${fn:escapeXml(next)}">

      <c:if test="${not empty error}"><p class="alert" role="alert">${fn:escapeXml(error)}</p></c:if>

      <div class="field">
        <label for="email">Email</label>
        <input id="email" type="email" name="email" value="${fn:escapeXml(email)}" autocomplete="email" required autofocus>
      </div>
      <div class="field">
        <label for="password">Password</label>
        <div class="pw">
          <input id="password" type="password" name="password" autocomplete="current-password" required>
          <button type="button" class="pw-toggle" aria-label="Show password"><svg class="ic"><use href="#i-eye"/></svg></button>
        </div>
      </div>
      <button type="submit" class="btn btn-ink btn-block">Log in</button>
      <p class="fine center">No account yet? <a href="${ctx}/register">Create one</a></p>
    </form>
  </div>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
