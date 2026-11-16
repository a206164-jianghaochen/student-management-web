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
  util/
    DBUtil.java             loads db.properties, hands out connections
    HtmlUtil.java           HTML escaping
  db.properties             local credentials (not committed)
  db.properties.example     template to copy
```

## Database

Create a Derby database named `student` with one table:

```sql
CREATE TABLE STUDENT (
    NAME    VARCHAR(100),
    EMAIL   VARCHAR(100),
    PHONENO VARCHAR(30)
);
```

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

- `NAME` is used as the primary key, so duplicate names would collide. Moving to
  an auto-increment `ID` is the next step.
- No connection pooling yet; `DBUtil` opens a connection per request.
- HTML is written from the servlet rather than from JSP views.