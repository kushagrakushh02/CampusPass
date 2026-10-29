package com.campuspass.servlet;

import com.campuspass.dao.EventDAO;
import com.campuspass.dao.RegistrationDAO;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Home page: hero ticket plus the searchable list of events. */
@WebServlet(urlPatterns = {"", "/events"})
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String q = Web.clean(req.getParameter("q"));
        String category = Web.clean(req.getParameter("category"));
        boolean past = "past".equals(req.getParameter("show"));
        try {
            req.setAttribute("events", EventDAO.search(q, category, !past));
            req.setAttribute("categories", EventDAO.categories());
            req.setAttribute("heroEvent", EventDAO.nextUpcoming());
            req.setAttribute("upcomingCount", EventDAO.countUpcoming());
            req.setAttribute("ticketsBooked", RegistrationDAO.countConfirmedTickets());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("q", q);
        req.setAttribute("category", category);
        req.setAttribute("show", past ? "past" : "upcoming");
        Web.forward(req, res, "home.jsp");
    }
}
