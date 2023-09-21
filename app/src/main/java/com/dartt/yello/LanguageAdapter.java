package com.dartt.yello;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {
    int currentLanguageIndex;
    Context context;
    Lang[] activeLanguages;
    OnLanguageSelectedListener languageSelectedListener;

    public LanguageAdapter(Context context) {
        this.context = context;
        activeLanguages = LanguageManager.getActiveLanguages(context);
    }

    void setOnLanguageSelectedListener(OnLanguageSelectedListener listener) {
        languageSelectedListener = listener;
    }

    void setCurrentLanguage(String languageCode) {
        for (int i = 0; i < activeLanguages.length; i++) {
            Lang lang = activeLanguages[i];
            if (languageCode.equals(lang.code)) {
                int oldPosition = currentLanguageIndex;
                currentLanguageIndex = i;
                notifyItemChanged(oldPosition);
                notifyItemChanged(i);
                languageSelectedListener.onLanguageSelected(lang);
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
            .setMessage("You haven't downloaded the language yet. Do you want to download it now?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    context.startActivity(new Intent(context, ManageLanguageActivity.class));
                }
            })
            .setNegativeButton("No", null);
        builder.show();
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_list_item, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, final int position) {
        if (position < activeLanguages.length) {
            holder.lblLanguageName.setText(activeLanguages[position].locale);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (languageSelectedListener != null) {
                        int oldPosition = currentLanguageIndex;
                        currentLanguageIndex = position;
                        notifyItemChanged(oldPosition);
                        notifyItemChanged(position);
                        languageSelectedListener.onLanguageSelected(activeLanguages[position]);
                    }
                }
            });
        }
        else {
            holder.lblLanguageName.setText("  +  ");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, ManageLanguageActivity.class));
                }
            });
        }
        if (currentLanguageIndex == position) {
            holder.lblLanguageName.setTextColor(0xFFFFBF00);
        }
        else {
            holder.lblLanguageName.setTextColor(0xFF000000);
        }

    }

    @Override
    public int getItemCount() {
        return activeLanguages.length + 1;
    }

    public void refreshLanguages() {
        activeLanguages = LanguageManager.getActiveLanguages(context);
        notifyDataSetChanged();
    }

    interface OnLanguageSelectedListener {
        void onLanguageSelected(Lang language);
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView lblLanguageName;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            lblLanguageName = (TextView) itemView;
        }
    }
}
