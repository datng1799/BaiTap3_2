package com.example.baitap3_2;

import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Các hàm tiện ích xử lý chuỗi.
 */
public final class TextUtil {

    private TextUtil() {
    }

    /**
     * Chuẩn hóa chuỗi để tìm kiếm: bỏ dấu tiếng Việt, đổi đ -> d, viết thường.
     * Ví dụ: "Nguyễn Quốc Đạt" -> "nguyen quoc dat"
     *
     * Lý do: toán tử LIKE của SQLite chỉ không phân biệt hoa/thường với chữ không dấu,
     * nên ta lưu sẵn một cột đã chuẩn hóa để gõ "dat" vẫn tìm ra "Đạt".
     */
    public static String normalize(String s) {
        if (s == null) {
            return "";
        }
        // Tách chữ và dấu (NFD), sau đó xóa các ký tự dấu (\p{M})
        String t = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        // Chữ đ/Đ không tách được dấu nên phải thay thủ công
        t = t.replace('đ', 'd').replace('Đ', 'D');
        return t.toLowerCase(Locale.ROOT).trim();
    }

    /** Định dạng tiền: 15000000 -> "15.000.000 VNĐ" */
    public static String formatMoney(long amount) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return nf.format(amount) + " VNĐ";
    }
}
