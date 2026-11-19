package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Student;
import util.DBUtil;

/**
 * All SQL for the STUDENT table lives here.
 * The servlet never sees a Connection, a ResultSet, or a SQL string.
 */
public class StudentDAO {

    private static final String COLUMNS = "NAME, EMAIL, PHONENO";

    private static final String SQL_FIND_ALL
            = "SELECT " + COLUMNS + " FROM STUDENT";
    private static final String SQL_FIND_BY_NAME
            = "SELECT " + COLUMNS + " FROM STUDENT WHERE NAME = ?";
    private static final String SQL_SEARCH_BY_NAME
            = "SELECT " + COLUMNS + " FROM STUDENT WHERE NAME LIKE ?";
    private static final String SQL_INSERT
            = "INSERT INTO STUDENT (NAME, EMAIL, PHONENO) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE
            = "UPDATE STUDENT SET EMAIL = ?, PHONENO = ? WHERE NAME = ?";
    private static final String SQL_DELETE
            = "DELETE FROM STUDENT WHERE NAME = ?";

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_FIND_ALL);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                students.add(toStudent(rs));
            }
        }
        return students;
    }

    public List<Student> searchByName(String keyword) throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_SEARCH_BY_NAME)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(toStudent(rs));
                }
            }
        }
        return students;
    }

    /** @return the student, or null when no row matches */
    public Student findByName(String name) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? toStudent(rs) : null;
            }
        }
    }

    public void insert(Student student) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
            bind(pstmt, student);
            pstmt.executeUpdate();
        }
    }

    public void update(String originalName, String newEmail, String newPhoneNo) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {
            pstmt.setString(1, newEmail);
            pstmt.setString(2, newPhoneNo);
            pstmt.setString(3, originalName);
            pstmt.executeUpdate();
        }
    }

    public void delete(String name) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    /**
     * Inserts two rows as one unit of work: both succeed or neither does.
     * Used by the transaction demo page.
     */
    public void insertBoth(Student first, Student second) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
                bind(pstmt, first);
                pstmt.executeUpdate();

                bind(pstmt, second);
                pstmt.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void bind(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getName());
        pstmt.setString(2, student.getEmail());
        pstmt.setString(3, student.getPhoneNo());
    }

    private Student toStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("NAME"),
                rs.getString("EMAIL"),
                rs.getString("PHONENO"));
    }
}