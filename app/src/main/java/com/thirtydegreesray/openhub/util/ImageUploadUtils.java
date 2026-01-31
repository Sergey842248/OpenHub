package com.thirtydegreesray.openhub.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImageUploadUtils {

    private static final int MAX_DIMENSION = 2048;
    private static final int MAX_UPLOAD_BYTES = 9 * 1024 * 1024;

    public static class ProcessedFile {
        @NonNull
        private final String fileName;
        @NonNull
        private final String mimeType;
        @NonNull
        private final byte[] bytes;

        public ProcessedFile(@NonNull String fileName, @NonNull String mimeType, @NonNull byte[] bytes) {
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.bytes = bytes;
        }

        @NonNull
        public String getFileName() {
            return fileName;
        }

        @NonNull
        public String getMimeType() {
            return mimeType;
        }

        @NonNull
        public byte[] getBytes() {
            return bytes;
        }

        @NonNull
        public MultipartBody.Part toMultipartBodyPart() {
            MediaType mediaType = MediaType.parse(mimeType);
            RequestBody requestBody = RequestBody.create(mediaType, bytes);
            return MultipartBody.Part.createFormData("file", fileName, requestBody);
        }
    }

    @NonNull
    public static ProcessedFile processImageForUpload(@NonNull Context context, @NonNull Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        String displayName = getDisplayName(resolver, uri);
        if (StringUtils.isBlank(displayName)) {
            displayName = "image";
        }

        String mimeType = resolver.getType(uri);
        if (StringUtils.isBlank(mimeType)) {
            mimeType = guessMimeTypeFromName(displayName);
        }

        boolean isImage = !StringUtils.isBlank(mimeType) && mimeType.startsWith("image/");

        if (isImage && (mimeType.equals("image/gif") || mimeType.equals("image/svg+xml"))) {
            byte[] bytes = readAllBytes(resolver, uri);
            return new ProcessedFile(ensureExtension(displayName, getExtensionFromMimeType(mimeType)), mimeType, bytes);
        }

        Bitmap bitmap = decodeDownsampled(resolver, uri, MAX_DIMENSION);
        if (bitmap == null) {
            if (isImage) {
                byte[] bytes = readAllBytes(resolver, uri);
                return new ProcessedFile(ensureExtension(displayName, getExtensionFromMimeType(mimeType)), mimeType, bytes);
            }
            throw new IOException("Invalid file type");
        }

        bitmap = rotateIfNeeded(resolver, uri, bitmap);

        boolean hasAlpha = bitmap.hasAlpha();
        Bitmap.CompressFormat compressFormat = hasAlpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
        String outMimeType = hasAlpha ? "image/png" : "image/jpeg";
        String outExtension = hasAlpha ? "png" : "jpg";

        byte[] encoded = encodeBitmap(bitmap, compressFormat, hasAlpha ? 100 : 85);

        if (hasAlpha && encoded.length > MAX_UPLOAD_BYTES) {
            Bitmap flattened = flattenAlpha(bitmap);
            encoded = encodeBitmap(flattened, Bitmap.CompressFormat.JPEG, 85);
            int quality = 85;
            while (encoded.length > MAX_UPLOAD_BYTES && quality >= 50) {
                quality -= 10;
                encoded = encodeBitmap(flattened, Bitmap.CompressFormat.JPEG, quality);
            }
            flattened.recycle();
            outMimeType = "image/jpeg";
            outExtension = "jpg";
        } else if (!hasAlpha) {
            int quality = 85;
            while (encoded.length > MAX_UPLOAD_BYTES && quality >= 50) {
                quality -= 10;
                encoded = encodeBitmap(bitmap, Bitmap.CompressFormat.JPEG, quality);
            }
        }

        bitmap.recycle();
        return new ProcessedFile(ensureExtension(displayName, outExtension), outMimeType, encoded);
    }

    @Nullable
    private static Bitmap decodeDownsampled(@NonNull ContentResolver resolver, @NonNull Uri uri, int maxDimension) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = resolver.openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension, maxDimension);
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try (InputStream in = resolver.openInputStream(uri)) {
                return BitmapFactory.decodeStream(in, null, options);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }

    @NonNull
    private static Bitmap rotateIfNeeded(@NonNull ContentResolver resolver, @NonNull Uri uri, @NonNull Bitmap bitmap) {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) return bitmap;
            ExifInterface exifInterface = new ExifInterface(in);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotation = exifToDegrees(orientation);
            if (rotation == 0) return bitmap;

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) {
                bitmap.recycle();
            }
            return rotated;
        } catch (Exception ignored) {
            return bitmap;
        }
    }

    private static int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    @NonNull
    private static byte[] encodeBitmap(@NonNull Bitmap bitmap, @NonNull Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, quality, baos);
        return baos.toByteArray();
    }

    @NonNull
    private static Bitmap flattenAlpha(@NonNull Bitmap bitmap) {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return result;
    }

    @NonNull
    private static byte[] readAllBytes(@NonNull ContentResolver resolver, @NonNull Uri uri) throws IOException {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) throw new IOException("Can not open input stream");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    @Nullable
    private static String getDisplayName(@NonNull ContentResolver resolver, @NonNull Uri uri) {
        try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @NonNull
    private static String ensureExtension(@NonNull String fileName, @NonNull String extension) {
        String lower = fileName.toLowerCase();
        String ext = "." + extension.toLowerCase();
        if (lower.endsWith(ext)) {
            return fileName;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot > 0 && dot < fileName.length() - 1) {
            return fileName.substring(0, dot) + ext;
        }
        return fileName + ext;
    }

    @Nullable
    private static String guessMimeTypeFromName(@NonNull String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".heic")) return "image/heic";
        if (lower.endsWith(".heif")) return "image/heif";
        return null;
    }

    @NonNull
    private static String getExtensionFromMimeType(@NonNull String mimeType) {
        if (mimeType.equals("image/png")) return "png";
        if (mimeType.equals("image/jpeg")) return "jpg";
        if (mimeType.equals("image/webp")) return "webp";
        if (mimeType.equals("image/gif")) return "gif";
        if (mimeType.equals("image/heic")) return "heic";
        if (mimeType.equals("image/heif")) return "heif";
        if (mimeType.equals("image/svg+xml")) return "svg";
        return "img";
    }
}
