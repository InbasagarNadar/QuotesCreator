package com.dartt.yello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class ServerImageDialogFragment extends DialogFragment {

    ImageView imageView;
    Listener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_server_image, container, false);
        imageView = v.findViewById(R.id.image_view);
        Glide.with(this).load(getArguments().getString("image_url")).into(imageView);
        v.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onImageSelected(getArguments().getString("image_url"));
                }
                dismiss();
            }
        });
        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onNothingSelected();
                }
                dismiss();
            }
        });
        return v;
    }

    interface Listener {
        void onImageSelected(String image_url);

        void onNothingSelected();
    }

    public static ServerImageDialogFragment newInstance(String image_url) {
        ServerImageDialogFragment dialogFragment = new ServerImageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("image_url", image_url);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }
}
