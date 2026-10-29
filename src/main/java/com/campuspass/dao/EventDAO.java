package com.campuspass.dao;

import com.campuspass.model.Event;
import com.campuspass.model.TicketType;
import com.campuspass.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class EventDAO {

    /** Every event query starts from this: the event plus its seat, sales and price figures. */
    private static final String BASE =
        "SELECT e.event_id, e.title, e.category, e.venue, e.event_date, e.description, e.status, "
      + "(SELECT COALESCE(SUM(t.total_seats), 0) FROM ticket_types t WHERE t.event_id = e.event_id) AS total_seats, "
      + "(SELECT COALESCE(SUM(t.seats_left), 0)  FROM ticket_types t WHERE t.event_id = e.event_id) AS seats_left, "
      + "(SELECT COALESCE(MIN(t.price), 0)       FROM ticket_types t WHERE t.event_id = e.event_id) AS min_price, "
      + "(SELECT COALESCE(SUM(r.quantity), 0) FROM registrations r "
      + "   WHERE r.event_id = e.event_id AND r.status = 'CONFIRMED') AS tickets_sold, "
      + "(SELECT COALESCE(SUM(r.quantity * t.price), 0) FROM registrations r "
      + "   JOIN ticket_types t ON t.type_id = r.type_id "
      + "   WHERE r.event_id = e.event_id AND r.status = 'CONFIRMED') AS revenue "
      + "FROM events e ";

    private EventDAO() { }

    private static Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("event_id"));
        e.setTitle(rs.getString("title"));
        e.setCategory(rs.getString("category"));
        e.setVenue(rs.getString("venue"));
        e.setEventDate(rs.getTimestamp("event_date"));
        e.setDescription(rs.getString("description"));
        e.setStatus(rs.getString("status"));
        e.setTotalSeats(rs.getInt("total_seats"));
        e.setSeatsLeft(rs.getInt("seats_left"));
        e.setMinPrice(rs.getBigDecimal("min_price"));
        e.setTicketsSold(rs.getInt("tickets_sold"));
        e.setRevenue(rs.getBigDecimal("revenue"));
        return e;
    }

    /** Public listing: active events only, upcoming or past, with optional text and category filters. */
    public static List<Event> search(String q, String category, boolean upcoming) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE);
        sql.append("WHERE e.status = 'ACTIVE' AND e.event_date ").append(upcoming ? ">= NOW()" : "< NOW()");
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isEmpty()) {
            sql.append(" AND (e.title LIKE ? OR e.venue LIKE ? OR e.description LIKE ?)");
            String like = "%" + q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND e.category = ?");
            args.add(category);
        }
        sql.append(upcoming ? " ORDER BY e.event_date ASC" : " ORDER BY e.event_date DESC");

        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) ps.setObject(i + 1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    /** Admin listing: every event, newest date first, including cancelled ones. */
    public static List<Event> listAll() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(BASE + "ORDER BY e.event_date DESC")) {
            List<Event> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    /** One event together with its ticket types, or null if the id does not exist. */
    public static Event findById(int id) throws SQLException {
        Event e = null;
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(BASE + "WHERE e.event_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) e = map(rs);
            }
        }
        if (e != null) {
            e.setTicketTypes(TicketTypeDAO.listByEvent(id));
            e.recalcFromTypes();
        }
        return e;
    }

    /** The soonest bookable event; shown on the home-page hero. */
    public static Event nextUpcoming() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(BASE + "WHERE e.status = 'ACTIVE' AND e.event_date >= NOW() "
                                                 + "ORDER BY e.event_date ASC LIMIT 1")) {
            return rs.next() ? map(rs) : null;
        }
    }

    public static List<String> categories() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT category FROM events WHERE status = 'ACTIVE' ORDER BY category")) {
            List<String> list = new ArrayList<>();
            while (rs.next()) list.add(rs.getString(1));
            return list;
        }
    }

    public static int countUpcoming() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM events WHERE status = 'ACTIVE' AND event_date >= NOW()")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Saves a new event and its ticket types in one transaction; returns the new event id. */
    public static int create(Event e, List<TicketType> types) throws SQLException {
        String sql = "INSERT INTO events (title, category, venue, event_date, description, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getTitle());
                ps.setString(2, e.getCategory());
                ps.setString(3, e.getVenue());
                ps.setTimestamp(4, e.getEventDate());
                ps.setString(5, e.getDescription());
                ps.setString(6, e.getStatus());
                ps.executeUpdate();
                int id;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
                TicketTypeDAO.insertAll(con, id, types);
                con.commit();
                return id;
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public static void update(Event e) throws SQLException {
        String sql = "UPDATE events SET title = ?, category = ?, venue = ?, event_date = ?, description = ?, status = ? "
                   + "WHERE event_id = ?";
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getCategory());
            ps.setString(3, e.getVenue());
            ps.setTimestamp(4, e.getEventDate());
            ps.setString(5, e.getDescription());
            ps.setString(6, e.getStatus());
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Deletes an event that has no registrations. If people have already booked, the event is
     * marked CANCELLED instead so their ticket history is not lost.
     * @return true if the row was deleted, false if it was only cancelled
     */
    public static boolean deleteOrCancel(int id) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                int bookings;
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM registrations WHERE event_id = ?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        bookings = rs.getInt(1);
                    }
                }
                boolean deleted = (bookings == 0);
                String sql = deleted ? "DELETE FROM events WHERE event_id = ?"
                                     : "UPDATE events SET status = 'CANCELLED' WHERE event_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                con.commit();
                return deleted;
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
}
