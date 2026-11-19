package servlet;

import dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import model.Student;
import util.HtmlUtil;

@WebServlet("/student")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** The servlet talks to this, never to JDBC. */
    private final StudentDAO dao = new StudentDAO();

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
                    showAllStudents(out);
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
                    showTransactionTest(out);
                    break;
                default:
                    showAllStudents(out);
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

        try {
            if ("insert".equals(action)) {
                dao.insert(new Student(
                        request.getParameter("name"),
                        request.getParameter("email"),
                        request.getParameter("phoneno")));
            } else if ("delete".equals(action)) {
                dao.delete(request.getParameter("name"));
            } else if ("update".equals(action)) {
                dao.update(
                        request.getParameter("originalName"),
                        request.getParameter("email"),
                        request.getParameter("phoneno"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        response.sendRedirect("student?action=list");
    }

    private void renderTable(List<Student> students, PrintWriter out) {
        out.println("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>");
        out.println("<tr style='background-color:#f2f2f2;'>");
        out.println("<th>NAME</th><th>EMAIL</th><th>PHONENO</th><th>ACTIONS</th>");
        out.println("</tr>");

        for (Student student : students) {
            String name = student.getName();
            out.println("<tr>");
            out.println("<td>" + HtmlUtil.escape(name) + "</td>");
            out.println("<td>" + HtmlUtil.escape(student.getEmail()) + "</td>");
            out.println("<td>" + HtmlUtil.escape(student.getPhoneNo()) + "</td>");
            out.println("<td>");

            out.println("<form action='student' method='POST' style='display:inline;'>");
            out.println("<input type='hidden' name='action' value='delete'/>");
            out.println("<input type='hidden' name='name' value='" + HtmlUtil.escape(name) + "'/>");
            out.println("<input type='submit' value='Delete' onclick='return confirm(\"Are you sure?\");'/>");
            out.println("</form>");

            String editUrl = "student?action=edit&name="
                    + URLEncoder.encode(name, StandardCharsets.UTF_8);
            out.println("<a href='" + HtmlUtil.escape(editUrl) + "'>Edit</a>");

            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");
    }

    private void showAllStudents(PrintWriter out) {
        try {
            renderTable(dao.findAll(), out);
        } catch (SQLException ex) {
            printError("Database Error", ex, out);
        }
    }

    private void searchStudent(String keyword, PrintWriter out) {
        if (keyword == null || keyword.trim().isEmpty()) {
            showAllStudents(out);
            return;
        }
        out.println("<h3>Search Results for: <span style='color:blue;'>"
                + HtmlUtil.escape(keyword) + "</span></h3>");
        try {
            renderTable(dao.searchByName(keyword), out);
        } catch (SQLException ex) {
            printError("Search Error", ex, out);
        }
    }

    private void showTransactionTest(PrintWriter out) {
        out.println("<h3>--- Testing Transaction (ACID Test) ---</h3>");
        try {
            dao.insertBoth(
                    new Student("user1", "user1@example.com", "235778910"),
                    new Student("user2", "user2@example.com", "232323232"));
            out.println("<p style='color:green;'>[Transaction] Committed successfully!</p>");
        } catch (SQLException ex) {
            out.println("<p style='color:red;'>[Transaction Failed] Rollback executed: "
                    + HtmlUtil.escape(ex.getMessage()) + "</p>");
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
        if (name == null || name.isEmpty()) {
            showAllStudents(out);
            return;
        }
        try {
            Student student = dao.findByName(name);
            if (student == null) {
                out.println("<p style='color:red;'>No such student.</p>");
                return;
            }
            out.println("<h3>Edit Student: " + HtmlUtil.escape(name) + "</h3>");
            out.println("<form action='student' method='POST'>");
            out.println("<input type='hidden' name='action' value='update'/>");
            out.println("<input type='hidden' name='originalName' value='" + HtmlUtil.escape(name) + "'/>");
            out.println("Email: <input type='email' name='email' value='"
                    + HtmlUtil.escape(student.getEmail()) + "' required/> ");
            out.println("Phone: <input type='text' name='phoneno' value='"
                    + HtmlUtil.escape(student.getPhoneNo()) + "' required/> ");
            out.println("<input type='submit' value='Save Changes'/> ");
            out.println("<a href='student?action=list'>Cancel</a>");
            out.println("</form>");
        } catch (SQLException ex) {
            printError("Error", ex, out);
        }
    }

    private void printError(String label, SQLException ex, PrintWriter out) {
        out.println("<p style='color:red;'>" + label + ": "
                + HtmlUtil.escape(String.valueOf(ex.getMessage())) + "</p>");
    }
}