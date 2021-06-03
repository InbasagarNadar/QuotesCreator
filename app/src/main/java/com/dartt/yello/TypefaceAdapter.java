package com.dartt.yello;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class TypefaceAdapter extends RecyclerView.Adapter<TypefaceAdapter.FontViewHolder> {
    int currentFontIndex = -1;
    Lang language;
    FontHolder[] fonts;
    FontHolder selectedFont;
    Context context;
    RecyclerView recyclerView;
    OnItemSelectedListener onItemSelectedListener;

    public TypefaceAdapter(Context context, RecyclerView recyclerView, Lang language) {
        this.context = context;
        this.recyclerView = recyclerView;
        setLanguage(language);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    void setAdapterFont(FontHolder fontHolder) {

        int oldFontIndex = currentFontIndex;
        for (int i = 0; i < fonts.length; i++) {
            if (fontHolder.equals(fonts[i])) {
                currentFontIndex = i;
                if (oldFontIndex >= 0) {
                    notifyItemChanged(oldFontIndex);
                }
                notifyItemChanged(currentFontIndex);
                recyclerView.scrollToPosition(i);
                selectedFont = fontHolder;
                return;
            }
        }
        currentFontIndex = -1;

    }

    @NonNull
    @Override
    public FontViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.font_list_row, parent, false);
        return new FontViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FontViewHolder holder, final int position) {
        final TextView view = holder.lblFontName;
        view.setTypeface(fonts[position].typeface);
        view.setText(fonts[position].name);
        if (onItemSelectedListener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentFontIndex >= 0) {
                        notifyItemChanged(currentFontIndex);
                    }
                    currentFontIndex = position;
                    view.setTextColor(0xFFFFBF00);
                    selectedFont = fonts[position];
                    onItemSelectedListener.onItemSelected(selectedFont);
                }
            });
        }
        if (currentFontIndex == position) {
            view.setTextColor(0xFFFFBF00);
        }
        else {
            view.setTextColor(0xFF000000);
        }
    }

    @Override
    public int getItemCount() {
        return fonts.length;
    }

    public void setLanguage(Lang language) {
        if (this.language != null) {
            if (this.language.equals(language)) {
                return;
            }
        }
        currentFontIndex = -1;
        this.language = language;
        fonts = language.getFonts(context);
        notifyDataSetChanged();
        if (selectedFont != null) {
            setAdapterFont(selectedFont);
        }
    }

    interface OnItemSelectedListener {
        void onItemSelected(FontHolder fontHolder);
    }

    static class FontViewHolder extends RecyclerView.ViewHolder {
        TextView lblFontName;

        public FontViewHolder(@NonNull View itemView) {
            super(itemView);
            lblFontName = (TextView) itemView;
        }
    }
}
