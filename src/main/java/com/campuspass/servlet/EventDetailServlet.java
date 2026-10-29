package com.campuspass.servlet;

import com.campuspass.dao.EventDAO;
import com.campuspass.dao.RegistrationDAO;
import com.campuspass.model.Event;
import com.campuspass.model.User;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/event")
public class EventDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int id = Web.intParam(req, "id", 0);
        try {
            Event event = EventDAO.findById(id);
            if (event == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            User user = Web.currentUser(req);
            if (user != null) {
                req.setAttribute("myBooking", RegistrationDAO.findActive(user.getId(), id));
            }
            req.setAttribute("event", event);
            req.setAttribute("maxQty", RegistrationDAO.MAX_PER_BOOKING);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        Web.forward(req, res, "event.jsp");
    }
}
