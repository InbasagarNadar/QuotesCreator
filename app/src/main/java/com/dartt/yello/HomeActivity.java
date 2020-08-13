package com.dartt.yello;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;

public class HomeActivity extends AppCompatActivity {

    final int QUOTE_FROM_IMAGE_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void onQuoteFromImageClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), QUOTE_FROM_IMAGE_REQUEST_CODE);
    }

    public void onQuoteFromColorClick(View view) {
        Intent intent = new Intent(this, CreateBackgroundActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (requestCode == QUOTE_FROM_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, CreateBackgroundActivity.class);
            intent.setData(resultIntent.getData());
            startActivity(intent);
            if (false) {
                Uri imageUri = resultIntent.getData();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                try {
                    BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri),
                            null,
                            options);
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                float imageAspectRatio = (float) imageWidth / (float) imageHeight;
            }
        }
    }

}