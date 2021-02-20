package com.dartt.yello;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class StickerBottomSheet extends BottomSheetDialogFragment {

    interface Listener {
        void onStickerSelectListener(int resourceId);
    }

    RecyclerView recyclerView;
    Listener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_sticker, container, false);
        recyclerView = v.findViewById(R.id.recycler_view_sticker);
        recyclerView.setAdapter(new StickerAdapter(getContext()));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        return v;
    }

    class StickerViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onStickerSelectListener(StickerViewHolder.this.getAdapterPosition());
                    }
                    dismiss();
                }
            });
        }

    }

    class StickerAdapter extends RecyclerView.Adapter<StickerViewHolder> {

        final int COLUMNS = 3;
        final Context context;


        public StickerAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_row_layout, parent, false);
            return new StickerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
            Glide.with(context).load(Variables.stickerResourceId[position]).fitCenter().into(holder.imageView);
            //Glide.with(context).load(Variables.stickerResourceId[position * 3 + 1]).into(holder.imageViews[1]);
            //Glide.with(context).load(Variables.stickerResourceId[position * 3 + 2]).into(holder.imageViews[2]);
        }

        @Override
        public int getItemCount() {
            return Variables.stickerResourceId.length;
            //return (int) Math.ceil((float) Variables.stickerResourceId.length / (float) COLUMNS);
        }

    }


}
