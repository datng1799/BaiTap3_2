package com.example.baitap3_2;

/**
 * Lớp mô tả 1 nhân sự (model).
 * Mỗi đối tượng Employee tương ứng với 1 dòng trong bảng "employees" của SQLite.
 */
public class Employee {

    private String id;          // Mã nhân sự (khóa chính, ví dụ: NV001)
    private String name;        // Họ và tên
    private int birthYear;      // Năm sinh
    private String gender;      // Giới tính: "Nam" / "Nữ"
    private String hometown;    // Quê quán
    private String education;   // Trình độ (Đại học, Thạc sĩ, ...)
    private byte[] image;       // Ảnh, lưu dạng mảng byte (BLOB); null = chưa có ảnh
    private long salary;        // Mức lương (VNĐ)
    private String department;  // Phòng ban: Hành chính / Phần mềm / Kỹ thuật / Dự án

    public Employee() {
    }

    public Employee(String id, String name, int birthYear, String gender, String hometown,
                    String education, byte[] image, long salary, String department) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
        this.gender = gender;
        this.hometown = hometown;
        this.education = education;
        this.image = image;
        this.salary = salary;
        this.department = department;
    }

    // ===== Getter / Setter =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getHometown() { return hometown; }
    public void setHometown(String hometown) { this.hometown = hometown; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public long getSalary() { return salary; }
    public void setSalary(long salary) { this.salary = salary; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
