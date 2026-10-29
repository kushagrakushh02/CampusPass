package com.campuspass.tools;

import com.campuspass.dao.RegistrationDAO;
import com.campuspass.util.AppException;
import com.campuspass.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stand-alone check for the "last seat" problem. Run it from your IDE (it has a main method).
 *
 *   args: [seats] [buyers]      default: 5 seats, 30 buyers
 *
 * It creates a throw-away event with {seats} seats and {buyers} users, makes all users try to
 * book one ticket at exactly the same moment, then prints how many bookings succeeded and how
 * many seats are left. Correct behaviour: successes == seats and seats left == 0.
 * Everything it creates is deleted again at the end.
 */
public class ConcurrencyCheck {

    public static void main(String[] args) throws Exception {
        final int seats = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        final int buyers = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        final String tag = "cc-test-" + System.currentTimeMillis();

        int eventId;
        int typeId;
        List<Integer> userIds = new ArrayList<>();

        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO events (title, category, venue, event_date, description) VALUES (?, 'General', 'Test hall', ?, 'temporary')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Concurrency test " + tag);
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 86_400_000L));
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) { k.next(); eventId = k.getInt(1); }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left) VALUES (?, 'Test', 0, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, eventId);
                ps.setInt(2, seats);
                ps.setInt(3, seats);
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) { k.next(); typeId = k.getInt(1); }
            }
            for (int i = 0; i < buyers; i++) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, 'x', 'USER')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Tester " + i);
                    ps.setString(2, tag + "-" + i + "@example.test");
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); userIds.add(k.getInt(1)); }
                }
            }
        }

        final int fEvent = eventId;
        final int fType = typeId;
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger refused = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(buyers);
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(buyers);

        for (final int uid : userIds) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();                                   // every thread waits for the same starting gun
                    RegistrationDAO.book(uid, fEvent, fType, 1);
                    ok.incrementAndGet();
                } catch (AppException e) {
                    refused.incrementAndGet();                    // "not enough seats left"
                } catch (SQLException | InterruptedException e) {
                    failed.incrementAndGet();
                    System.err.println("Unexpected: " + e);
                }
            });
        }
        ready.await();
        long t0 = System.nanoTime();
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS);
        long ms = (System.nanoTime() - t0) / 1_000_000;

        int seatsLeft;
        int confirmed;
        try (Connection con = DBUtil.getConnection(); Statement st = con.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT seats_left FROM ticket_types WHERE type_id = " + typeId)) {
                rs.next();
                seatsLeft = rs.getInt(1);
            }
            try (ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(quantity),0) FROM registrations WHERE event_id = "
                    + eventId + " AND status = 'CONFIRMED'")) {
                rs.next();
                confirmed = rs.getInt(1);
            }
            // clean up everything this test created
            st.executeUpdate("DELETE FROM registrations WHERE event_id = " + eventId);
            st.executeUpdate("DELETE FROM events WHERE event_id = " + eventId);
            st.executeUpdate("DELETE FROM users WHERE email LIKE '" + tag + "-%'");
        }

        System.out.println("Seats available : " + seats);
        System.out.println("Buyers at once  : " + buyers);
        System.out.println("Bookings OK     : " + ok.get());
        System.out.println("Refused (full)  : " + refused.get());
        System.out.println("Errors          : " + failed.get());
        System.out.println("seats_left now  : " + seatsLeft);
        System.out.println("Tickets in DB   : " + confirmed);
        System.out.println("Time taken      : " + ms + " ms");
        boolean pass = ok.get() == seats && seatsLeft == 0 && confirmed == seats && failed.get() == 0;
        System.out.println(pass ? "RESULT: PASS - no overselling" : "RESULT: FAIL - check the booking code");
    }
}
