package com.campuspass.servlet.admin;

import com.campuspass.dao.EventDAO;
import com.campuspass.dao.UserDAO;
import com.campuspass.model.Event;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Organiser dashboard: summary figures and the list of all events. The AccessFilter has already checked the role. */
@WebServlet("/admin")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            List<Event> events = EventDAO.listAll();
            int tickets = 0;
            int seatsLeft = 0;
            BigDecimal revenue = BigDecimal.ZERO;
            for (Event e : events) {
                tickets += e.getTicketsSold();
                seatsLeft += e.isCancelled() || e.isPast() ? 0 : e.getSeatsLeft();
                revenue = revenue.add(e.getRevenue());
            }
            req.setAttribute("events", events);
            req.setAttribute("ticketsSold", tickets);
            req.setAttribute("seatsOpen", seatsLeft);
            req.setAttribute("revenue", revenue);
            req.setAttribute("userCount", UserDAO.count());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        Web.forward(req, res, "admin/dashboard.jsp");
    }
}
