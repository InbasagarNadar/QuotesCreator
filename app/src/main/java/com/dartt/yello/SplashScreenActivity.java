package com.dartt.yello;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        new Handler().postDelayed((Runnable) () -> {
            ImageView imageView = findViewById(R.id.splash_screen_logo);
            imageView.setVisibility(View.VISIBLE);
            Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AnimatedVectorDrawableCompat) {
                AnimatedVectorDrawableCompat animatedDrawable = (AnimatedVectorDrawableCompat) imageView.getDrawable();
                animatedDrawable.start();
            }
            else if(drawable instanceof AnimatedVectorDrawable){
                AnimatedVectorDrawable animatedDrawable = (AnimatedVectorDrawable) imageView.getDrawable();
                animatedDrawable.start();

            }
            else{
                Log.d("SplashScreen", "Not Animated Vector Drawable");
            }
        }, 100);

        getSharedPreferences("splash", MODE_PRIVATE).edit().putString("version", "1.27").apply();
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreenActivity.this, TemplatesActivity.class);
            startActivity(i);
            finish();
        }, 3600);
    }
}