# BaiTap3_2 – Ứng dụng quản lý nhân sự (Android, Java, SQLite)

**Sinh viên:** Nguyễn Quốc Đạt – **MSSV:** K23DTCN092

## Chức năng
- Hiển thị danh sách nhân sự (thẻ thu gọn: ảnh, tên, mã, phòng ban, lương; bấm vào để mở rộng xem đủ thông tin)
- **Thêm / Sửa** nhân sự (màn hình form riêng, có chọn ảnh từ thư viện)
- **Xóa** nhân sự (có hộp thoại xác nhận)
- **Tìm kiếm** theo mã, tên, quê quán, phòng ban (gõ không dấu vẫn tìm được, ví dụ `dat` → `Đạt`)
- **Sắp xếp** theo mức lương tăng / giảm dần (dùng `ORDER BY` trong SQLite)
- Mọi màn hình đều có thanh thông tin sinh viên (ảnh đại diện + MSSV – Họ tên)

## Thông tin nhân sự
Mã nhân sự, Họ và tên, Năm sinh, Giới tính, Quê quán, Trình độ, Ảnh, Mức lương,
Phòng ban (Hành chính, Phần mềm, Kỹ thuật, Dự án).

## Cấu trúc code (`app/src/main/java/com/example/baitap3_2/`)
| File | Vai trò |
|---|---|
| `Employee.java` | Lớp model 1 nhân sự |
| `DatabaseHelper.java` | SQLite: tạo bảng, dữ liệu mẫu, thêm/sửa/xóa, tìm kiếm + sắp xếp |
| `MainActivity.java` | Màn hình danh sách, tìm kiếm, sắp xếp, xóa |
| `EmployeeAdapter.java` | Adapter RecyclerView, thu gọn / mở rộng thẻ |
| `EmployeeFormActivity.java` | Màn hình thêm / sửa, kiểm tra dữ liệu nhập |
| `ImageUtil.java` | Thu nhỏ ảnh, chuyển ảnh ↔ mảng byte (BLOB) |
| `TextUtil.java` | Bỏ dấu tiếng Việt để tìm kiếm, định dạng tiền |

## Bảng SQLite `employees`
`emp_id` (TEXT, khóa chính), `name`, `birth_year`, `gender`, `hometown`, `education`,
`image` (BLOB), `salary` (INTEGER), `department`, `search_text` (chuỗi đã bỏ dấu để tìm kiếm).
