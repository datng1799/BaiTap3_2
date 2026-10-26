package com.example.baitap3_2;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Màn hình chính: hiển thị danh sách nhân sự, tìm kiếm, sắp xếp theo lương,
 * và điều hướng sang màn hình thêm / sửa; xóa trực tiếp bằng hộp thoại xác nhận.
 */
public class MainActivity extends AppCompatActivity implements EmployeeAdapter.OnEmployeeActionListener {

    private DatabaseHelper db;
    private EmployeeAdapter adapter;

    private TextInputEditText edtSearch;
    private Spinner spSort;
    private TextView tvCount;
    private TextView tvEmpty;

    // Mở màn hình thêm/sửa; khi quay về với RESULT_OK thì tải lại danh sách
    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Chừa khoảng trống cho thanh trạng thái / thanh điều hướng
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        // --- Ánh xạ view ---
        edtSearch = findViewById(R.id.edtSearch);
        spSort = findViewById(R.id.spSort);
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rvEmployees = findViewById(R.id.rvEmployees);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // --- Danh sách ---
        adapter = new EmployeeAdapter(this);
        rvEmployees.setLayoutManager(new LinearLayoutManager(this));
        rvEmployees.setAdapter(adapter);

        // --- Tìm kiếm: gõ tới đâu lọc tới đó ---
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadData();
            }
        });

        // --- Sắp xếp theo lương ---
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);
        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- Nút thêm ---
        fabAdd.setOnClickListener(v ->
                formLauncher.launch(new Intent(this, EmployeeFormActivity.class)));

        loadData();
    }

    /** Lấy dữ liệu từ SQLite theo từ khóa + kiểu sắp xếp hiện tại rồi đổ vào danh sách. */
    private void loadData() {
        String keyword = edtSearch.getText() == null ? "" : edtSearch.getText().toString();
        int sortMode = spSort.getSelectedItemPosition(); // 0, 1, 2 khớp với DatabaseHelper.SORT_*

        List<Employee> list = db.getEmployees(keyword, sortMode);
        adapter.setItems(list);

        tvCount.setText(getString(R.string.count_format, list.size()));
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ===== Xử lý nút Sửa / Xóa trên từng thẻ =====

    @Override
    public void onEdit(Employee employee) {
        Intent intent = new Intent(this, EmployeeFormActivity.class);
        intent.putExtra(EmployeeFormActivity.EXTRA_EMP_ID, employee.getId());
        formLauncher.launch(intent);
    }

    @Override
    public void onDelete(Employee employee) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message,
                        employee.getName(), employee.getId(), getString(R.string.student_full)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    if (db.deleteEmployee(employee.getId())) {
                        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
