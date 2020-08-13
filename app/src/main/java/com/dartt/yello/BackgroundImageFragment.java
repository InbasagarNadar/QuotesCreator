package com.dartt.yello;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class BackgroundImageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragment = inflater.inflate(R.layout.fragment_background_image, container, false);
        final CreateBackgroundActivity activity = (CreateBackgroundActivity) getActivity();

        final int _40dp = Variables.densityToPixel(activity, 40);
        final int _5dp = Variables.densityToPixel(activity, 5);
        //final int _1dp = Variables.densityToPixel(activity, 1);

        LinearLayout backgroundImageButtonList = fragment.findViewById(R.id.background_image_linear_layout);
        for (int i = 0; i < Variables.BackgroundImages.length; i++) {
            final int imageId = Variables.BackgroundImages[i];
            final Button button = new Button(activity);
            button.setOutlineProvider(ViewOutlineProvider.BOUNDS);
            Glide.with(this).load(imageId).override(_40dp).transform(new RoundedCorners(_5dp)).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    button.setBackground(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_40dp, _40dp);
            params.leftMargin = _5dp;
            button.setLayoutParams(params);
            backgroundImageButtonList.addView(button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.Background.loadImageFromResource(imageId);
                }
            });
        }

        return fragment;
    }
}