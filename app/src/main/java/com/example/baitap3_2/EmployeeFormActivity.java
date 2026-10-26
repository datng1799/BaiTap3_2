package com.example.baitap3_2;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Màn hình thêm mới / sửa nhân sự.
 * - Mở không kèm EXTRA_EMP_ID  -> chế độ THÊM
 * - Mở có kèm EXTRA_EMP_ID     -> chế độ SỬA (không cho đổi mã nhân sự)
 */
public class EmployeeFormActivity extends AppCompatActivity {

    public static final String EXTRA_EMP_ID = "extra_emp_id";
    private static final String STATE_IMAGE = "state_image";

    private static final int MIN_BIRTH_YEAR = 1940;
    private static final int MIN_AGE = 16; // tuổi tối thiểu đi làm

    private DatabaseHelper db;
    private String editingId;   // null = đang thêm mới
    private byte[] imageBytes;  // ảnh hiện tại (null = chưa có ảnh)

    private TextInputLayout tilId, tilName, tilBirthYear, tilHometown, tilSalary;
    private TextInputEditText edtId, edtName, edtBirthYear, edtHometown, edtSalary;
    private RadioGroup rgGender;
    private RadioButton rbFemale;
    private Spinner spEducation, spDepartment;
    private ShapeableImageView ivPhoto;

    private String[] departments;
    private String[] educations;

    // Mở thư viện ảnh để chọn 1 ảnh
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::onImagePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_form);

        // Chừa chỗ cho thanh hệ thống và bàn phím (để bàn phím không che nút Lưu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        departments = getResources().getStringArray(R.array.departments);
        educations = getResources().getStringArray(R.array.educations);

        bindViews();
        setupSpinners();

        editingId = getIntent().getStringExtra(EXTRA_EMP_ID);
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        tvFormTitle.setText(editingId == null ? R.string.title_add : R.string.title_edit);

        if (savedInstanceState != null) {
            // Xoay màn hình: các ô nhập tự khôi phục, chỉ cần lấy lại ảnh
            imageBytes = savedInstanceState.getByteArray(STATE_IMAGE);
        } else if (editingId != null) {
            fillForm(db.getEmployee(editingId));
        }
        if (editingId != null) {
            edtId.setEnabled(false); // mã là khóa chính -> không cho sửa
        }
        showImage();
    }

    private void bindViews() {
        tilId = findViewById(R.id.tilId);
        tilName = findViewById(R.id.tilName);
        tilBirthYear = findViewById(R.id.tilBirthYear);
        tilHometown = findViewById(R.id.tilHometown);
        tilSalary = findViewById(R.id.tilSalary);

        edtId = findViewById(R.id.edtId);
        edtName = findViewById(R.id.edtName);
        edtBirthYear = findViewById(R.id.edtBirthYear);
        edtHometown = findViewById(R.id.edtHometown);
        edtSalary = findViewById(R.id.edtSalary);

        rgGender = findViewById(R.id.rgGender);
        rbFemale = findViewById(R.id.rbFemale);
        spEducation = findViewById(R.id.spEducation);
        spDepartment = findViewById(R.id.spDepartment);
        ivPhoto = findViewById(R.id.ivPhoto);

        MaterialButton btnPickPhoto = findViewById(R.id.btnPickPhoto);
        MaterialButton btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        btnPickPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnRemovePhoto.setOnClickListener(v -> {
            imageBytes = null;
            showImage();
        });
        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        ArrayAdapter<String> eduAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, educations);
        eduAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEducation.setAdapter(eduAdapter);
        spEducation.setSelection(2); // mặc định "Đại học"

        ArrayAdapter<String> depAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, departments);
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDepartment.setAdapter(depAdapter);
    }

    /** Đổ dữ liệu nhân sự cần sửa lên form. */
    private void fillForm(Employee e) {
        if (e == null) {
            return;
        }
        edtId.setText(e.getId());
        edtName.setText(e.getName());
        edtBirthYear.setText(String.valueOf(e.getBirthYear()));
        edtHometown.setText(e.getHometown());
        edtSalary.setText(String.valueOf(e.getSalary()));

        if (getString(R.string.gender_female).equals(e.getGender())) {
            rbFemale.setChecked(true);
        } else {
            rgGender.check(R.id.rbMale);
        }

        int eduIndex = Arrays.asList(educations).indexOf(e.getEducation());
        if (eduIndex >= 0) {
            spEducation.setSelection(eduIndex);
        }
        int depIndex = Arrays.asList(departments).indexOf(e.getDepartment());
        if (depIndex >= 0) {
            spDepartment.setSelection(depIndex);
        }
        imageBytes = e.getImage();
    }

    /** Nhận ảnh vừa chọn: thu nhỏ rồi giữ lại dạng byte để lưu vào SQLite. */
    private void onImagePicked(Uri uri) {
        if (uri == null) {
            return; // người dùng bấm quay lại, không chọn ảnh
        }
        try {
            imageBytes = ImageUtil.loadScaledJpeg(this, uri);
            showImage();
        } catch (IOException | RuntimeException ex) {
            Toast.makeText(this, R.string.err_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void showImage() {
        Bitmap bmp = ImageUtil.toBitmap(imageBytes);
        if (bmp != null) {
            ivPhoto.setImageBitmap(bmp);
        } else {
            ivPhoto.setImageResource(R.drawable.ic_person);
        }
    }

    /** Kiểm tra dữ liệu nhập rồi thêm mới hoặc cập nhật vào SQLite. */
    private void save() {
        // Xóa thông báo lỗi cũ
        tilId.setError(null);
        tilName.setError(null);
        tilBirthYear.setError(null);
        tilHometown.setError(null);
        tilSalary.setError(null);

        String id = textOf(edtId).toUpperCase(Locale.ROOT);
        String name = textOf(edtName);
        String birthYearText = textOf(edtBirthYear);
        String hometown = textOf(edtHometown);
        String salaryText = textOf(edtSalary);

        boolean valid = true;

        // --- Các ô bắt buộc ---
        if (TextUtils.isEmpty(id)) {
            tilId.setError(getString(R.string.err_required));
            valid = false;
        }
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.err_required));
            valid = false;
        }
        if (TextUtils.isEmpty(hometown)) {
            tilHometown.setError(getString(R.string.err_required));
            valid = false;
        }

        // --- Năm sinh hợp lệ ---
        int maxYear = Calendar.getInstance().get(Calendar.YEAR) - MIN_AGE;
        int birthYear = 0;
        try {
            birthYear = Integer.parseInt(birthYearText);
        } catch (NumberFormatException ignored) {
            // để birthYear = 0 -> báo lỗi bên dưới
        }
        if (birthYear < MIN_BIRTH_YEAR || birthYear > maxYear) {
            tilBirthYear.setError(getString(R.string.err_birth_year, MIN_BIRTH_YEAR, maxYear));
            valid = false;
        }

        // --- Mức lương > 0 ---
        long salary = 0;
        try {
            salary = Long.parseLong(salaryText);
        } catch (NumberFormatException ignored) {
            // để salary = 0 -> báo lỗi bên dưới
        }
        if (salary <= 0) {
            tilSalary.setError(getString(R.string.err_salary));
            valid = false;
        }

        // --- Thêm mới thì mã không được trùng ---
        if (valid && editingId == null && db.exists(id)) {
            tilId.setError(getString(R.string.err_id_exists));
            valid = false;
        }

        if (!valid) {
            return;
        }

        String gender = rgGender.getCheckedRadioButtonId() == R.id.rbFemale
                ? getString(R.string.gender_female)
                : getString(R.string.gender_male);
        String education = educations[spEducation.getSelectedItemPosition()];
        String department = departments[spDepartment.getSelectedItemPosition()];

        Employee e = new Employee(
                editingId != null ? editingId : id,
                name, birthYear, gender, hometown, education, imageBytes, salary, department);

        boolean ok = (editingId == null) ? db.insertEmployee(e) : db.updateEmployee(e);
        if (ok) {
            Toast.makeText(this, editingId == null ? R.string.added : R.string.updated,
                    Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // báo cho MainActivity tải lại danh sách
            finish();
        } else {
            Toast.makeText(this, R.string.err_save, Toast.LENGTH_SHORT).show();
        }
    }

    /** Lấy chữ trong ô nhập, đã cắt khoảng trắng 2 đầu. */
    private static String textOf(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray(STATE_IMAGE, imageBytes);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
