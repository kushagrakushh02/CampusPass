package com.campuspass.servlet.admin;

import com.campuspass.dao.TicketTypeDAO;
import com.campuspass.util.AppException;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Add, update or delete one ticket type of an existing event. */
@WebServlet("/admin/ticket-type")
public class TicketTypeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = Web.clean(req.getParameter("action"));
        int eventId = Web.intParam(req, "eventId", 0);
        int typeId = Web.intParam(req, "typeId", 0);
        String label = Web.clean(req.getParameter("label"));
        try {
            if ("delete".equals(action)) {
                TicketTypeDAO.delete(typeId, eventId);
                Web.flash(req, "success", "Ticket type removed.");
            } else if ("add".equals(action) || "update".equals(action)) {
                BigDecimal price;
                int seats;
                try {
                    String p = Web.clean(req.getParameter("price"));
                    price = new BigDecimal(p.isEmpty() ? "0" : p);
                    seats = Integer.parseInt(Web.clean(req.getParameter("seats")));
                } catch (NumberFormatException e) {
                    throw new AppException("Enter a valid price and a whole number of seats.");
                }
                if (label.isEmpty() || label.length() > 60 || price.signum() < 0 || seats < 1 || seats > 100000) {
                    throw new AppException("A ticket type needs a name, a price of 0 or more, and at least 1 seat.");
                }
                if ("add".equals(action)) {
                    TicketTypeDAO.add(eventId, label, price, seats);
                    Web.flash(req, "success", "Ticket type added.");
                } else {
                    TicketTypeDAO.update(typeId, eventId, label, price, seats);
                    Web.flash(req, "success", "Ticket type updated.");
                }
            }
        } catch (AppException e) {
            Web.flash(req, "error", e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        res.sendRedirect(req.getContextPath() + "/admin/event?id=" + eventId);
    }
}
