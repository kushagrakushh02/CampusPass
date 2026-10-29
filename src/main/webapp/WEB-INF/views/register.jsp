<c:set var="pageTitle" value="Create account"/>
<c:set var="navActive" value="login"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="container auth">
  <div class="auth-card">
    <div class="auth-side">
      <h1>Create your account</h1>
      <p>One account is all you need to book tickets for any event on campus.</p>
    </div>
    <form class="auth-form" method="post" action="${ctx}/register" novalidate>
      <input type="hidden" name="_csrf" value="${sessionScope.csrf}">

      <c:if test="${not empty error}"><p class="alert" role="alert">${fn:escapeXml(error)}</p></c:if>

      <div class="field">
        <label for="name">Full name</label>
        <input id="name" type="text" name="name" value="${fn:escapeXml(name)}" autocomplete="name" maxlength="80" required autofocus>
      </div>
      <div class="field">
        <label for="email">Email</label>
        <input id="email" type="email" name="email" value="${fn:escapeXml(email)}" autocomplete="email" maxlength="120" required>
      </div>
      <div class="field">
        <label for="password">Password</label>
        <div class="pw">
          <input id="password" type="password" name="password" autocomplete="new-password" minlength="8" required>
          <button type="button" class="pw-toggle" aria-label="Show password"><svg class="ic"><use href="#i-eye"/></svg></button>
        </div>
        <div class="strength" aria-hidden="true"><span></span></div>
        <small class="hint" id="pw-hint">At least 8 characters, with letters and numbers.</small>
      </div>
      <div class="field">
        <label for="confirm">Confirm password</label>
        <input id="confirm" type="password" name="confirm" autocomplete="new-password" required>
      </div>
      <button type="submit" class="btn btn-ink btn-block">Create account</button>
      <p class="fine center">Already registered? <a href="${ctx}/login">Log in</a></p>
    </form>
  </div>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
