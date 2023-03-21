package com.dartt.yello;


import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class BackgroundImageFragment extends Fragment implements View.OnClickListener {

    private static final int INTERNET_IMAGE_REQUEST_CODE = 0;
    private static final int SELECT_BACKGROUND_IMAGE_REQUEST_CODE = 1;
    CreateBackgroundActivity activity;
    ImageButton btnRemoveBackground;
    HorizontalScrollView horizontalScrollView;
    ActivityResultLauncher<String> imagePermissionActivityResultLauncher;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePermissionActivityResultLauncher = StorageHelper.registerImagePermissionResultLauncher(this, (permission, granted) -> {
            if (granted) {
                Toast.makeText(activity, "Granted", Toast.LENGTH_LONG).show();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Please grant storage permission to import image from gallery");
                builder.setPositiveButton("Okay", (dialog, id1) -> {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        StorageHelper.requestImageReadPermission(imagePermissionActivityResultLauncher);
                    }
                    else {
                        StorageHelper.openAppInfoSystemSetting(activity);
                    }
                });
                builder.create().show();
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragment = inflater.inflate(R.layout.fragment_background_image, container, false);
        activity = (CreateBackgroundActivity) getActivity();

        horizontalScrollView = fragment.findViewById(R.id.horizontal_scroll_view);

        final int _40dp = Variables.densityToPixel(activity, 40);
        final int _5dp = Variables.densityToPixel(activity, 5);
        final int _2dp = Variables.densityToPixel(activity, 2);

        btnRemoveBackground = fragment.findViewById(R.id.btn_remove_background);

        LinearLayout backgroundImageButtonList = fragment.findViewById(R.id.background_image_linear_layout);
        for (int i = 0; i < Variables.BackgroundImages.length; i++) {
            final int imageId = Variables.BackgroundImages[i];
            final ImageButton button = new ImageButton(activity);
            button.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            button.setElevation(_2dp);
            button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_rounded_corner, null));
            button.setClipToOutline(true);
            Glide.with(this).load(imageId).centerCrop().override(_40dp).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    button.setImageDrawable(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_40dp, _40dp);
            params.rightMargin = _5dp;
            button.setLayoutParams(params);
            backgroundImageButtonList.addView(button);

            button.setOnClickListener(view -> {
                //activity.Background.loadImageFromResource(imageId);
                activity.setEditorBackground(imageId);
                btnRemoveBackground.setVisibility(View.VISIBLE);
            });
        }
        /*View buttonShowInternetImagePicker = fragment.findViewById(R.id.button_show_internet_image_picker);
        backgroundImageButtonList.removeView(buttonShowInternetImagePicker);
        backgroundImageButtonList.addView(buttonShowInternetImagePicker);*/

        fragment.findViewById(R.id.img_btn_gallery_background).setOnClickListener(this);
        fragment.findViewById(R.id.btn_remove_background).setOnClickListener(this);
        fragment.findViewById(R.id.button_show_internet_image_picker).setOnClickListener(this);

        /*if(activity.editorFragment.backgroundViewContainer.currentBackgroundType != ImageViewContainer.BACKGROUND_TYPE_NULL){
            btnRemoveBackground.setVisibility(View.VISIBLE);
        }*/

        if (savedInstanceState != null) {
            horizontalScrollView.setScrollX(savedInstanceState.getInt("horizontal_scroll"));
            btnRemoveBackground.setVisibility(savedInstanceState.getInt("button_delete_visibility"));
        }


        return fragment;
    }

    void showGalleryImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_BACKGROUND_IMAGE_REQUEST_CODE);
    }

    public void resetState(ImageViewContainer viewContainer) {
        if (viewContainer.currentBackgroundType == ImageViewContainer.BACKGROUND_TYPE_NULL) {
            btnRemoveBackground.setVisibility(View.GONE);
        }
        else {
            btnRemoveBackground.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_remove_background) {
            btnRemoveBackground.setVisibility(View.GONE);
            activity.onRemoveBackgroundClick(view);
        }
        else if (id == R.id.button_show_internet_image_picker) {
            // Intent intent = new Intent(activity, ServerImagesActivity.class);
            Intent intent = new Intent(activity, ImageSearchActivity.class);
            startActivityForResult(intent, INTERNET_IMAGE_REQUEST_CODE);
        }
        else if (id == R.id.img_btn_gallery_background) {
            if (StorageHelper.hasImageReadPermission(activity)) {
                showGalleryImagePicker();
            }
            else {
                StorageHelper.requestImageReadPermission(imagePermissionActivityResultLauncher);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (resultCode == RESULT_OK) {
            if (requestCode == INTERNET_IMAGE_REQUEST_CODE) {
                activity.setEditorBackground(resultIntent.getExtras().getString("image_url"));
                btnRemoveBackground.setVisibility(View.VISIBLE);
            }
            else if (requestCode == SELECT_BACKGROUND_IMAGE_REQUEST_CODE) {
                activity.setEditorBackground(resultIntent.getData());
                btnRemoveBackground.setVisibility(View.VISIBLE);
            }
            Log.e("Frag", "OnActivityResult");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("horizontal_scroll", horizontalScrollView.getScrollX());
        outState.putInt("button_delete_visibility", btnRemoveBackground.getVisibility());
    }
}