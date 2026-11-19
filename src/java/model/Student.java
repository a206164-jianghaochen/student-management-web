package model;

/**
 * One row of the STUDENT table.
 * A plain data holder: no JDBC, no HTML, no business rules.
 *
 * The id is 0 on a Student that has not been saved yet. The database
 * assigns the real value (identity column), and the DAO fills it in
 * when the row is read back.
 */
public class Student {

    private int id;
    private String name;
    private String email;
    private String phoneNo;

    public Student() {
    }

    /** For a brand new row: no id yet. */
    public Student(String name, String email, String phoneNo) {
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    /** For a row loaded from the database. */
    public Student(int id, String name, String email, String phoneNo) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name=" + name
                + ", email=" + email + ", phoneNo=" + phoneNo + "}";
    }
}
