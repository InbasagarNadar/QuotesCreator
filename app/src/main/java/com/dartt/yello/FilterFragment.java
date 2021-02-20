package com.dartt.yello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.util.Locale;

public class FilterFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    Filter imageFilter = new Filter();
    SaturationSubfilter saturationSubFilter = new SaturationSubfilter(1); // [0 - 3]
    ContrastSubFilter contrastSubFilter = new ContrastSubFilter(1); //[.25 - 2.5]
    BrightnessSubFilter brightnessSubFilter = new BrightnessSubFilter(0); //[-100, 100]

    FilterListener listener;

    SeekBar contrastSeekBar;
    SeekBar brightnessSeekBar;
    SeekBar saturationSeekBar;

    TextView contrastTextView;
    TextView brightnessTextView;
    TextView saturationTextView;

    ImageButton[] buttons;
    ConstraintLayout[] seekBarParents;

    public FilterFragment() {
    }

    public static FilterFragment newInstance(FilterListener listener) {
        FilterFragment fragment = new FilterFragment();
        fragment.listener = listener;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        imageFilter.addSubFilter(contrastSubFilter);
        imageFilter.addSubFilter(saturationSubFilter);
        imageFilter.addSubFilter(brightnessSubFilter);

        contrastSeekBar = view.findViewById(R.id.contrast_seekbar);
        brightnessSeekBar = view.findViewById(R.id.brightness_seekbar);
        saturationSeekBar = view.findViewById(R.id.saturation_seekbar);
        contrastTextView = view.findViewById(R.id.contrast_text);
        brightnessTextView = view.findViewById(R.id.brightness_text);
        saturationTextView = view.findViewById(R.id.saturation_text);

        contrastSeekBar.setMax(150);
        brightnessSeekBar.setMax(200);
        saturationSeekBar.setMax(100);
        resetFilter();
        contrastSeekBar.setOnSeekBarChangeListener(this);
        brightnessSeekBar.setOnSeekBarChangeListener(this);
        saturationSeekBar.setOnSeekBarChangeListener(this);

        buttons = new ImageButton[3];
        buttons[0] = view.findViewById(R.id.button_contrast);
        buttons[1] = view.findViewById(R.id.button_brightness);
        buttons[2] = view.findViewById(R.id.button_saturation);
        for (int i = 0; i < 3; i++) {
            buttons[i].setOnClickListener(this);
        }
        seekBarParents = new ConstraintLayout[3];
        seekBarParents[0] = view.findViewById(R.id._contrast_panel);
        seekBarParents[1] = view.findViewById(R.id._brightness_panel);
        seekBarParents[2] = view.findViewById(R.id._saturation_panel);

        return view;
    }

    public void resetFilter() {
        contrastSeekBar.setProgress(75);
        brightnessSeekBar.setProgress(100);
        saturationSeekBar.setProgress(50);
        contrastSubFilter.setContrast(1);
        saturationSubFilter.setLevel(1);
        brightnessSubFilter.setBrightness(0);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (seekBar.equals(contrastSeekBar)) {
            float contrast = .25f + i / 100f;
            contrastSubFilter.setContrast(contrast);
            contrastTextView.setText(String.format(Locale.ENGLISH, "%.2f", contrast));
        } else if (seekBar.equals(brightnessSeekBar)) {
            int brightness = i - 100;
            brightnessSubFilter.setBrightness(brightness);
            brightnessTextView.setText(String.format(Locale.ENGLISH, "%d", brightness));
        } else {
            float saturation;
            if (i <= 50) {
                saturation = i * .02f;
            } else {
                saturation = 1 + (i - 50) * .04f;
            }
            saturationSubFilter.setLevel(saturation);
            saturationTextView.setText(String.format(Locale.ENGLISH, "%.2f", saturation));
        }
        listener.onFilterChange(imageFilter);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        listener.onEditComplete(imageFilter);
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < 3; i++) {
            if (view == buttons[i]) {
                seekBarParents[i].setVisibility(View.VISIBLE);
                buttons[i].setColorFilter(getResources().getColor(R.color.colorAccent));
            } else {
                seekBarParents[i].setVisibility(View.GONE);
                buttons[i].setColorFilter(0xFF000000);
            }
        }
    }

    interface FilterListener {
        void onFilterChange(Filter filter);

        void onEditComplete(Filter filter);
    }

}