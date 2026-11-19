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
 *
 * The primary key is ID, an identity column maintained by the database:
 * inserts never mention it, and the value comes back on select.
 */
public class StudentDAO {

    private static final String COLUMNS = "ID, NAME, EMAIL, PHONENO";

    private static final String SQL_FIND_ALL
            = "SELECT " + COLUMNS + " FROM STUDENT ORDER BY ID";
    private static final String SQL_FIND_BY_ID
            = "SELECT " + COLUMNS + " FROM STUDENT WHERE ID = ?";
    private static final String SQL_SEARCH_BY_NAME
            = "SELECT " + COLUMNS + " FROM STUDENT WHERE LOWER(NAME) LIKE LOWER(?) ORDER BY ID";
    private static final String SQL_INSERT
            = "INSERT INTO STUDENT (NAME, EMAIL, PHONENO) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE
            = "UPDATE STUDENT SET NAME = ?, EMAIL = ?, PHONENO = ? WHERE ID = ?";
    private static final String SQL_DELETE
            = "DELETE FROM STUDENT WHERE ID = ?";

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

    /** @return the student, or null when no row has that id */
    public Student findById(int id) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            pstmt.setInt(1, id);
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

    /** Updates the row whose id matches, using the values carried by the student. */
    public void update(Student student) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getEmail());
            pstmt.setString(3, student.getPhoneNo());
            pstmt.setInt(4, student.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE)) {
            pstmt.setInt(1, id);
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

    /** Binds the three columns an insert writes; ID is left to the database. */
    private void bind(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getName());
        pstmt.setString(2, student.getEmail());
        pstmt.setString(3, student.getPhoneNo());
    }

    private Student toStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("ID"),
                rs.getString("NAME"),
                rs.getString("EMAIL"),
                rs.getString("PHONENO"));
    }
}
