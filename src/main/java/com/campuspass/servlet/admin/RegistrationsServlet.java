package com.campuspass.servlet.admin;

import com.campuspass.dao.EventDAO;
import com.campuspass.dao.RegistrationDAO;
import com.campuspass.model.Event;
import com.campuspass.model.Registration;
import com.campuspass.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/** Everyone who registered for one event; add ?format=csv to download the same list as a spreadsheet. */
@WebServlet("/admin/registrations")
public class RegistrationsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int eventId = Web.intParam(req, "eventId", 0);
        try {
            Event event = EventDAO.findById(eventId);
            if (event == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            List<Registration> list = RegistrationDAO.listByEvent(eventId);
            if ("csv".equals(req.getParameter("format"))) {
                writeCsv(res, event, list);
                return;
            }
            req.setAttribute("event", event);
            req.setAttribute("registrations", list);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        Web.forward(req, res, "admin/registrations.jsp");
    }

    private void writeCsv(HttpServletResponse res, Event event, List<Registration> list) throws IOException {
        res.setContentType("text/csv");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setHeader("Content-Disposition", "attachment; filename=\"registrations-event-" + event.getId() + ".csv\"");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        PrintWriter out = res.getWriter();
        out.print('\uFEFF');                                        // BOM so Excel reads UTF-8 correctly
        out.print("Ticket code,Name,Email,Ticket type,Quantity,Amount,Status,Booked at\r\n");
        for (Registration r : list) {
            out.print(csv(r.getTicketCode()) + "," + csv(r.getUserName()) + "," + csv(r.getUserEmail()) + ","
                    + csv(r.getTypeLabel()) + "," + r.getQuantity() + "," + r.getTotal().toPlainString() + ","
                    + r.getStatus() + "," + fmt.format(r.getBookedAt()) + "\r\n");
        }
    }

    /** Quotes a CSV field and defuses spreadsheet formulas (a name like =CMD() would otherwise run in Excel). */
    private static String csv(String s) {
        if (s == null) return "";
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
