package com.campuspass.servlet;

import com.campuspass.dao.UserDAO;
import com.campuspass.model.User;
import com.campuspass.util.PasswordUtil;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (Web.currentUser(req) != null) {
            res.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.setAttribute("next", Web.safeNext(req.getParameter("next")));
        Web.forward(req, res, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = Web.clean(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password");
        String next = Web.safeNext(req.getParameter("next"));

        User user;
        try {
            user = UserDAO.findByEmail(email);
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // same message whether the email or the password was wrong
        if (!PasswordUtil.verify(password, user == null ? null : user.getPasswordHash())) {
            req.setAttribute("error", "That email and password do not match. Please try again.");
            req.setAttribute("email", email);
            req.setAttribute("next", next);
            Web.forward(req, res, "login.jsp");
            return;
        }

        req.changeSessionId();                       // new session id after login (prevents session fixation)
        user.setPasswordHash(null);                  // the hash never needs to live in the session
        req.getSession().setAttribute("user", user);
        Web.flash(req, "success", "Welcome back, " + user.getName() + ".");
        String target = next != null ? next : (user.isAdmin() ? "/admin" : "/");
        res.sendRedirect(req.getContextPath() + target);
    }
}
