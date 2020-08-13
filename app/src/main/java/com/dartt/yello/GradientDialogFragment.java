package com.dartt.yello;

import android.content.Context;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dartt.yello.views.CustomColorPickerView;
import com.jaredrummler.android.colorpicker.ColorPickerView;

public class GradientDialogFragment extends DialogFragment {

    Orientation[] orientations = new Orientation[]{Orientation.LEFT_RIGHT, Orientation.BL_TR, Orientation.BOTTOM_TOP, Orientation.BR_TL, Orientation.RIGHT_LEFT, Orientation.TR_BL, Orientation.TOP_BOTTOM, Orientation.TL_BR};

    final float LUMINANCE_POINT = 100;
    final int MAX_GRADIENT_COLORS = 7;
    int colorCount = 2;
    Orientation currentOrientation;
    CreateBackgroundActivity activity;
    LinearLayout colors_parent;
    CustomColorPickerView colorPickerView;

    ImageView newGradientImageView;
    Button hexCodeButton;
    Button okButton;
    TextView colorCountTextView;
    Spinner orientationSpinner;
    Button increaseColorButton;
    Button decreaseColorButton;

    Button[] colorButtons;

    int[] colors;

    int currentColorButtonIndex = 0;

    GradientDialogListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gradient, container, false);
        activity = (CreateBackgroundActivity) getActivity();
        colorPickerView = v.findViewById(R.id.color_picker);
        colorCountTextView = v.findViewById(R.id._color_count_text_view);
        increaseColorButton = v.findViewById(R.id._increase_color_count);
        decreaseColorButton = v.findViewById(R.id._decrease_color_count);
        colors_parent = v.findViewById(R.id.color_parent);
        newGradientImageView = v.findViewById(R.id.new_gradient_image);
        okButton = v.findViewById(R.id.confirm_button);
        hexCodeButton = v.findViewById(R.id.hex_code_button);
        orientationSpinner = v.findViewById(R.id.orientation_spinner);

        currentOrientation = Variables.currentGradientOrientation;
        colors = new int[MAX_GRADIENT_COLORS];
        colorCount = Variables.currentGradient.length;
        colorButtons = new Button[MAX_GRADIENT_COLORS];
        final int _5dp = Variables.densityToPixel(activity, 5);
        final int _40dp = Variables.densityToPixel(activity, 40);
        for (int i = 0; i < MAX_GRADIENT_COLORS; i++) {
            final int index = i;
            Button button = new Button(activity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_40dp, _40dp);
            params.rightMargin = _5dp;
            button.setLayoutParams(params);
            button.setPadding(0, 0, 0, 0);
            button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            button.setGravity(Gravity.CENTER);
            if (i >= colorCount) {
                button.setVisibility(View.GONE);
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onColorButtonClick(index);
                }
            });
            colorButtons[i] = button;
            colors_parent.addView(button);
            int color = Variables.currentGradient[Math.min(i, colorCount - 1)];
            if (luminanceOf(color) > LUMINANCE_POINT) {
                button.setTextColor(0xFF000000);
            }
            else {
                button.setTextColor(0xFFFFFFFF);
            }
            setButtonColor(i, color);
        }
        onColorButtonClick(0);

        changeColorCount(0);
        increaseColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeColorCount(1);
            }
        });
        decreaseColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeColorCount(-1);
            }
        });

        colorPickerView.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                setButtonColor(currentColorButtonIndex, newColor);
                updateHex(newColor);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    int[] gradient = new int[colorCount];
                    System.arraycopy(colors, 0, gradient, 0, colorCount);
                    listener.onGradientSelected(gradient, currentOrientation);
                }
                dismiss();
            }
        });
        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        orientationSpinner.setAdapter(new OrientationSelectAdapter(activity));
        for (int i = 0; i < orientations.length; i++) {
            if (orientations[i] == currentOrientation) {
                orientationSpinner.setSelection(i);
                break;
            }
        }
        orientationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentOrientation = orientations[i];
                refreshGradientImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return v;
    }

    float luminanceOf(int color) {
        float r = (color >> 16) & 0xFF;
        float g = (color >> 8) & 0xFF;
        float b = color & 0xFF;
        return (float) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    void onColorButtonClick(int index) {
        currentColorButtonIndex = index;
        colorPickerView.setColor(colors[index]);
        updateHex(colors[index]);
        for (Button colorButton : colorButtons) {
            colorButton.setText("");
        }
        colorButtons[index].setText("◉");

    }

    void updateHex(int newColor) {
        hexCodeButton.setBackground(Variables.newColorDrawable(activity, newColor, Variables.CORNER_DP));
        hexCodeButton.setText(String.format("#%s", Integer.toHexString(newColor).substring(2).toUpperCase()));
        if (luminanceOf(newColor) > LUMINANCE_POINT) {
            hexCodeButton.setTextColor(0xFF000000);
        }
        else {
            hexCodeButton.setTextColor(0xFFFFFFFF);
        }
    }

    void setButtonColor(int index, int color) {
        colors[index] = color;
        if (luminanceOf(color) > LUMINANCE_POINT) {
            colorButtons[index].setTextColor(0xFF000000);
        }
        else {
            colorButtons[index].setTextColor(0xFFFFFFFF);
        }
        colorButtons[index].setBackground(Variables.newColorDrawable(activity, color, Variables.CORNER_DP));
        refreshGradientImage();
    }

    private void changeColorCount(int i) {
        colorCount += i;
        increaseColorButton.setEnabled(colorCount < MAX_GRADIENT_COLORS);
        decreaseColorButton.setEnabled(colorCount > 2);
        colorCountTextView.setText(String.valueOf(colorCount));
        if (i == 1) {
            colorButtons[colorCount - 1].setVisibility(View.VISIBLE);
        }
        else if (i == -1) {
            colorButtons[colorCount].setVisibility(View.GONE);
            if (currentColorButtonIndex >= colorCount) {
                onColorButtonClick(colorCount - 1);
            }
        }
        refreshGradientImage();
    }

    void refreshGradientImage() {
        int[] gradient = new int[colorCount];
        System.arraycopy(colors, 0, gradient, 0, colorCount);
        newGradientImageView.setImageDrawable(Variables.newGradientDrawable(activity, gradient, currentOrientation, Variables.CORNER_DP));
    }

    interface GradientDialogListener {
        void onGradientSelected(int[] gradient, Orientation orientation);
    }

    class OrientationSelectAdapter extends ArrayAdapter<String> {

        View spinnerDisplayView;
        LayoutInflater inflater;
        String[] labels;

        public OrientationSelectAdapter(@NonNull Context context) {
            super(context, R.layout.support_simple_spinner_dropdown_item);
            inflater = LayoutInflater.from(context);
            labels = new String[]{"0°", "45°", "90°", "135°", "180°", "225°", "270°", "315°"};
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = inflater.inflate(R.layout.spinner_image_text, null);
            ImageView icon = view.findViewById(R.id.spinner_image);
            TextView textView = view.findViewById(R.id.spinner_text);
            icon.setClipToOutline(true);
            int[] gradient = new int[colorCount];
            System.arraycopy(colors, 0, gradient, 0, colorCount);
            icon.setImageDrawable(Variables.newGradientDrawable(activity, gradient, orientations[position], Variables.CORNER_DP));

            textView.setTextColor(0xFF000000);

            textView.setText(labels[position]);
            return view;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, null);
            }
            ((TextView) convertView).setText(labels[position]);
            ((TextView) convertView).setGravity(Gravity.CENTER);
            spinnerDisplayView = convertView;
            return convertView;
        }


        @Override
        public int getCount() {
            return labels.length;
        }
    }
}
