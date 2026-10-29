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
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (Web.currentUser(req) != null) {
            res.sendRedirect(req.getContextPath() + "/");
            return;
        }
        Web.forward(req, res, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String name = Web.clean(req.getParameter("name"));
        String email = Web.clean(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");

        String error = null;
        if (name.length() < 2 || name.length() > 80) {
            error = "Please enter your name (2 to 80 characters).";
        } else if (!EMAIL.matcher(email).matches() || email.length() > 120) {
            error = "That email address does not look right.";
        } else if (!PasswordUtil.isStrongEnough(password)) {
            error = "Use at least 8 characters with a mix of letters and numbers.";
        } else if (!password.equals(confirm)) {
            error = "The two passwords are not the same.";
        }

        if (error == null) {
            try {
                if (UserDAO.findByEmail(email) != null) {
                    error = "An account with this email already exists. Try logging in instead.";
                } else {
                    int id = UserDAO.create(name, email, PasswordUtil.hash(password), "USER");
                    User u = new User();
                    u.setId(id);
                    u.setName(name);
                    u.setEmail(email);
                    req.changeSessionId();
                    req.getSession().setAttribute("user", u);
                    Web.flash(req, "success", "Your account is ready. Pick an event and book your first ticket.");
                    res.sendRedirect(req.getContextPath() + "/");
                    return;
                }
            } catch (SQLIntegrityConstraintViolationException dup) {
                error = "An account with this email already exists. Try logging in instead.";
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
        req.setAttribute("error", error);
        req.setAttribute("name", name);
        req.setAttribute("email", email);
        Web.forward(req, res, "register.jsp");
    }
}
