package com.example.baitap3_2;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter hiển thị danh sách nhân sự trong RecyclerView.
 * Mỗi thẻ có 2 trạng thái: thu gọn / mở rộng (bấm vào thẻ để chuyển).
 */
public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    /** Activity cài đặt interface này để xử lý khi bấm Sửa / Xóa. */
    public interface OnEmployeeActionListener {
        void onEdit(Employee employee);

        void onDelete(Employee employee);
    }

    private final List<Employee> items = new ArrayList<>();
    // Lưu mã các nhân sự đang mở rộng (giữ trạng thái khi cuộn danh sách)
    private final Set<String> expandedIds = new HashSet<>();
    private final OnEmployeeActionListener listener;

    public EmployeeAdapter(OnEmployeeActionListener listener) {
        this.listener = listener;
    }

    /** Thay toàn bộ dữ liệu (sau khi tìm kiếm / sắp xếp / thêm / sửa / xóa). */
    public void setItems(List<Employee> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Employee e = items.get(position);

        // --- Phần thu gọn ---
        h.tvName.setText(e.getName());
        h.tvId.setText(e.getId());
        h.tvDepartment.setText(e.getDepartment());
        h.tvSalary.setText(TextUtil.formatMoney(e.getSalary()));

        Bitmap bmp = ImageUtil.toBitmap(e.getImage());
        if (bmp != null) {
            h.ivPhoto.setImageBitmap(bmp);
        } else {
            h.ivPhoto.setImageResource(R.drawable.ic_person); // chưa có ảnh -> ảnh mặc định
        }

        // --- Phần chi tiết ---
        h.tvBirthYear.setText(h.itemView.getContext()
                .getString(R.string.detail_birth_year, e.getBirthYear()));
        h.tvGender.setText(h.itemView.getContext()
                .getString(R.string.detail_gender, e.getGender()));
        h.tvHometown.setText(h.itemView.getContext()
                .getString(R.string.detail_hometown, e.getHometown()));
        h.tvEducation.setText(h.itemView.getContext()
                .getString(R.string.detail_education, e.getEducation()));

        // --- Trạng thái mở rộng / thu gọn ---
        boolean expanded = expandedIds.contains(e.getId());
        h.layoutDetail.setVisibility(expanded ? View.VISIBLE : View.GONE);
        h.ivExpand.setRotation(expanded ? 180f : 0f);

        // Bấm vào thẻ: đảo trạng thái rồi vẽ lại đúng dòng đó
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            String id = items.get(pos).getId();
            if (expandedIds.contains(id)) {
                expandedIds.remove(id);
            } else {
                expandedIds.add(id);
            }
            notifyItemChanged(pos);
        });

        h.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Giữ tham chiếu tới các view của 1 thẻ để không phải findViewById lại nhiều lần. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final ImageView ivExpand;
        final TextView tvName;
        final TextView tvId;
        final TextView tvDepartment;
        final TextView tvSalary;
        final View layoutDetail;
        final TextView tvBirthYear;
        final TextView tvGender;
        final TextView tvHometown;
        final TextView tvEducation;
        final MaterialButton btnEdit;
        final MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivExpand = itemView.findViewById(R.id.ivExpand);
            tvName = itemView.findViewById(R.id.tvName);
            tvId = itemView.findViewById(R.id.tvId);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            layoutDetail = itemView.findViewById(R.id.layoutDetail);
            tvBirthYear = itemView.findViewById(R.id.tvBirthYear);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvHometown = itemView.findViewById(R.id.tvHometown);
            tvEducation = itemView.findViewById(R.id.tvEducation);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
