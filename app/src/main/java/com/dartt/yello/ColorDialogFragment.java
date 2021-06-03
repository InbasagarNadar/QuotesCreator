package com.dartt.yello;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.dartt.yello.views.CustomColorPickerView;
import com.jaredrummler.android.colorpicker.ColorPickerView;

public class ColorDialogFragment extends DialogFragment {

    int oldColor;
    final float LUMINANCE_POINT = 100;
    CreateBackgroundActivity activity;
    LinearLayout colors_parent;
    CustomColorPickerView colorPickerView;

    Button oldColorButton;
    Button hexCodeButton;
    Button okButton;
    TextView colorCountTextView;


    Listener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_color, container, false);
        oldColor = getArguments().getInt("color");
        activity = (CreateBackgroundActivity) getActivity();
        colorPickerView = v.findViewById(R.id.color_picker);
        colorCountTextView = v.findViewById(R.id._color_count_text_view);
        colors_parent = v.findViewById(R.id.color_parent);

        okButton = v.findViewById(R.id.confirm_button);
        hexCodeButton = v.findViewById(R.id.new_color_image);
        oldColorButton = v.findViewById(R.id.old_color_image);
        oldColorButton.setBackground(Variables.newColorDrawable(activity, oldColor, Variables.CORNER_DP));
        oldColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColor(oldColor);
            }
        });

        colorPickerView.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                updateHex(newColor);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onColorSelected(colorPickerView.getColor());
                dismiss();
            }
        });
        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        if (savedInstanceState == null) {
            setColor(oldColor);
        } else {
            setColor(savedInstanceState.getInt("selected_color"));
        }
        oldColorButton.setText(String.format("#%s", Integer.toHexString(oldColor).substring(2).toUpperCase()));
        if (GraphicsHelper.luminanceOf(oldColor) > LUMINANCE_POINT) {
            oldColorButton.setTextColor(Color.BLACK);
        }
        else {
            oldColorButton.setTextColor(Color.WHITE);
        }
        return v;
    }


    void updateHex(int newColor) {
        hexCodeButton.setBackground(Variables.newColorDrawable(activity, newColor, Variables.CORNER_DP));
        hexCodeButton.setText(String.format("#%s", Integer.toHexString(newColor).substring(2).toUpperCase()));
        if (GraphicsHelper.luminanceOf(newColor) > LUMINANCE_POINT) {
            hexCodeButton.setTextColor(0xFF000000);
        }
        else {
            hexCodeButton.setTextColor(0xFFFFFFFF);
        }
    }

    void setColor(int color) {
        updateHex(color);
        colorPickerView.setColor(color);
    }

    public static ColorDialogFragment newInstance(int color, Listener listener) {
        Bundle args = new Bundle();
        args.putInt("color", color);
        ColorDialogFragment fragment = new ColorDialogFragment();
        fragment.listener = listener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_color", colorPickerView.getColor());
    }

    interface Listener {
        void onColorSelected(int color);
    }

}
