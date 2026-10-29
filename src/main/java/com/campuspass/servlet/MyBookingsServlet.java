package com.campuspass.servlet;

import com.campuspass.dao.RegistrationDAO;
import com.campuspass.model.User;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/my-bookings")
public class MyBookingsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = Web.currentUser(req);
        try {
            req.setAttribute("bookings", RegistrationDAO.listByUser(user.getId()));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        Web.forward(req, res, "my-bookings.jsp");
    }
}
