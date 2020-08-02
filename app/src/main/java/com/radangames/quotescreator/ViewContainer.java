package com.radangames.quotescreator;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;

public class ViewContainer {

    public View view;
    public float scale = .5f;
    public float width;
    public float height;
    public float textSizePx = 100;
    public SpannableStringBuilder text;

    public void remove() {
        ((ViewGroup) view.getParent()).removeView(view);
    }

}

