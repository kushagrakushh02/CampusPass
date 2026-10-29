package com.campuspass.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Reads db.properties once and hands out JDBC connections. */
public final class DBUtil {

    private static final Properties CFG = new Properties();

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties was not found on the classpath");
            }
            CFG.load(in);
            Class.forName(CFG.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CFG.getProperty("db.url"), CFG.getProperty("db.user"), CFG.getProperty("db.password"));
    }

    public static String get(String key, String fallback) {
        return CFG.getProperty(key, fallback);
    }
}
