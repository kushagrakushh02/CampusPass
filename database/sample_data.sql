-- Sample events so the home page is not empty. Dates are relative to "now",
-- so the events are always upcoming whenever you import this file.
-- Run after schema.sql:  mysql -u root -p campuspass < database/sample_data.sql
-- (The admin user is created automatically when Tomcat starts the app.)
USE campuspass;

INSERT INTO events (title, category, venue, event_date, description) VALUES
('Code Sprint 2026', 'Technology', 'UIC Block, Seminar Hall 2', DATE_ADD(NOW(), INTERVAL 6 DAY),
 'A six-hour team coding contest. Teams of two solve problems on data structures, databases and web development. Bring your laptop and charger; snacks are on us.'),
('Rangmanch Cultural Night', 'Cultural', 'Open Air Theatre, Main Campus', DATE_ADD(NOW(), INTERVAL 11 DAY),
 'Dance, music and theatre performances by student societies, followed by an open mic. Gates open an hour before the first act.'),
('Java Web Workshop: Servlets to Deployment', 'Workshop', 'Computer Lab 4, UIC', DATE_ADD(NOW(), INTERVAL 14 DAY),
 'A hands-on day building a small web app with Servlets, JSP and JDBC, then deploying it on Tomcat. Basic Java knowledge is enough.'),
('Inter-Department Cricket League', 'Sports', 'University Cricket Ground', DATE_ADD(NOW(), INTERVAL 18 DAY),
 'Knock-out matches between departments. Register as a spectator to get a seat in the pavilion and free refreshments.'),
('Careers in Cloud: Alumni Talk', 'Seminar', 'Auditorium B', DATE_ADD(NOW(), INTERVAL 21 DAY),
 'Three alumni working in cloud and DevOps share how they got their first roles, what interviews looked like, and what they would learn first today.'),
('Placement Prep Bootcamp', 'Career', 'Lecture Hall 12', DATE_ADD(NOW(), INTERVAL 27 DAY),
 'Aptitude, group discussion and resume clinics in one weekend. Limited seats so every participant gets feedback.'),
('Winter Fest 2026', 'Fest', 'Central Lawns', DATE_ADD(NOW(), INTERVAL 40 DAY),
 'Food stalls, live band, games and a night market run by student clubs. Passes include entry to all stalls.'),
('Freshers Welcome Meet', 'Cultural', 'Auditorium A', DATE_SUB(NOW(), INTERVAL 20 DAY),
 'Welcome event for the new MCA batch. This one has already happened, so it appears under past events.');

-- Ticket types: (event title, label, price, seats)
INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Student', 0, 120, 120 FROM events WHERE title = 'Code Sprint 2026';
INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'External participant', 299, 30, 30 FROM events WHERE title = 'Code Sprint 2026';

INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'General', 100, 400, 400 FROM events WHERE title = 'Rangmanch Cultural Night';
INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Front row', 250, 60, 60 FROM events WHERE title = 'Rangmanch Cultural Night';

INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Participant', 199, 40, 40 FROM events WHERE title LIKE 'Java Web Workshop%';

INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Spectator', 0, 250, 250 FROM events WHERE title = 'Inter-Department Cricket League';
INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Pavilion seat', 150, 50, 50 FROM events WHERE title = 'Inter-Department Cricket League';

INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Open entry', 0, 180, 180 FROM events WHERE title LIKE 'Careers in Cloud%';

INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Weekend pass', 349, 80, 80 FROM events WHERE title = 'Placement Prep Bootcamp';

INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Day pass', 199, 500, 500 FROM events WHERE title = 'Winter Fest 2026';
INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'VIP pass', 599, 40, 40 FROM events WHERE title = 'Winter Fest 2026';

INSERT INTO ticket_types (event_id, label, price, total_seats, seats_left)
SELECT event_id, 'Fresher', 0, 200, 200 FROM events WHERE title = 'Freshers Welcome Meet';
