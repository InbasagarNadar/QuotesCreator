package com.dartt.yello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;


public class StickerEditorFragment extends Fragment implements View.OnClickListener {

    CreateBackgroundActivity activity;
    StickerViewContainer activeViewContainer;
    ConstraintLayout parent;
    ImageView imageView;
    SeekBar opacitySeekBar;


    public StickerEditorFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (CreateBackgroundActivity) getActivity();
        View fragment = inflater.inflate(R.layout.fragment_sticker_editor, container, false);
        parent = fragment.findViewById(R.id.parent);
        imageView = fragment.findViewById(R.id.sticker_image_view);
        opacitySeekBar = fragment.findViewById(R.id.seekBar_sticker_opacity);
        fragment.findViewById(R.id.btn_sticker_cancel).setOnClickListener(this);
        fragment.findViewById(R.id.btn_sticker_commit).setOnClickListener(this);
        fragment.findViewById(R.id.btn_sticker_delete).setOnClickListener(this);
        fragment.findViewById(R.id.btn_flip_vertical).setOnClickListener(this);
        fragment.findViewById(R.id.btn_flip_horizontal).setOnClickListener(this);
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setOpacity((float) i / (float) seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return fragment;
    }

    public void edit(StickerViewContainer stickerViewContainer) {
        activeViewContainer = stickerViewContainer;
        imageView.setImageDrawable(stickerViewContainer.getView().getDrawable());
        imageView.setScaleX(activeViewContainer.getView().getScaleX());
        imageView.setScaleY(activeViewContainer.getView().getScaleY());
        opacitySeekBar.setProgress((int) (activeViewContainer.getAlpha() * opacitySeekBar.getMax()));
        show();
    }

    public void show() {
        parent.setVisibility(View.VISIBLE);
    }

    public void hide() {
        parent.setVisibility(View.GONE);
    }

    private void setOpacity(float opacity) {
        imageView.setAlpha(opacity);
    }

    void commit() {
        activity.undo.beforeEdit(activeViewContainer);
        activeViewContainer.setAlpha(imageView.getAlpha());
        activeViewContainer.getView().setScaleX(imageView.getScaleX());
        activeViewContainer.getView().setScaleY(imageView.getScaleY());
        activity.undo.afterEdit(activeViewContainer);
    }

    void delete() {
        activity.removeElement(activeViewContainer);
    }

    //I know that I'm repeating the same line thrice, but ok.
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_sticker_cancel) {
            activeViewContainer = null;
            hide();
        } else if (id == R.id.btn_sticker_commit) {
            commit();
            hide();
        } else if (id == R.id.btn_sticker_delete) {
            delete();
            hide();
        } else if (id == R.id.btn_flip_horizontal) {
            imageView.setScaleX(imageView.getScaleX() * -1);
        } else if (id == R.id.btn_flip_vertical) {
            imageView.setScaleY(imageView.getScaleY() * -1);
        }
    }
}