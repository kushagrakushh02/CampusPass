package com.campuspass.servlet;

import com.campuspass.dao.RegistrationDAO;
import com.campuspass.model.Registration;
import com.campuspass.model.User;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/ticket")
public class TicketServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = Web.currentUser(req);
        String code = Web.clean(req.getParameter("code")).toUpperCase();
        try {
            Registration reg = RegistrationDAO.findByCode(code);
            // owners and admins may open a ticket; for anyone else it simply "does not exist"
            if (reg == null || (reg.getUserId() != user.getId() && !user.isAdmin())) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("reg", reg);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        Web.forward(req, res, "ticket.jsp");
    }
}
