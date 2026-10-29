package com.campuspass.dao;

import com.campuspass.model.Registration;
import com.campuspass.util.AppException;
import com.campuspass.util.DBUtil;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class RegistrationDAO {

    public static final int MAX_PER_BOOKING = 5;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";   // no 0/O/1/I to avoid mix-ups
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String BASE =
        "SELECT r.reg_id, r.user_id, r.event_id, r.type_id, r.quantity, r.ticket_code, r.status, r.booked_at, "
      + "e.title, e.venue, e.category, e.event_date, t.label, t.price, u.name AS user_name, u.email AS user_email "
      + "FROM registrations r "
      + "JOIN events e ON e.event_id = r.event_id "
      + "JOIN ticket_types t ON t.type_id = r.type_id "
      + "JOIN users u ON u.user_id = r.user_id ";

    private RegistrationDAO() { }

    // ------------------------------------------------------------------------------------
    //  Booking: check, reduce seats and save the registration as ONE transaction
    // ------------------------------------------------------------------------------------

    /**
     * Books {@code qty} tickets and returns the new ticket code.
     * The seat check and the seat reduction are a single conditional UPDATE. InnoDB locks the
     * row while it runs, so when two people go for the last seat, one UPDATE matches the row and
     * the other matches nothing (0 rows) and is rolled back.
     */
    public static String book(int userId, int eventId, int typeId, int qty) throws SQLException, AppException {
        if (qty < 1 || qty > MAX_PER_BOOKING) {
            throw new AppException("You can book between 1 and " + MAX_PER_BOOKING + " tickets at a time.");
        }
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                ensureEventIsOpen(con, eventId, typeId);
                if (hasActiveBooking(con, userId, eventId)) {
                    throw new AppException("You already have a booking for this event. Cancel it first if you want to change it.");
                }

                String reduce = "UPDATE ticket_types SET seats_left = seats_left - ? "
                              + "WHERE type_id = ? AND event_id = ? AND seats_left >= ?";
                try (PreparedStatement upd = con.prepareStatement(reduce)) {
                    upd.setInt(1, qty);
                    upd.setInt(2, typeId);
                    upd.setInt(3, eventId);
                    upd.setInt(4, qty);
                    if (upd.executeUpdate() == 0) {
                        throw new AppException("Sorry, there are not enough seats left in this category.");
                    }
                }

                String code = newTicketCode();
                String insert = "INSERT INTO registrations (user_id, event_id, type_id, quantity, ticket_code, status) "
                              + "VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
                try (PreparedStatement ins = con.prepareStatement(insert)) {
                    ins.setInt(1, userId);
                    ins.setInt(2, eventId);
                    ins.setInt(3, typeId);
                    ins.setInt(4, qty);
                    ins.setString(5, code);
                    ins.executeUpdate();
                }
                con.commit();
                return code;
            } catch (SQLIntegrityConstraintViolationException dup) {
                // two clicks of the same button at once: the unique key on active_key caught the second one
                con.rollback();
                throw new AppException("You already have a booking for this event.");
            } catch (AppException | SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private static void ensureEventIsOpen(Connection con, int eventId, int typeId) throws SQLException, AppException {
        String sql = "SELECT e.status, e.event_date FROM events e "
                   + "JOIN ticket_types t ON t.event_id = e.event_id WHERE e.event_id = ? AND t.type_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new AppException("Please choose a ticket type for this event.");
                if ("CANCELLED".equals(rs.getString("status"))) throw new AppException("This event has been cancelled.");
                if (rs.getTimestamp("event_date").before(new Timestamp(System.currentTimeMillis()))) {
                    throw new AppException("Registration is closed because this event has already started.");
                }
            }
        }
    }

    private static boolean hasActiveBooking(Connection con, int userId, int eventId) throws SQLException {
        String sql = "SELECT 1 FROM registrations WHERE user_id = ? AND event_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static String newTicketCode() {
        StringBuilder sb = new StringBuilder("CP-");
        for (int i = 0; i < 8; i++) sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        return sb.toString();
    }

    // ------------------------------------------------------------------------------------
    //  Cancellation: mark CANCELLED and give the seats back, again in one transaction
    // ------------------------------------------------------------------------------------

    public static void cancel(int regId, int userId) throws SQLException, AppException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                int typeId;
                int qty;
                String lock = "SELECT r.type_id, r.quantity, r.status, e.event_date FROM registrations r "
                            + "JOIN events e ON e.event_id = r.event_id "
                            + "WHERE r.reg_id = ? AND r.user_id = ? FOR UPDATE OF r";
                try (PreparedStatement ps = con.prepareStatement(lock)) {
                    ps.setInt(1, regId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new AppException("We could not find that booking.");
                        if (!"CONFIRMED".equals(rs.getString("status"))) throw new AppException("This booking is already cancelled.");
                        if (rs.getTimestamp("event_date").before(new Timestamp(System.currentTimeMillis()))) {
                            throw new AppException("This event has already started, so the booking can no longer be cancelled.");
                        }
                        typeId = rs.getInt("type_id");
                        qty = rs.getInt("quantity");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE registrations SET status = 'CANCELLED', cancelled_at = NOW() WHERE reg_id = ?")) {
                    ps.setInt(1, regId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE ticket_types SET seats_left = seats_left + ? WHERE type_id = ?")) {
                    ps.setInt(1, qty);
                    ps.setInt(2, typeId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (AppException | SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // ------------------------------------------------------------------------------------
    //  Read queries
    // ------------------------------------------------------------------------------------

    public static List<Registration> listByUser(int userId) throws SQLException {
        return query(BASE + "WHERE r.user_id = ? ORDER BY r.booked_at DESC, r.reg_id DESC", userId);
    }

    public static List<Registration> listByEvent(int eventId) throws SQLException {
        return query(BASE + "WHERE r.event_id = ? ORDER BY r.booked_at DESC, r.reg_id DESC", eventId);
    }

    public static Registration findActive(int userId, int eventId) throws SQLException {
        List<Registration> list = query(BASE + "WHERE r.user_id = ? AND r.event_id = ? AND r.status = 'CONFIRMED'", userId, eventId);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Registration findByCode(String code) throws SQLException {
        List<Registration> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(BASE + "WHERE r.ticket_code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list.isEmpty() ? null : list.get(0);
    }

    /** Total tickets currently held by people (cancelled bookings are not counted). */
    public static int countConfirmedTickets() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(quantity), 0) FROM registrations WHERE status = 'CONFIRMED'")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static List<Registration> query(String sql, int... params) throws SQLException {
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setInt(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                List<Registration> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    private static Registration map(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getInt("reg_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setEventId(rs.getInt("event_id"));
        r.setTypeId(rs.getInt("type_id"));
        r.setQuantity(rs.getInt("quantity"));
        r.setTicketCode(rs.getString("ticket_code"));
        r.setStatus(rs.getString("status"));
        r.setBookedAt(rs.getTimestamp("booked_at"));
        r.setEventTitle(rs.getString("title"));
        r.setVenue(rs.getString("venue"));
        r.setCategory(rs.getString("category"));
        r.setEventDate(rs.getTimestamp("event_date"));
        r.setTypeLabel(rs.getString("label"));
        r.setUnitPrice(rs.getBigDecimal("price"));
        r.setUserName(rs.getString("user_name"));
        r.setUserEmail(rs.getString("user_email"));
        return r;
    }
}
