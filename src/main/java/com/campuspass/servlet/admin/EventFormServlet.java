package com.campuspass.servlet.admin;

import com.campuspass.dao.EventDAO;
import com.campuspass.model.Event;
import com.campuspass.model.TicketType;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Add or edit an event. GET shows the form, POST validates and saves. */
@WebServlet("/admin/event")
public class EventFormServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static final List<String> CATEGORIES =
            Arrays.asList("Technology", "Cultural", "Sports", "Workshop", "Seminar", "Fest", "Career", "Music", "General");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int id = Web.intParam(req, "id", 0);
        Event event = new Event();
        if (id > 0) {
            try {
                event = EventDAO.findById(id);
            } catch (SQLException e) {
                throw new ServletException(e);
            }
            if (event == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        req.setAttribute("event", event);
        req.setAttribute("categories", CATEGORIES);
        Web.forward(req, res, "admin/event-form.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int id = Web.intParam(req, "id", 0);
        Event ev = new Event();
        ev.setId(id);
        ev.setTitle(Web.clean(req.getParameter("title")));
        ev.setCategory(Web.clean(req.getParameter("category")));
        ev.setVenue(Web.clean(req.getParameter("venue")));
        ev.setDescription(Web.clean(req.getParameter("description")));
        ev.setStatus("CANCELLED".equals(req.getParameter("status")) ? "CANCELLED" : "ACTIVE");

        String error = null;
        LocalDateTime when = null;
        try {
            when = LocalDateTime.parse(Web.clean(req.getParameter("eventDate")));
            ev.setEventDate(Timestamp.valueOf(when));
        } catch (DateTimeParseException e) {
            error = "Please choose a valid date and time.";
        }

        List<TicketType> types = new ArrayList<>();
        if (id == 0 && error == null) {
            String[] labels = req.getParameterValues("typeLabel");
            String[] prices = req.getParameterValues("typePrice");
            String[] seats = req.getParameterValues("typeSeats");
            int n = labels == null ? 0 : labels.length;
            try {
                for (int i = 0; i < n; i++) {
                    String label = Web.clean(labels[i]);
                    if (label.isEmpty()) continue;                       // skip blank rows
                    TicketType t = new TicketType();
                    t.setLabel(label);
                    t.setPrice(new BigDecimal(Web.clean(prices[i]).isEmpty() ? "0" : Web.clean(prices[i])));
                    t.setTotalSeats(Integer.parseInt(Web.clean(seats[i])));
                    if (t.getPrice().signum() < 0 || t.getTotalSeats() < 1 || t.getTotalSeats() > 100000
                            || label.length() > 60) {
                        throw new NumberFormatException();
                    }
                    types.add(t);
                }
            } catch (RuntimeException e) {
                error = "Check the ticket types: price must be 0 or more and seats must be a whole number from 1 upwards.";
            }
            ev.setTicketTypes(types);
            if (error == null && types.isEmpty()) error = "Add at least one ticket type.";
        }

        if (error == null && (ev.getTitle().isEmpty() || ev.getTitle().length() > 150)) {
            error = "Give the event a title (up to 150 characters).";
        }
        if (error == null && (ev.getVenue().isEmpty() || ev.getVenue().length() > 150)) {
            error = "Please enter the venue.";
        }
        if (error == null && (ev.getCategory().isEmpty() || ev.getCategory().length() > 40)) {
            error = "Please choose a category.";
        }
        if (error == null && id == 0 && when.isBefore(LocalDateTime.now())) {
            error = "A new event needs a date in the future.";
        }

        if (error != null) {                                  // show the form again with what was typed
            req.setAttribute("error", error);
            req.setAttribute("event", ev);
            req.setAttribute("categories", CATEGORIES);
            Web.forward(req, res, "admin/event-form.jsp");
            return;
        }

        try {
            if (id == 0) {
                int newId = EventDAO.create(ev, types);
                Web.flash(req, "success", "Event published. Add more ticket types any time.");
                res.sendRedirect(req.getContextPath() + "/admin/event?id=" + newId);
            } else {
                EventDAO.update(ev);
                Web.flash(req, "success", "Event details saved.");
                res.sendRedirect(req.getContextPath() + "/admin/event?id=" + id);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
