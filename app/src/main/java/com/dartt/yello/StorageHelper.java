package com.dartt.yello;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class StorageHelper {

    public static final String IMAGES_FOLDER_NAME = "Yello";


//    public static boolean getAskedPermission(Context context){
//        SharedPreferences sharedPreferences = context.getSharedPreferences("permissions", MODE_PRIVATE);
//        return sharedPreferences.getBoolean("asked_storage_permission", false);
//    }
//
//    public static void setAskedPermission(Context context, boolean asked){
//        SharedPreferences sharedPreferences = context.getSharedPreferences("permissions", MODE_PRIVATE);
//        sharedPreferences.edit().putBoolean("asked_storage_permission", asked).apply();
//    }

    interface PermissionResultCallback {
        void onResult(String permission, boolean granted);

    }

    public static boolean hasImageReadPermission(Context context) {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        }
        else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean hasWritePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Toast.makeText(context, "HasPermission: true", Toast.LENGTH_LONG).show();
            return true;
        }
        else {
            //Toast.makeText(context, "HasPermissionVer: " + hp, Toast.LENGTH_LONG).show();
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }


    public static void requestImageReadPermission(ActivityResultLauncher<String> resultLauncher) {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        }
        else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        resultLauncher.launch(permission);
    }

    public static void requestWritePermission(ActivityResultLauncher<String> resultLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d("WritePermission", "Can't request permission on >=R");
        }
        else {
            resultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    public static ActivityResultLauncher<String> registerWritePermissionResultLauncher(ActivityResultCaller caller, PermissionResultCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d("WritePermission", "Can't request permission on >=R");
            return null;
        }
        else {
            return registerResultLauncher(caller, callback, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    public static ActivityResultLauncher<String> registerImagePermissionResultLauncher(ActivityResultCaller caller, PermissionResultCallback callback) {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        }
        else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        return registerResultLauncher(caller, callback, permission);
    }

    private static ActivityResultLauncher<String> registerResultLauncher(ActivityResultCaller caller, PermissionResultCallback callback, String permission) {
        return caller.registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> callback.onResult(permission, result));
    }

    public static void openAppInfoSystemSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }


    static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("StorageHelper", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    static String saveBitmap(Context context, View view) throws IOException {
        int targetWidth = 1080;
        int targetHeight = 1080;
        float scale = targetWidth / ((float) view.getWidth());

        Bitmap originalImage = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(originalImage);
        bitmapCanvas.scale(scale, scale);
        view.draw(bitmapCanvas);

        String fileName = "shayari_" + System.currentTimeMillis();
        String path;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
            path = StorageHelper.getRealPathFromURI(context, imageUri);
        }
        else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;
            File fileDirectory = new File(imagesDir);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }
            File image = new File(imagesDir, fileName + ".jpg");
            fos = new FileOutputStream(image);
            path = image.getAbsolutePath();
        }
        originalImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        if (fos != null) {
            fos.flush();
            fos.close();
        }
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
        return path;

    }

    static void shareFile(Context context, File imageFile, String textData) {
        Uri myImageFileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider", imageFile);

        //create a intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri);
        intent.putExtra(Intent.EXTRA_TEXT, textData);
        intent.setType("image/jpeg");
        Intent chooser = Intent.createChooser(intent, "Share with");

        //To remove that random error telling grant uri permission
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, myImageFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        //If app exists to share image then share
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
    }
}
