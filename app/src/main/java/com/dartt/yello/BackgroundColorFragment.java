package com.dartt.yello;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

public class BackgroundColorFragment extends Fragment implements View.OnClickListener {

    final String COLOR_DIALOG = "color_dialog";
    final String GRADIENT_DIALOG = "gradient_dialog";

    private static final int FILL_TYPE_COLOR = 0;
    private static final int FILL_TYPE_GRADIENT = 1;
    private int currentFillType = FILL_TYPE_COLOR;

    Spinner fillTypeSpinner;
    HorizontalScrollView colorList;
    HorizontalScrollView gradientList;
    SeekBar blurSeekBar;
    BackgroundFillSelectAdapter backgroundFillSelectAdapter;

    CreateBackgroundActivity activity;

    ColorDialogFragment.Listener colorDialogListener = (color) -> {
        activity.undo.addState(new ColorChangeState(Variables.currentColor, color));
        setArtBoardFill(color);
    };

    GradientDialogFragment.Listener gradientDialogListener = this::setArtBoardFill;

    public BackgroundColorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (CreateBackgroundActivity) getActivity();
        View fragment = inflater.inflate(R.layout.fragment_background_color, container, false);
        colorList = fragment.findViewById(R.id.colorScrollView);
        gradientList = fragment.findViewById(R.id.gradientScrollView);
        blurSeekBar = fragment.findViewById(R.id.blur_seek_bar);
        fillTypeSpinner = fragment.findViewById(R.id.background_fill_spinner);


        final int _5dp = Variables.densityToPixel(activity, 5);
        final int _40dp = Variables.densityToPixel(activity, 40);


        backgroundFillSelectAdapter = new BackgroundFillSelectAdapter(activity);
        fillTypeSpinner.setAdapter(backgroundFillSelectAdapter);

        Intent intent = activity.getIntent();
        if (intent.hasExtra("draft_key")) {
            String draftKey = intent.getStringExtra("draft_key");
            SharedPreferences sharedPref = activity.getSharedPreferences("drafts", Context.MODE_PRIVATE);
            try {
                JSONObject draft = new JSONObject(sharedPref.getString(draftKey, "{}"));
                fillTypeSpinner.setSelection(draft.getJSONObject("editable_json").getJSONObject("artBoard").getInt("fillType"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        fillTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (activity != null) {
                    if (activity.Background != null) {
                        switch (i) {
                            case 0:
                                if (currentFillType != FILL_TYPE_COLOR) {
                                    activity.undo.addState(new ColorChangeState(Variables.currentGradient, Variables.currentGradientOrientation, Variables.currentColor));
                                }
                                setArtBoardFill(Variables.currentColor);
                                break;
                            case 1:
                                if (currentFillType != FILL_TYPE_GRADIENT) {
                                    activity.undo.addState(new ColorChangeState(Variables.currentColor, Variables.currentGradient, Variables.currentGradientOrientation));
                                }
                                setArtBoardFill(Variables.currentGradient, Variables.currentGradientOrientation);
                                break;
                        }
                        colorList.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
                        gradientList.setVisibility(i == 1 ? View.VISIBLE : View.GONE);
                        blurSeekBar.setVisibility(i == 2 ? View.VISIBLE : View.GONE);
                        Log.e("TAG", "index" + i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        LinearLayout colorButtonList = fragment.findViewById(R.id.colorButtonList);
        for (int i = 0; i < Variables.Colors.length; i++) {
            Button button = new Button(activity);
            final int color = Variables.Colors[i];
            button.setBackground(Variables.newColorDrawable(activity, color, Variables.CORNER_DP));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_40dp, _40dp);
            params.leftMargin = _5dp;
            button.setLayoutParams(params);
            colorButtonList.addView(button);
            button.setOnClickListener(view -> {
                activity.undo.addState(new ColorChangeState(Variables.currentColor, color));
                setArtBoardFill(color);
            });
        }

        LinearLayout gradientButtonList = fragment.findViewById(R.id.gradientButtonList);
        for (int i = 0; i < Variables.Gradients.length; i++) {
            Button button = new Button(activity);
            final int[] gradient = Variables.Gradients[i];
            final GradientDrawable.Orientation gradientOrientation = Variables.GradientsOrientation[i];
            button.setOnClickListener(view -> {
                if (activity != null) {
                    activity.undo.addState(new ColorChangeState(Variables.currentGradient, Variables.currentGradientOrientation, gradient, gradientOrientation));
                    setArtBoardFill(gradient, gradientOrientation);
                }
            });
            button.setBackground(Variables.newGradientDrawable(activity, gradient, gradientOrientation, Variables.CORNER_DP));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_40dp, _40dp);
            params.leftMargin = _5dp;
            button.setLayoutParams(params);
            gradientButtonList.addView(button);
        }

        fragment.findViewById(R.id.img_btn_pick_art_board_gradient).setOnClickListener(this);
        fragment.findViewById(R.id.img_btn_pick_art_board_color).setOnClickListener(this);

        if (savedInstanceState != null) {
            ColorDialogFragment colorDialogFragment = ((ColorDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(COLOR_DIALOG));
            if (colorDialogFragment != null) {
                colorDialogFragment.listener = colorDialogListener;
            }
            GradientDialogFragment gradientDialogFragment = ((GradientDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(GRADIENT_DIALOG));
            if (gradientDialogFragment != null) {
                gradientDialogFragment.listener = gradientDialogListener;
            }
        }

        return fragment;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();

        if (id == R.id.img_btn_pick_art_board_color) {
            Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(COLOR_DIALOG);
            if (prev == null) {
                ColorDialogFragment dialogFragment = ColorDialogFragment.newInstance(Variables.currentColor, colorDialogListener);
                dialogFragment.show(ft, COLOR_DIALOG);
            }

        }
        else if (id == R.id.img_btn_pick_art_board_gradient) {
            Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(GRADIENT_DIALOG);
            if (prev == null) {
                GradientDialogFragment dialogFragment = new GradientDialogFragment();
                dialogFragment.listener = (gradient, gradientOrientation) -> {
                    activity.undo.addState(new ColorChangeState(Variables.currentGradient, Variables.currentGradientOrientation, gradient, gradientOrientation));
                    setArtBoardFill(gradient, gradientOrientation);
                };
                dialogFragment.setCancelable(false);
                dialogFragment.show(ft, GRADIENT_DIALOG);
            }
        }

    }

    void setArtBoardFill(int color) {
        activity.setEditorArtBoardFill(color);
        currentFillType = FILL_TYPE_COLOR;
        ((BackgroundFillSelectAdapter) fillTypeSpinner.getAdapter()).notifyDataSetChanged();
    }

    void setArtBoardFill(int[] gradient, GradientDrawable.Orientation orientation) {
        activity.setEditorArtBoardFill(gradient, orientation);
        currentFillType = FILL_TYPE_GRADIENT;
        ((BackgroundFillSelectAdapter) fillTypeSpinner.getAdapter()).notifyDataSetChanged();
    }


    class ColorChangeState implements Undo.Undoable {

        private static final int TYPE_COLOR_COLOR = 0;
        private static final int TYPE_COLOR_GRADIENT = 1;
        private static final int TYPE_GRADIENT_COLOR = 2;
        private static final int TYPE_GRADIENT_GRADIENT = 3;
        private final int actionType;

        int oldColor;
        int newColor;
        int[] oldGradient;
        GradientDrawable.Orientation oldGradientOrientation;
        int[] newGradient;
        GradientDrawable.Orientation newGradientOrientation;

        ColorChangeState(int oldColor, int newColor) {
            this.oldColor = oldColor;
            this.newColor = newColor;
            actionType = TYPE_COLOR_COLOR;
        }

        ColorChangeState(int[] oldGradient, GradientDrawable.Orientation oldGradientOrientation, int[] newGradient, GradientDrawable.Orientation newGradientOrientation) {
            this.oldGradient = oldGradient;
            this.oldGradientOrientation = oldGradientOrientation;
            this.newGradient = newGradient;
            this.newGradientOrientation = newGradientOrientation;
            actionType = TYPE_GRADIENT_GRADIENT;
        }

        ColorChangeState(int oldColor, int[] newGradient, GradientDrawable.Orientation newGradientOrientation) {
            this.oldColor = oldColor;
            this.newGradient = newGradient;
            this.newGradientOrientation = newGradientOrientation;
            actionType = TYPE_COLOR_GRADIENT;
        }

        ColorChangeState(int[] oldGradient, GradientDrawable.Orientation oldGradientOrientation, int newColor) {
            this.oldGradient = oldGradient;
            this.oldGradientOrientation = oldGradientOrientation;
            this.newColor = newColor;
            actionType = TYPE_GRADIENT_COLOR;
        }

        @Override
        public void undo() {
            if (actionType == TYPE_COLOR_COLOR) {
                setArtBoardFill(oldColor);
            }
            else if (actionType == TYPE_GRADIENT_GRADIENT) {
                setArtBoardFill(oldGradient, oldGradientOrientation);
            }
            else if (actionType == TYPE_COLOR_GRADIENT) {
                fillTypeSpinner.setSelection(0);
                setArtBoardFill(oldColor);
            }
            else if (actionType == TYPE_GRADIENT_COLOR) {
                fillTypeSpinner.setSelection(1);
                setArtBoardFill(oldGradient, oldGradientOrientation);
            }
        }

        @Override
        public void redo() {
            if (actionType == TYPE_COLOR_COLOR) {
                setArtBoardFill(newColor);
            }
            else if (actionType == TYPE_GRADIENT_GRADIENT) {
                setArtBoardFill(newGradient, newGradientOrientation);
            }
            else if (actionType == TYPE_COLOR_GRADIENT) {
                fillTypeSpinner.setSelection(1);
                setArtBoardFill(newGradient, newGradientOrientation);
            }
            else if (actionType == TYPE_GRADIENT_COLOR) {
                fillTypeSpinner.setSelection(0);
                setArtBoardFill(newColor);
            }
        }
    }

    class BackgroundFillSelectAdapter extends ArrayAdapter<String> {

        View spinnerDisplayView;
        LayoutInflater inflater;
        final String[] labels = new String[]{"Solid Color", "Gradient"};

        public BackgroundFillSelectAdapter(@NonNull Context context) {
            super(context, R.layout.support_simple_spinner_dropdown_item);
            inflater = LayoutInflater.from(context);
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
            ImageView activeSpinnerDisplayImage = convertView.findViewById(R.id.spinner_image);
            activeSpinnerDisplayImage.setClipToOutline(true);
            if (position == 0) {
                activeSpinnerDisplayImage.setImageDrawable(Variables.newColorDrawable(activity, Variables.currentColor, Variables.CORNER_DP));
            }
            else if (position == 1) {
                activeSpinnerDisplayImage.setImageDrawable(Variables.newGradientDrawable(activity, Variables.currentGradient, GradientDrawable.Orientation.TL_BR, Variables.CORNER_DP));
            }
            spinnerDisplayView = convertView;
            return convertView;
        }


        @Override
        public int getCount() {
            return 2;
        }
    }
}
