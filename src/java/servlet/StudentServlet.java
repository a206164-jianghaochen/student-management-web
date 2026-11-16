package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import util.DBUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import util.HtmlUtil;

@WebServlet("/student")
public class StudentServlet extends HttpServlet {

   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            action = "list"; 
        }
        try (PrintWriter out = response.getWriter()) {
            out.println("<html><head><title>Student Management System</title></head><body>");
            out.println("<h2>Student Management System (Web Version)</h2>");
            
            out.println("<p>");
            out.println("<a href='student?action=list'>Show All Students</a> | ");
            out.println("<a href='student?action=testTx'>Test Transaction</a>");
            out.println("</p><hr/>");
            out.println("<form action='student' method='GET' style='display:inline;'>");
            out.println("<input type= 'text' name='searchKeyword' placeholder='Search by name' required>");
            out.println("<input type= 'hidden' name='action' value='search'>");
            out.println("<input type='submit' value='Search'/>");
            out.println("</form>");
            out.println("<br/><br/>");
            
            switch (action) {
                case "list":
                    showAllStudentsWeb(out);
                    renderInsertForm(out);
                    break;
                case "edit":
                    renderEditForm(request.getParameter("name"), out);
                    break;
                case "search":
                    searchStudent(request.getParameter("searchKeyword"), out);
                    renderInsertForm(out);
                    break;
                case "testTx":
                    testTransactionWeb(out);
                    break;
                default:
                    showAllStudentsWeb(out);
                    renderInsertForm(out);
            }
            out.println("</body></html>");
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phoneno");

            String insertSQL = "INSERT INTO STUDENT (NAME, EMAIL, PHONENO) VALUES (?, ?, ?)";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else if ("delete".equals(action)) {
            String targetName = request.getParameter("name");
            String deleteSQL = "DELETE FROM STUDENT WHERE NAME = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.setString(1, targetName);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }else if("update".equals(action)){
            String originalName = request.getParameter("originalName");
            String newEmail = request.getParameter("email");
            String newPhone = request.getParameter("phoneno");
            String updateSQL = "UPDATE student SET EMAIL = ?, PHONENO = ? WHERE NAME = ?";
            try(Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSQL)){
                pstmt.setString(1, newEmail);
                pstmt.setString(2, newPhone);
                pstmt.setString(3, originalName);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                System.getLogger(StudentServlet.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        response.sendRedirect("student?action=list");
    }
    private void renderWebTable(ResultSet rs, PrintWriter out) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        out.println("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>");
        out.println("<tr style='background-color:#f2f2f2;'>");
        for (int i = 1; i <= columnCount; i++) {
            out.println("<th>" + meta.getColumnName(i) + "</th>");
        }
        out.println("<th>ACTIONS</th>"); 
        out.println("</tr>");

        while (rs.next()) {
            out.println("<tr>");
            for (int i = 1; i <= columnCount; i++) {
                out.println("<td>" + HtmlUtil.escape(rs.getString(i)) + "</td>");
            }
            String studentName = rs.getString("NAME");
            out.println("<td>");
            out.println("<form action='student' method='POST' style='display:inline;'>");
            out.println("<input type='hidden' name='action' value='delete'/>");
            out.println("<input type='hidden' name='name' value='" + HtmlUtil.escape(studentName) + "'/>");
            out.println("<input type='submit' value='Delete' onclick='return confirm(\"Are you sure?\");'/>");
            out.println("</form>");
            
            String editUrl = "student?action=edit&name="
            + URLEncoder.encode(studentName, StandardCharsets.UTF_8);
            out.println("<a href='" + HtmlUtil.escape(editUrl) + "'>Edit</a>");
            
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");
    }
    private void showAllStudentsWeb(PrintWriter out) {
        String selectSQL = "SELECT * FROM STUDENT";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {     
            renderWebTable(rs, out);

        } catch (SQLException ex) {
            out.println("<p style='color:red;'>Database Error: " + ex.getMessage() + "</p>");
        }
    }
    private void testTransactionWeb(PrintWriter out) {
        out.println("<h3>--- Testing Transaction (ACID Test) ---</h3>");
        String insertSQL = "INSERT INTO STUDENT (NAME, EMAIL, PHONENO) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSQL);
                 PreparedStatement pstmt2 = conn.prepareStatement(insertSQL)) {
                
                pstmt1.setString(1, "user1");
                pstmt1.setString(2, "user1@example.com");
                pstmt1.setString(3, "235778910");
                pstmt1.executeUpdate();

                pstmt2.setString(1, "user2");
                pstmt2.setString(2, "user2@example.com");
                pstmt2.setString(3, "232323232");
                pstmt2.executeUpdate();
                conn.commit(); 
                out.println("<p style='color:green;'>[Transaction] Committed successfully!</p>");

            } catch (SQLException ex) {
                conn.rollback(); 
                out.println("<p style='color:red;'>[Transaction Failed] Rollback executed: " + ex.getMessage() + "</p>");
            } finally {
                conn.setAutoCommit(true); 
            }
        } catch (SQLException ex) {
            out.println("<p style='color:red;'>Connection Error: " + ex.getMessage() + "</p>");
        }
    }
    private void renderInsertForm(PrintWriter out) {
        out.println("<h3>Add New Student</h3>");
        out.println("<form action='student' method='POST'>");
        out.println("<input type='hidden' name='action' value='insert'/>");
        out.println("Name: <input type='text' name='name' required/> ");
        out.println("Email: <input type='email' name='email' required/> ");
        out.println("Phone: <input type='text' name='phoneno' required/> ");
        out.println("<input type='submit' value='Add Student'/>");
        out.println("</form>");
    } 
    private void renderEditForm(String name, PrintWriter out) {
    String selectSQL = "SELECT * FROM STUDENT WHERE NAME = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
        pstmt.setString(1, name);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String email = rs.getString("EMAIL");
                String phone = rs.getString("PHONENO");

                out.println("<h3>Edit Student: " + HtmlUtil.escape(name) + "</h3>");
                out.println("<form action='student' method='POST'>");
                out.println("<input type='hidden' name='action' value='update'/>");
                out.println("<input type='hidden' name='originalName' value='" + HtmlUtil.escape(name) + "'/>");
                out.println("Email: <input type='email' name='email' value='" + HtmlUtil.escape(email) + "' required/> ");
                out.println("Phone: <input type='text' name='phoneno' value='" + HtmlUtil.escape(phone) + "' required/> ");
                out.println("<input type='submit' value='Save Changes'/> ");
                out.println("<a href='student?action=list'>Cancel</a>");
                out.println("</form>");
            }
        }
    } catch (SQLException ex) {
        out.println("<p style='color:red;'>Error: " + ex.getMessage() + "</p>");
    }
}  
    private void searchStudent(String name, PrintWriter out){
        if(name==null || name.trim().isEmpty()){
            showAllStudentsWeb(out);
            return;
        }
        out.println("<h3>Search Results for: <span style='color:blue;'>" + HtmlUtil.escape(name) + "</span></h3>");
        String searchSQL = "SELECT * FROM STUDENT WHERE NAME LIKE ?";
        try(Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(searchSQL);
                ){
            pstmt.setString(1, "%" + name + "%");
            try(ResultSet rs = pstmt.executeQuery()){
                renderWebTable(rs, out);
            }
        } catch (SQLException ex) {
            out.println("<p style='color:red;'>Search Error: " + ex.getMessage() + "</p>");
        }
    }
}
