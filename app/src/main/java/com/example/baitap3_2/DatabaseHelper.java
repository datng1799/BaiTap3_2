package com.example.baitap3_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp làm việc với cơ sở dữ liệu SQLite.
 * Chứa đầy đủ các thao tác: thêm, sửa, xóa, lấy theo mã, tìm kiếm + sắp xếp theo lương.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "qlns.db";
    private static final int DB_VERSION = 1;

    // ===== Tên bảng và tên cột =====
    public static final String TABLE = "employees";
    public static final String COL_ID = "emp_id";
    public static final String COL_NAME = "name";
    public static final String COL_BIRTH_YEAR = "birth_year";
    public static final String COL_GENDER = "gender";
    public static final String COL_HOMETOWN = "hometown";
    public static final String COL_EDUCATION = "education";
    public static final String COL_IMAGE = "image";
    public static final String COL_SALARY = "salary";
    public static final String COL_DEPARTMENT = "department";
    // Cột phụ: chuỗi đã bỏ dấu (mã + tên + quê + phòng ban) dùng cho tìm kiếm
    public static final String COL_SEARCH = "search_text";

    // ===== Các kiểu sắp xếp (khớp thứ tự mảng R.array.sort_options) =====
    public static final int SORT_DEFAULT = 0;       // theo mã nhân sự
    public static final int SORT_SALARY_ASC = 1;    // lương tăng dần
    public static final int SORT_SALARY_DESC = 2;   // lương giảm dần

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE + " ("
                    + COL_ID + " TEXT PRIMARY KEY, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_BIRTH_YEAR + " INTEGER, "
                    + COL_GENDER + " TEXT, "
                    + COL_HOMETOWN + " TEXT, "
                    + COL_EDUCATION + " TEXT, "
                    + COL_IMAGE + " BLOB, "
                    + COL_SALARY + " INTEGER, "
                    + COL_DEPARTMENT + " TEXT, "
                    + COL_SEARCH + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Bài tập đơn giản: nâng version thì xóa bảng cũ và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /** Thêm vài nhân sự mẫu để lần đầu mở app đã có dữ liệu xem thử. */
    private void insertSampleData(SQLiteDatabase db) {
        Employee[] samples = {
                new Employee("NV001", "Trần Văn An", 1990, "Nam", "Hà Nội",
                        "Đại học", null, 18_000_000, "Phần mềm"),
                new Employee("NV002", "Lê Thị Bình", 1995, "Nữ", "Nam Định",
                        "Cao đẳng", null, 9_500_000, "Hành chính"),
                new Employee("NV003", "Phạm Minh Cường", 1988, "Nam", "Hải Phòng",
                        "Thạc sĩ", null, 25_000_000, "Dự án"),
                new Employee("NV004", "Đỗ Thu Hà", 1998, "Nữ", "Thanh Hóa",
                        "Đại học", null, 14_000_000, "Kỹ thuật"),
                new Employee("NV005", "Vũ Đức Long", 1993, "Nam", "Bắc Ninh",
                        "Đại học", null, 16_500_000, "Phần mềm")
        };
        for (Employee e : samples) {
            db.insert(TABLE, null, toContentValues(e));
        }
    }

    // ======================= THÊM / SỬA / XÓA =======================

    /** Thêm mới. Trả về false nếu lỗi (ví dụ trùng mã nhân sự). */
    public boolean insertEmployee(Employee e) {
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(TABLE, null, toContentValues(e));
        return result != -1;
    }

    /** Cập nhật nhân sự theo mã. Trả về true nếu có dòng được sửa. */
    public boolean updateEmployee(Employee e) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.update(TABLE, toContentValues(e), COL_ID + " = ?",
                new String[]{e.getId()});
        return rows > 0;
    }

    /** Xóa nhân sự theo mã. */
    public boolean deleteEmployee(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE, COL_ID + " = ?", new String[]{id});
        return rows > 0;
    }

    // ======================= TRUY VẤN =======================

    /** Kiểm tra mã nhân sự đã tồn tại chưa (dùng khi thêm mới). */
    public boolean exists(String id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE, new String[]{COL_ID}, COL_ID + " = ?",
                new String[]{id}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    /** Lấy 1 nhân sự theo mã, không có thì trả về null. */
    public Employee getEmployee(String id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE, null, COL_ID + " = ?",
                new String[]{id}, null, null, null)) {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
            return null;
        }
    }

    /**
     * Lấy danh sách nhân sự có lọc và sắp xếp ngay trong câu SQL.
     *
     * @param keyword  từ khóa tìm theo mã / tên / quê quán / phòng ban (rỗng = lấy tất cả)
     * @param sortMode SORT_DEFAULT, SORT_SALARY_ASC hoặc SORT_SALARY_DESC
     */
    public List<Employee> getEmployees(String keyword, int sortMode) {
        List<Employee> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // --- Điều kiện WHERE cho tìm kiếm ---
        String selection = null;
        String[] selectionArgs = null;
        String key = TextUtil.normalize(keyword);
        if (!key.isEmpty()) {
            selection = COL_SEARCH + " LIKE ?";
            selectionArgs = new String[]{"%" + key + "%"};
        }

        // --- ORDER BY cho sắp xếp ---
        String orderBy;
        switch (sortMode) {
            case SORT_SALARY_ASC:
                orderBy = COL_SALARY + " ASC, " + COL_ID + " ASC";
                break;
            case SORT_SALARY_DESC:
                orderBy = COL_SALARY + " DESC, " + COL_ID + " ASC";
                break;
            default:
                orderBy = COL_ID + " ASC";
                break;
        }

        // Tương đương: SELECT * FROM employees WHERE search_text LIKE '%key%' ORDER BY ...
        try (Cursor c = db.query(TABLE, null, selection, selectionArgs, null, null, orderBy)) {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        }
        return list;
    }

    // ======================= CHUYỂN ĐỔI =======================

    /** Employee -> ContentValues (để insert / update). */
    private ContentValues toContentValues(Employee e) {
        ContentValues v = new ContentValues();
        v.put(COL_ID, e.getId());
        v.put(COL_NAME, e.getName());
        v.put(COL_BIRTH_YEAR, e.getBirthYear());
        v.put(COL_GENDER, e.getGender());
        v.put(COL_HOMETOWN, e.getHometown());
        v.put(COL_EDUCATION, e.getEducation());
        v.put(COL_IMAGE, e.getImage());
        v.put(COL_SALARY, e.getSalary());
        v.put(COL_DEPARTMENT, e.getDepartment());
        String searchText = e.getId() + " " + e.getName() + " "
                + e.getHometown() + " " + e.getDepartment();
        v.put(COL_SEARCH, TextUtil.normalize(searchText));
        return v;
    }

    /** Dòng hiện tại của Cursor -> Employee. */
    private Employee fromCursor(Cursor c) {
        Employee e = new Employee();
        e.setId(c.getString(c.getColumnIndexOrThrow(COL_ID)));
        e.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        e.setBirthYear(c.getInt(c.getColumnIndexOrThrow(COL_BIRTH_YEAR)));
        e.setGender(c.getString(c.getColumnIndexOrThrow(COL_GENDER)));
        e.setHometown(c.getString(c.getColumnIndexOrThrow(COL_HOMETOWN)));
        e.setEducation(c.getString(c.getColumnIndexOrThrow(COL_EDUCATION)));
        int imgIndex = c.getColumnIndexOrThrow(COL_IMAGE);
        e.setImage(c.isNull(imgIndex) ? null : c.getBlob(imgIndex));
        e.setSalary(c.getLong(c.getColumnIndexOrThrow(COL_SALARY)));
        e.setDepartment(c.getString(c.getColumnIndexOrThrow(COL_DEPARTMENT)));
        return e;
    }
}
