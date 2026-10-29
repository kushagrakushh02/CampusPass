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

@WebServlet("/cancel")
public class CancelServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = Web.currentUser(req);
        int regId = Web.intParam(req, "regId", 0);
        try {
            RegistrationDAO.cancel(regId, user.getId());      // only the owner's booking can match
            Web.flash(req, "success", "Booking cancelled. Your seats have been released.");
        } catch (AppException e) {
            Web.flash(req, "error", e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        res.sendRedirect(req.getContextPath() + "/my-bookings");
    }
}
