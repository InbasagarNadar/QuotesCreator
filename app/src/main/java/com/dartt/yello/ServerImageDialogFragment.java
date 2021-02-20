package com.dartt.yello;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ServerImageDialogFragment extends DialogFragment {

    ImageButton refreshButton;
    ImageView imageView;
    Listener listener;
    String imageUrl;
    boolean downloaded = false;
    CircularProgressDrawable loadingDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_server_image, container, false);
        loadingDrawable = new CircularProgressDrawable(getContext());
        loadingDrawable.setCenterRadius(Variables.densityToPixel(getContext(), 50));
        //loadingDrawable.setBackgroundColor(0xFF000000);
        refreshButton = v.findViewById(R.id.image_button);
        imageView = v.findViewById(R.id.image_view);
        imageUrl = getArguments().getString("image_url");
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImage();
            }
        });
        loadImage();
        v.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downloaded) {
                    if (listener != null) {
                        listener.onImageSelected(getArguments().getString("image_url"));
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Please let the image downloadZipAsync finish.", Toast.LENGTH_SHORT).show();
                }
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

    void loadImage() {
        loadingDrawable.start();
        refreshButton.setVisibility(View.GONE);
        Glide.with(this).load(imageUrl).placeholder(loadingDrawable).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                refreshButton.setVisibility(View.VISIBLE);
                loadingDrawable.stop();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                downloaded = true;
                return false;
            }
        }).into(imageView);
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
