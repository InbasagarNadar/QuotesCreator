package com.radangames.quotescreator.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.radangames.quotescreator.R;

import java.lang.reflect.Field;

public class CustomColorPickerView extends ColorPickerView {

    public CustomColorPickerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        try {
            TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.CustomColorPickerView);
            Field huePanelWidthPx = ColorPickerView.class.getDeclaredField("huePanelWidthPx");
            huePanelWidthPx.setAccessible(true);
            huePanelWidthPx.setInt(this, a.getDimensionPixelSize(R.styleable.CustomColorPickerView_hueSliderWidth, 10));
            Field panelSpacingPx = ColorPickerView.class.getDeclaredField("panelSpacingPx");
            panelSpacingPx.setAccessible(true);
            panelSpacingPx.setInt(this, a.getDimensionPixelSize(R.styleable.CustomColorPickerView_hueSliderSpacing, 10));
            a.recycle();
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e("Reflection", "I guess this was caused coz of obfuscation");
        }
    }

    static int dpToPx(Context c, float dipValue) {
        DisplayMetrics metrics = c.getResources().getDisplayMetrics();
        float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
        int res = (int) (val + 0.5); // Round
        // Ensure at least 1 pixel if val was > 0
        return res == 0 && val > 0 ? 1 : res;
    }
}