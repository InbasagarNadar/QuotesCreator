package com.dartt.yello;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BackgroundColorFragment extends Fragment {

    public Spinner fillTypeSpinner;
    public ImageView activeSpinnerDisplayImage;

    public HorizontalScrollView colorList;
    public HorizontalScrollView gradientList;
    public SeekBar blurSeekBar;

    CreateBackgroundActivity activity;

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

        View fragment = inflater.inflate(R.layout.fragment_background_color, container, false);
        colorList = fragment.findViewById(R.id.colorScrollView);
        gradientList = fragment.findViewById(R.id.gradientScrollView);
        blurSeekBar = fragment.findViewById(R.id.blur_seek_bar);
        activity = (CreateBackgroundActivity) getActivity();
        if (activity != null) {
            final int _5dp = Variables.densityToPixel(activity, 5);
            final int _40dp = Variables.densityToPixel(activity, 40);

            fillTypeSpinner = fragment.findViewById(R.id.background_fill_spinner);
            fillTypeSpinner.setAdapter(new BackgroundFillSelectAdapter(activity));
            fillTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    activity.Background.onFillTypeChanged(i);
                    colorList.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
                    gradientList.setVisibility(i == 1 ? View.VISIBLE : View.GONE);
                    blurSeekBar.setVisibility(i == 2 ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            LinearLayout gradientButtonList = fragment.findViewById(R.id.gradientButtonList);
            for (int i = 0; i < Variables.Gradients.length; i++) {
                Button button = new Button(activity);
                final int[] gradient = Variables.Gradients[i];
                final GradientDrawable.Orientation gradientOrientation = Variables.GradientsOrientation[i];
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.Background.setFillGradient(gradient, gradientOrientation);
                        //Variables.currentGradient = gradient;
                        //Variables.currentGradientOrientation = gradientOrientation;
                    }
                });
                button.setBackground(Variables.newGradientDrawable(activity, gradient, gradientOrientation, Variables.CORNER_DP));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_40dp, _40dp);
                params.leftMargin = _5dp;
                button.setLayoutParams(params);
                gradientButtonList.addView(button);
            }
            LinearLayout colorButtonList = fragment.findViewById(R.id.colorButtonList);
            for (int i = 0; i < Variables.Colors.length; i++) {
                Button button = new Button(activity);
                final int color = Variables.Colors[i];
                button.setBackground(Variables.newColorDrawable(activity, color, Variables.CORNER_DP));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_40dp, _40dp);
                params.leftMargin = _5dp;
                button.setLayoutParams(params);
                colorButtonList.addView(button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.Background.setFillColor(color);
                    }
                });
            }
        }
        return fragment;
    }


    class BackgroundFillSelectAdapter extends ArrayAdapter<String> {

        View spinnerDisplayView;
        LayoutInflater inflater;
        String[] labels;

        public BackgroundFillSelectAdapter(@NonNull Context context) {
            super(context, R.layout.support_simple_spinner_dropdown_item);
            inflater = LayoutInflater.from(context);
            labels = new String[]{"Solid Color", "Gradient", "Blur"};
        }


        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = inflater.inflate(R.layout.spinner_image_text, null);
            ImageView icon = view.findViewById(R.id.spinner_image);
            TextView textView = view.findViewById(R.id.spinner_text);
            icon.setClipToOutline(true);
            if (position == 0) {
                icon.setImageDrawable(Variables.newColorDrawable(activity, Variables.currentColor, Variables.CORNER_DP));
            }
            else if (position == 1) {
                icon.setImageDrawable(Variables.newGradientDrawable(activity, Variables.currentGradient, GradientDrawable.Orientation.TL_BR, Variables.CORNER_DP));
            }
            textView.setTextColor(0xFF000000);

            textView.setText(labels[position]);
            return view;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_color_select_selected, null);

            }
            activeSpinnerDisplayImage = convertView.findViewById(R.id.spinner_image);
            activeSpinnerDisplayImage.setClipToOutline(true);
            if (position == 0) {
                activeSpinnerDisplayImage.setImageDrawable(Variables.newColorDrawable(activity, Variables.currentColor, Variables.CORNER_DP));
            }
            else if (position == 1) {
                activeSpinnerDisplayImage.setImageDrawable(Variables.newGradientDrawable(activity, Variables.currentGradient, GradientDrawable.Orientation.TL_BR, Variables.CORNER_DP));
            }
            //Log.e("Hmm")
            //this.spinnerDisplayView = spinnerDisplayView;
            spinnerDisplayView = convertView;
            return convertView;
        }


        @Override
        public int getCount() {
            return 3;
        }
    }
}
