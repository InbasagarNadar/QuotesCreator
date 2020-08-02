package com.radangames.quotescreator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class RatioFragment extends Fragment {
    public RatioFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ratio, container, false);
    }
/*
    public void onRatioClick(View view){
        ((CreateBackgroundActivity)getActivity()).onRatioClick(view);
    }*/
}