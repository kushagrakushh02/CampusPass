# CampusPass: Event Registration and Ticketing System

A web application where organisers publish events and students book tickets online.
Built with **Servlet, JSP (JSTL) and JDBC** on **Apache Tomcat 10.1** with **MySQL 8**.

## What it does

- Sign up, log in, log out (session based, PBKDF2 password hashes, CSRF token on every form)
- Browse, search and filter events; see live seat counts
- Book tickets of different categories (Student, General, VIP ...); get a ticket with a unique code
- Seat booking is **one database transaction**, so the last seat can never be sold twice
- One live booking per person per event (enforced by the database as well)
- See booking history, print a ticket, cancel a booking (seats go back)
- Organiser area: dashboard, add / edit / delete events, manage ticket types, see registrations, export CSV

## What you need

| Tool | Version |
|------|---------|
| JDK | 17 or newer |
| Maven | 3.8+ |
| Apache Tomcat | **10.1.x** (Jakarta namespace, not Tomcat 9) |
| MySQL | 8.0.16 or newer |

## Run it

1. **Create the database**
   ```
   mysql -u root -p < database/schema.sql
   mysql -u root -p campuspass < database/sample_data.sql      # optional sample events
   ```
2. **Set your MySQL login** in `src/main/resources/db.properties` (`db.user`, `db.password`).
3. **Build the WAR**
   ```
   mvn clean package
   ```
4. **Deploy**: copy `target/campuspass.war` into Tomcat's `webapps/` folder and start Tomcat
   (or add the project to Eclipse / IntelliJ with a Tomcat 10.1 server).
5. Open <http://localhost:8080/campuspass/>

On first start the app creates an organiser account (from `db.properties`):

- Email: `admin@campuspass.com`
- Password: `Admin@123`  (change it in `db.properties` before first start if you like)

Students create their own accounts with **Create account**.

## Check that overselling is impossible

`com.campuspass.tools.ConcurrencyCheck` has a `main` method. Run it from your IDE after the database is set up:

```
args: 5 30        (5 seats, 30 people booking at the same instant)
```

It prints how many bookings succeeded and how many seats are left, then cleans up after itself.
Expected: 5 bookings OK, 25 refused, `seats_left = 0`, `RESULT: PASS`.

## Project layout

```
src/main/java/com/campuspass
  model/      User, Event, TicketType, Registration
  dao/        UserDAO, EventDAO, TicketTypeDAO, RegistrationDAO   (all JDBC lives here)
  servlet/    controllers; servlet/admin/ for organiser pages
  filter/     AccessFilter  (login check, role check, CSRF, security headers)
  listener/   AppInitListener (creates the admin account)
  util/       DBUtil, PasswordUtil, Web, AppException
  tools/      ConcurrencyCheck
src/main/webapp
  WEB-INF/views/   JSP pages (not reachable by URL, only through servlets)
  assets/          style.css, app.js
database/          schema.sql, sample_data.sql
```

## Request flow (MVC Model 2)

Browser -> Servlet (validates input) -> DAO (JDBC, PreparedStatement) -> MySQL;
the servlet puts the result in the request and forwards to a JSP that draws the page.
Servlets never print HTML and JSPs never touch the database.
