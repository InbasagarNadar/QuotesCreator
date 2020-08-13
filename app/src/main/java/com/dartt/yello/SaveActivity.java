package com.dartt.yello;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SaveActivity extends AppCompatActivity {

    ImageView imageView;
    String savedFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        imageView = findViewById(R.id.image_view);
        imageView.setImageBitmap(CreateBackgroundActivity.bitmap);

    }

    public void onSaveClick(View view) {
        try {
            if (savedFilePath.equals("")) {
                savedFilePath = saveImage(CreateBackgroundActivity.bitmap, "quote");
                Toast.makeText(this, "Saved at " + savedFilePath, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Already saved at " + savedFilePath, Toast.LENGTH_LONG).show();
            }
        }
        catch (IOException e) {
            Toast.makeText(this, "Something went wrong :/" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        /*File filesDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        assert filesDir != null;
        if (!filesDir.exists()) {
            if (filesDir.   mkdirs()) {
            }
        }
        File file = new File(filesDir, "quote.jpg");
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Cant able to create file");
                }
            }
            OutputStream os = new FileOutputStream(file);
            CreateBackgroundActivity.bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.close();
            Log.e("TAG", "File Path= " + file.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void onShareClick(View view) {

        File cachePath = new File(getExternalCacheDir(), "image/");
        cachePath.mkdirs();

        //create png file
        File file = new File(cachePath, "Yello.png");
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            CreateBackgroundActivity.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hmmmmm", Toast.LENGTH_SHORT).show();
        }

        try {
            Uri myImageFileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

            //create a intent
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri);
            intent.setType("image/png");

            startActivity(Intent.createChooser(intent, "Share with"));
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    final String IMAGES_FOLDER_NAME = "Yello";

    private String saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        String path;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
            path = imageUri.getPath() + "/" + name + ".png";
        }
        else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);
            path = image.getAbsolutePath();
        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
        return path;
    }
}