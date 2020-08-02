package com.radangames.quotescreator;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BackgroundColorFragment extends Fragment {

    public BackgroundColorFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final CreateBackgroundActivity activity = (CreateBackgroundActivity) getActivity();

        View fragment = inflater.inflate(R.layout.fragment_background_color, container, false);
        LinearLayout gradientButtonList = fragment.findViewById(R.id.gradientButtonList);
        Context context = getContext();
        for (int i = 0; i < Variables.Gradients.length; i++) {
            Button button = new Button(context);
            final int[] gradient = Variables.Gradients[i];
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.Background.setFillGradient(gradient);
                }
            });
            //button.setBackgroundColor(Variables.Colors[i]);
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, gradient);
            gradientDrawable.setCornerRadius(Variables.densityToPixel(5));
            button.setBackground(gradientDrawable);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Variables.densityToPixel(40), Variables.densityToPixel(40));
            params.leftMargin = Variables.densityToPixel(5);
            button.setLayoutParams(params);
            gradientButtonList.addView(button);
        }
        LinearLayout colorButtonList = fragment.findViewById(R.id.colorButtonList);
        for (int i = 0; i < Variables.Gradients.length; i++) {
            Button button = new Button(context);
            final int color = Variables.Colors[i];
            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setColor(color);
            colorDrawable.setCornerRadius(Variables.densityToPixel(5));
            button.setBackground(colorDrawable);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Variables.densityToPixel(40), Variables.densityToPixel(40));
            params.leftMargin = Variables.densityToPixel(5);
            button.setLayoutParams(params);
            colorButtonList.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.Background.setFillColor(color);
                }
            });
        }
        return fragment;
    }
}