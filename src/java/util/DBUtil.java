/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBUtil {

    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        Properties p = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            p.load(in);
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        URL = p.getProperty("db.url");
        USER = p.getProperty("db.user");
        PASS = p.getProperty("db.password");
    }

    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
