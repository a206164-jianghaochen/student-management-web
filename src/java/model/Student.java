package model;

/**
 * One row of the STUDENT table.
 * A plain data holder: no JDBC, no HTML, no business rules.
 */
public class Student {

    private String name;
    private String email;
    private String phoneNo;

    public Student() {
    }

    public Student(String name, String email, String phoneNo) {
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
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
        return "Student{name=" + name + ", email=" + email + ", phoneNo=" + phoneNo + "}";
    }
}