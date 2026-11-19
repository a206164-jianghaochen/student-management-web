# Student Management System

A CRUD web application built for a Java Web course at Universiti Kebangsaan
Malaysia. Jakarta Servlet on the front, Apache Derby behind it, no framework.

## Features

- List all students, insert, update, delete
- Search by name (partial match)
- Explicit transaction handling with commit / rollback (see `testTransactionWeb()`)

## Security

| Concern | How it is handled |
| --- | --- |
| SQL injection | Every query uses `PreparedStatement` with bound parameters |
| XSS | All dynamic output is escaped through `HtmlUtil` |
| Query-string injection | Link parameters are URL-encoded before being written into `href` |
| Credential leakage | Database credentials live in `db.properties`, not in Java source |

The XSS fix was verified by inserting a student named `<b>test</b>`: the list
page renders it as literal text rather than bold.

## Structure

```
src/java/
  servlet/
    StudentServlet.java     request handling and HTML rendering
  dao/
    StudentDAO.java         all SQL for the STUDENT table
  model/
    Student.java            one row; no JDBC, no HTML
  util/
    DBUtil.java             loads db.properties, hands out connections
    HtmlUtil.java           HTML escaping
  db.properties             local credentials (not committed)
  db.properties.example     template to copy
```

The servlet never builds a SQL string or opens a `Connection`: it asks the DAO
for a `List<Student>` and renders that.

## Database

Create a Derby database named `student` with one table:

```sql
CREATE TABLE STUDENT (
    ID      INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    NAME    VARCHAR(100) NOT NULL,
    EMAIL   VARCHAR(100),
    PHONENO VARCHAR(30),
    PRIMARY KEY (ID)
);
```

`ID` is assigned by the database: inserts never send a value for it, and the
generated key comes back on the next select. Names are free to repeat.

Copy `src/java/db.properties.example` to `src/java/db.properties` and fill in
your own values:

```
db.url=jdbc:derby://localhost:1527/student
db.user=<your user>
db.password=<your password>
```

## Running

1. Open the project in NetBeans
2. Start the GlassFish server and the Derby network server
3. Clean and Build, then Run
4. Open `http://localhost:8080/StudentManagementWeb/student`

## Known limitations

- No pagination: a listing loads the whole table into memory.
- No server-side validation beyond what the browser enforces with `required`.
- No connection pooling yet; `DBUtil` opens a connection per request.
- HTML is written from the servlet rather than from JSP views.