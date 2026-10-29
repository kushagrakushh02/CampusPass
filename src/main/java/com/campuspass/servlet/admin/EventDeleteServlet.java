package com.campuspass.servlet.admin;

import com.campuspass.dao.EventDAO;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/event/delete")
public class EventDeleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int id = Web.intParam(req, "id", 0);
        try {
            boolean deleted = EventDAO.deleteOrCancel(id);
            Web.flash(req, "success", deleted
                    ? "Event deleted."
                    : "This event already has bookings, so it was marked as cancelled instead of deleted.");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        res.sendRedirect(req.getContextPath() + "/admin");
    }
}
