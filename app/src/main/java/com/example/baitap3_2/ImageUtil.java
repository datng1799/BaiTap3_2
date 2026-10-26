package com.example.baitap3_2;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tiện ích xử lý ảnh nhân sự.
 * Ảnh chọn từ máy được thu nhỏ rồi nén JPEG trước khi lưu vào SQLite,
 * vì lưu ảnh gốc (vài MB) vào BLOB sẽ làm app chậm và dễ lỗi khi đọc.
 */
public final class ImageUtil {

    /** Cạnh dài nhất của ảnh sau khi thu nhỏ (pixel). */
    public static final int MAX_SIZE = 400;

    private ImageUtil() {
    }

    /** Đọc ảnh từ Uri (thư viện ảnh) -> thu nhỏ -> xoay đúng chiều -> mảng byte JPEG. */
    public static byte[] loadScaledJpeg(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        // Bước 1: chỉ đọc kích thước ảnh (chưa tải ảnh vào bộ nhớ)
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }

        // Bước 2: tính hệ số giảm mẫu (1, 2, 4, 8...) để tiết kiệm bộ nhớ
        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= MAX_SIZE
                && bounds.outHeight / (sample * 2) >= MAX_SIZE) {
            sample *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;

        Bitmap bmp;
        try (InputStream in = resolver.openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bmp == null) {
            throw new IOException("Không đọc được ảnh");
        }

        // Bước 3: thu nhỏ chính xác về MAX_SIZE
        float scale = Math.min(1f, (float) MAX_SIZE / Math.max(bmp.getWidth(), bmp.getHeight()));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Bước 4: ảnh chụp từ camera thường lưu hướng xoay trong EXIF -> xoay lại cho đúng
        matrix.postRotate(readExifRotation(resolver, uri));

        Bitmap result = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        // Bước 5: nén JPEG chất lượng 85%
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        return out.toByteArray();
    }

    /** Đọc góc xoay (0/90/180/270) từ thông tin EXIF của ảnh. */
    private static int readExifRotation(ContentResolver resolver, Uri uri) {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) {
                return 0;
            }
            ExifInterface exif = new ExifInterface(in);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    /** Mảng byte (lấy từ SQLite) -> Bitmap để hiển thị. Trả về null nếu không có ảnh. */
    public static Bitmap toBitmap(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
