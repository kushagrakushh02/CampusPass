package com.campuspass.dao;

import com.campuspass.model.TicketType;
import com.campuspass.util.AppException;
import com.campuspass.util.DBUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class TicketTypeDAO {

    private TicketTypeDAO() { }

    public static List<TicketType> listByEvent(int eventId) throws SQLException {
        String sql = "SELECT type_id, event_id, label, price, total_seats, seats_left "
                   + "FROM ticket_types WHERE event_id = ? ORDER BY price, type_id";
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                List<TicketType> list = new ArrayList<>();
                while (rs.next()) {
                    TicketType t = new TicketType();
                    t.setId(rs.getInt("type_id"));
                    t.setEventId(rs.getInt("event_id"));
                    t.setLabel(rs.getString("label"));
                    t.setPrice(rs.getBigDecimal("price"));
                    t.setTotalSeats(rs.getInt("total_seats"));
                    t.setSeatsLeft(rs.getInt("seats_left"));
                    list.add(t);
                }
                return list;
            }
        }
    }

    /** Used by EventDAO.create() so the event and its ticket types are saved in one transaction. */
    static void insertAll(Connection con, int eventId, List<TicketType> types) throws SQLException {
        String sql = "INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (TicketType t : types) {
                ps.setInt(1, eventId);
                ps.setString(2, t.getLabel());
                ps.setBigDecimal(3, t.getPrice());
                ps.setInt(4, t.getTotalSeats());
                ps.setInt(5, t.getTotalSeats());       // nothing sold yet, so every seat is free
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static void add(int eventId, String label, BigDecimal price, int seats) throws SQLException {
        List<TicketType> one = new ArrayList<>();
        TicketType t = new TicketType();
        t.setLabel(label);
        t.setPrice(price);
        t.setTotalSeats(seats);
        one.add(t);
        try (Connection con = DBUtil.getConnection()) {
            insertAll(con, eventId, one);
        }
    }

    /**
     * Changes label, price and capacity. Seats already sold are kept: the new total may not be
     * lower than what has been sold. In MySQL the SET list runs left to right, so seats_left is
     * adjusted first (using the old total_seats) and total_seats is overwritten afterwards.
     */
    public static void update(int typeId, int eventId, String label, BigDecimal price, int newTotal)
            throws SQLException, AppException {
        String sql = "UPDATE ticket_types SET label = ?, price = ?, "
                   + "seats_left = seats_left + (? - total_seats), total_seats = ? "
                   + "WHERE type_id = ? AND event_id = ? AND ? >= (total_seats - seats_left)";
        try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, label);
            ps.setBigDecimal(2, price);
            ps.setInt(3, newTotal);
            ps.setInt(4, newTotal);
            ps.setInt(5, typeId);
            ps.setInt(6, eventId);
            ps.setInt(7, newTotal);
            if (ps.executeUpdate() == 0) {
                throw new AppException("Capacity cannot be lower than the tickets already sold.");
            }
        }
    }

    public static void delete(int typeId, int eventId) throws SQLException, AppException {
        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement chk = con.prepareStatement("SELECT COUNT(*) FROM registrations WHERE type_id = ?")) {
                chk.setInt(1, typeId);
                try (ResultSet rs = chk.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        throw new AppException("This ticket type already has bookings, so it cannot be deleted.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ticket_types WHERE type_id = ? AND event_id = ?")) {
                ps.setInt(1, typeId);
                ps.setInt(2, eventId);
                ps.executeUpdate();
            }
        }
    }
}
