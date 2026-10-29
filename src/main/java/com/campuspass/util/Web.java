package com.campuspass.util;

import com.campuspass.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Small helpers shared by the servlets. */
public final class Web {

    private Web() { }

    public static User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("user");
    }

    /** One-shot message shown at the top of the next page (header.jspf reads and removes it). */
    public static void flash(HttpServletRequest req, String type, String message) {
        HttpSession s = req.getSession(true);
        s.setAttribute("flashType", type);
        s.setAttribute("flashMsg", message);
    }

    public static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    public static int intParam(HttpServletRequest req, String name, int fallback) {
        try {
            return Integer.parseInt(req.getParameter(name).trim());
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    public static void forward(HttpServletRequest req, HttpServletResponse res, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, res);
    }

    /** Accepts only local paths like /event?id=3, so the login "next" parameter cannot cause open redirects. */
    public static String safeNext(String next) {
        if (next == null) return null;
        next = next.trim();
        if (next.length() < 1 || next.length() > 200 || next.charAt(0) != '/'
                || next.startsWith("//") || next.contains("\\") || next.contains("://")
                || next.indexOf('\r') >= 0 || next.indexOf('\n') >= 0) {
            return null;
        }
        return next;
    }
}
