package com.dartt.yello;

import androidx.fragment.app.DialogFragment;

public class ColorDialogFragment extends DialogFragment {


/*


    CreateBackgroundActivity activity;
    LinearLayout colors_parent;
    CustomColorPickerView colorPickerView;

    ImageView oldColorImageView;
    Button hexCodeButton;
    Button okButton;
    TextView colorCountTextView;

    ColorDialogListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_color, container, false);
        activity = (CreateBackgroundActivity) getActivity();
        colorPickerView = v.findViewById(R.id.color_picker);
        colorCountTextView = v.findViewById(R.id._color_count_text_view);
        colors_parent = v.findViewById(R.id.color_parent);

        okButton = v.findViewById(R.id.confirm_button);
        hexCodeButton = v.findViewById(R.id.hex_code_button);

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
                dismiss();
            }
        });
        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return v;
    }

    void setColorViewColor(int newColor){
        colorPickerView.setColor(newColor);
    }

    float luminanceOf(int color) {
        float r = (color >> 16) & 0xFF;
        float g = (color >> 8) & 0xFF;
        float b = color & 0xFF;
        return (float) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    void updateHex(int newColor) {
        hexCodeButton.setBackground(Variables.newColorDrawable(activity, newColor, Variables.CORNER_DP));
        hexCodeButton.setText(String.format("#%s", Integer.toHexString(newColor).substring(2).toUpperCase()));
        if(luminanceOf(newColor)>LUMINANCE_POINT) {
            hexCodeButton.setTextColor(0xFF000000);
        }
        else{
            hexCodeButton.setTextColor(0xFFFFFFFF);
        }
    }

    void setButtonColor(int index, int color) {
        colors[index] = color;
        if(luminanceOf(color)>LUMINANCE_POINT) {
            colorButtons[index].setTextColor(0xFF000000);
        }
        else{
            colorButtons[index].setTextColor(0xFFFFFFFF);
        }
        colorButtons[index].setBackground(Variables.newColorDrawable(activity, color, Variables.CORNER_DP));
        refreshGradientImage();
    }


    interface ColorDialogListener {
        void onColorSelected(int[] gradient, Orientation orientation);
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
    }*/
}
