package com.campuspass.listener;

import com.campuspass.dao.UserDAO;
import com.campuspass.util.DBUtil;
import com.campuspass.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Creates the default admin account on start-up, so no password hash has to be pasted into SQL. */
@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String email = DBUtil.get("admin.email", "admin@campuspass.com").toLowerCase();
        String password = DBUtil.get("admin.password", "Admin@123");
        try {
            if (UserDAO.findByEmail(email) == null) {
                UserDAO.create("Administrator", email, PasswordUtil.hash(password), "ADMIN");
                sce.getServletContext().log("CampusPass: default admin created (" + email + ")");
            }
        } catch (Throwable t) {
            sce.getServletContext().log("CampusPass: could not check the admin user. Is MySQL running and schema.sql imported? "
                    + t.getMessage());
        }
    }
}
