package com.campuspass.servlet;

import com.campuspass.dao.RegistrationDAO;
import com.campuspass.model.User;
import com.campuspass.util.AppException;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/book")
public class BookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = Web.currentUser(req);
        int eventId = Web.intParam(req, "eventId", 0);
        int typeId = Web.intParam(req, "typeId", 0);
        int qty = Web.intParam(req, "qty", 1);
        try {
            String code = RegistrationDAO.book(user.getId(), eventId, typeId, qty);
            Web.flash(req, "success", "You're in! Your booking is confirmed.");
            res.sendRedirect(req.getContextPath() + "/ticket?code=" + code);
        } catch (AppException e) {
            Web.flash(req, "error", e.getMessage());
            res.sendRedirect(req.getContextPath() + "/event?id=" + eventId);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
