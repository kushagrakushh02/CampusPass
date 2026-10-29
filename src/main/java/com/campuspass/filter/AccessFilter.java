package com.campuspass.filter;

import com.campuspass.model.User;
import com.campuspass.util.Web;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * One gatekeeper in front of every request:
 *   1. sets UTF-8 and basic security headers,
 *   2. makes sure the session holds a CSRF token and checks it on every POST,
 *   3. sends anonymous visitors to the login page when they open a protected URL,
 *   4. keeps normal users out of /admin.
 */
@WebFilter("/*")
public class AccessFilter implements Filter {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");

        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        if (path.isEmpty()) path = "/";

        if (path.startsWith("/assets/")) {          // css and js need none of the checks below
            chain.doFilter(request, response);
            return;
        }

        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Referrer-Policy", "same-origin");

        HttpSession session = req.getSession(true);
        if (session.getAttribute("csrf") == null) {
            byte[] bytes = new byte[24];
            RANDOM.nextBytes(bytes);
            session.setAttribute("csrf", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String sent = req.getParameter("_csrf");
            String real = (String) session.getAttribute("csrf");
            if (sent == null || !MessageDigest.isEqual(sent.getBytes(StandardCharsets.UTF_8), real.getBytes(StandardCharsets.UTF_8))) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Your session security token is missing or has expired. Please reload the page and try again.");
                return;
            }
        }

        boolean adminArea = path.equals("/admin") || path.startsWith("/admin/");
        boolean memberArea = path.equals("/book") || path.equals("/my-bookings")
                          || path.equals("/cancel") || path.equals("/ticket");

        if (adminArea || memberArea) {
            res.setHeader("Cache-Control", "no-store");
            User user = (User) session.getAttribute("user");
            if (user == null) {
                Web.flash(req, "error", "Please log in to continue.");
                String target = req.getContextPath() + "/login";
                if ("GET".equalsIgnoreCase(req.getMethod())) {
                    String back = path + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
                    target += "?next=" + URLEncoder.encode(back, StandardCharsets.UTF_8);
                }
                res.sendRedirect(target);
                return;
            }
            if (adminArea && !user.isAdmin()) {
                Web.flash(req, "error", "That area is for organisers only.");
                res.sendRedirect(req.getContextPath() + "/");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
