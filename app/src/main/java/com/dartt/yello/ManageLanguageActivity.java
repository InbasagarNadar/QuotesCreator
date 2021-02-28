package com.dartt.yello;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class ManageLanguageActivity extends AppCompatActivity {

    RecyclerView recyclerLanguage;

    CustomDownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_language);
        recyclerLanguage = findViewById(R.id.recycler_language);
        recyclerLanguage.setAdapter(new LanguageRecyclerAdapter(this));
        recyclerLanguage.setLayoutManager(new LinearLayoutManager(this));
    }

    public void onCancelClick(View view) {
        finish();
    }


    class LanguageRecyclerAdapter extends RecyclerView.Adapter<LanguageRecyclerAdapter.ViewHolder> {
        Context context;

        LanguageRecyclerAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.recycler_language_row, parent, false);
            return new ViewHolder(listItem);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            if (position == 0) {
                holder.txtLanguageName.setText(LanguageManager.languages[position].name);
            }
            else {
                holder.txtLanguageName.setText(String.format("%s (%s)", LanguageManager.languages[position].name, LanguageManager.languages[position].locale));
            }
            if (position == 0) {
                holder.cbxEnabled.setEnabled(false);
                holder.cbxEnabled.setChecked(true);
                holder.btnDownload.setVisibility(View.GONE);
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setEnabled(false);
            }
            else {
                holder.cbxEnabled.setEnabled(true);
                holder.btnDelete.setEnabled(true);
                if (LanguageManager.languages[position].isDownloaded(context)) {
                    holder.btnDownload.setVisibility(View.GONE);
                    holder.cbxEnabled.setVisibility(View.VISIBLE);
                    holder.cbxEnabled.setChecked(LanguageManager.languages[position].isActive(ManageLanguageActivity.this));
                    holder.cbxEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            LanguageManager.languages[position].setActive(ManageLanguageActivity.this, isChecked);
                        }
                    });
                    holder.btnDelete.setVisibility(View.VISIBLE);
                    holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ManageLanguageActivity.this)
                                    .setMessage("Do you want to delete the fonts for the language " + LanguageManager.languages[position].name + "?")
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Log.d("Language", "Deleting " + LanguageManager.languages[position].code);
                                            LanguageManager.languages[position].delete(ManageLanguageActivity.this);
                                            notifyItemChanged(position);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null);
                            builder.show();
                        }
                    });
                }
                else {
                    holder.btnDelete.setVisibility(View.GONE);
                    holder.cbxEnabled.setVisibility(View.GONE);
                    holder.btnDownload.setVisibility(View.VISIBLE);
                    holder.btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentTransaction ft = ManageLanguageActivity.this.getSupportFragmentManager().beginTransaction();
                            Fragment prev = ManageLanguageActivity.this.getSupportFragmentManager().findFragmentByTag("111");
                            if (prev == null) {
                                LanguageDownloadDialogFragment dialogFragment = LanguageDownloadDialogFragment.newInstance(position, new LanguageDownloadDialogFragment.Listener() {
                                    @Override
                                    public void onDismiss(int languageIndex, boolean installSuccess) {
                                        if (installSuccess) {
                                            notifyItemChanged(position);
                                        }
                                    }
                                });
                                dialogFragment.show(ft, "111");
                            }
                            Log.d("Language", "Downloading " + LanguageManager.languages[position].code);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return LanguageManager.languages.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView txtLanguageName;
            public CheckBox cbxEnabled;
            public ImageButton btnDownload;
            public ImageButton btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtLanguageName = itemView.findViewById(R.id.label_language);
                cbxEnabled = itemView.findViewById(R.id.cbx_enabled);
                btnDownload = itemView.findViewById(R.id.btn_download);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}


class FontHolder {
    String languageCode;
    Typeface typeface;
    String name;
    String path;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FontHolder that = (FontHolder) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}

class Lang {
    final String name;
    final String code;
    final String locale;

    Lang(String name, String locale, String code) {
        this.name = name;
        this.locale = locale;
        this.code = code;
    }

    boolean isDownloaded(Context context) {
        return context.getSharedPreferences("lang", MODE_PRIVATE).getBoolean(code + "_downloaded", false);
    }

    void setDownloaded(Context context, boolean downloaded) {
        context.getSharedPreferences("lang", MODE_PRIVATE).edit().putBoolean(code + "_downloaded", downloaded).apply();
    }

    boolean isActive(Context context) {
        return context.getSharedPreferences("lang", MODE_PRIVATE).getBoolean(code + "_active", false);
    }

    void setActive(Context context, boolean active) {
        context.getSharedPreferences("lang", MODE_PRIVATE).edit().putBoolean(code + "_active", active).apply();
    }

    String getFontsPath(Context context) {
        return context.getFilesDir() + "/fonts/" + code + "/";
    }

    Typeface getTypeface(Context context, String typefaceName) {
        if (isActive(context)) {
            return Typeface.createFromFile(context.getFilesDir() + "/fonts/" + code + "/" + typefaceName);
        }
        return null;
    }

    FontHolder[] getFonts(Context context) {
        if (isActive(context)) {
            String fontPath = getFontsPath(context);
            String[] fontNames = new File(fontPath).list();
            FontHolder[] fonts = new FontHolder[fontNames.length];
            for (int i = 0; i < fonts.length; i++) {
                FontHolder fontHolder = new FontHolder();
                fontHolder.name = fontNames[i];
                fontHolder.path = fontPath + fontNames[i];
                fontHolder.typeface = Typeface.createFromFile(fontHolder.path);
                fontHolder.languageCode = code;
                fonts[i] = fontHolder;
            }
            return fonts;
        }
        return null;
    }

    public void delete(Context context) {
        setDownloaded(context, false);
        setActive(context, false);
        String fontsPath = getFontsPath(context);
        String[] entries = new File(fontsPath).list();
        for (String s : entries) {
            File currentFile = new File(fontsPath, s);
            currentFile.delete();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass().equals(String.class)) {
            return code.equals(o);
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        Lang lang = (Lang) o;
        return code.equals(lang.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}

class LanguageManager {

    public static class Codes {
        final static String English = "english";
        final static String Devanagari = "devanagari";
        final static String Tamil = "tamil";
        final static String Malayalam = "malayalam";
        final static String Telugu = "telugu";
    }

    public static final int[] englishFontResourceId = {
            R.font.amatic_regular,
            R.font.badly_stamped,
            R.font.bear_hugs,
            R.font.century_gothic,
            R.font.cinzel_regular,
            R.font.clemente_p_dab_hairline,
            R.font.clemente_p_dai_regular,
            R.font.elvina_amelia,
            R.font.fragment_core,
            R.font.frenchpress,
            R.font.fugaz_one_regular,
            R.font.grandhotel_regular,
            R.font.hagin_caps_thin,
            R.font.hagin_caps_medium,
            R.font.handlee_regular,
            R.font.i_m_fell_english_regular,
            R.font.indie_flower_regular,
            R.font.julee_regular,
            R.font.libre_baskerville,
            R.font.libre_calson_display_regular,
            R.font.londrina_solid_thin,
            R.font.melon_hunter,
            R.font.merianda_bold,
            R.font.montserrat_light,
            R.font.montserrat_bold,
            R.font.nexa_light,
            R.font.nexa_bold,
            R.font.my_old_remington,
            R.font.obelisque,
            R.font.old_newspaper_types,
            R.font.open_sans_regular,
            R.font.paul_signature,
            R.font.permanent_marker,
            R.font.ranchers,
            R.font.roboto_regular,
            R.font.royal,
            R.font.satisfy_regular,
            R.font.silent_hunter_iii,
            R.font.telegraphem,
            R.font.travelling_typewriter,
            R.font.type_keys_filled,
            R.font.white_army,
            R.font.winkle_regular
    };

    public static final String[] englishFontResourceName = {
            "Amatic",
            "Badly Stamped",
            "Bear Hugs",
            "Century Gothic",
            "Cinzel",
            "ClementePDab Hairline",
            "ClementePDai Regular",
            "Elvina Amelia",
            "Fragment Core",
            "French Press",
            "Fugaz One",
            "Grandhotel",
            "Hagin Thin",
            "Hagin Regular",
            "Handlee",
            "IM Fell English",
            "Indie Flower",
            "Julee",
            "Libre Baskerville",
            "Libre Calson",
            "Londrina Solid",
            "Melon Hunter",
            "Merianda",
            "Montserrat Light",
            "Montserrat Bold",
            "Nexa Light",
            "Nexa Bold",
            "My Old Remington",
            "Obelisque",
            "Old Newspaper",
            "Open Sans",
            "Paul Signature",
            "Permanent Marker",
            "Ranchers",
            "Roboto",
            "Royal",
            "Satisfy",
            "Silent Hills III",
            "Telegraphem",
            "Travelling Typewriter",
            "Type Keys",
            "White Army",
            "Winkle Regular"
    };

    static Lang[] languages = new Lang[]{
            new Lang("English", "English", Codes.English) {
                @Override
                FontHolder[] getFonts(Context context) {
                    FontHolder[] fonts = new FontHolder[englishFontResourceName.length];
                    for (int i = 0; i < fonts.length; i++) {
                        FontHolder fontHolder = new FontHolder();
                        fontHolder.name = englishFontResourceName[i];
                        fontHolder.typeface = ResourcesCompat.getFont(context, englishFontResourceId[i]);
                        fontHolder.path = "res/" + fontHolder.name;
                        fontHolder.languageCode = Codes.English;
                        fonts[i] = fontHolder;
                    }
                    return fonts;
                }
            },
            new Lang("Devanagari", "देवनागरी", Codes.Devanagari),
            new Lang("Tamil", "தமிழ்", Codes.Tamil),
            new Lang("Malayalam", "മലയാളം", Codes.Malayalam),
            new Lang("Telugu", "తెలుగు", Codes.Telugu)
    };

    static boolean isLanguageDownloaded(Context context, String code) {
        if (code.equals(Codes.English)) {
            return true;
        }
        return context.getSharedPreferences("lang", MODE_PRIVATE).getBoolean(code + "_downloaded", false);
    }

    static boolean isLanguageActive(Context context, String code) {
        if (code.equals(Codes.English)) {
            return true;
        }
        return context.getSharedPreferences("lang", MODE_PRIVATE).getBoolean(code + "_active", false);
    }

    static String getLanguageCodeFromText(CharSequence charSequence) {
        String languageCode = Codes.English;
        Character.UnicodeBlock characterLanguage = Character.UnicodeBlock.of(charSequence.charAt(0));
        if ((characterLanguage == Character.UnicodeBlock.DEVANAGARI_EXTENDED || characterLanguage == Character.UnicodeBlock.DEVANAGARI)) {
            languageCode = Codes.Devanagari;
        }
        else if (characterLanguage == Character.UnicodeBlock.TAMIL) {
            languageCode = Codes.Tamil;
        }
        else if (characterLanguage == Character.UnicodeBlock.MALAYALAM) {
            languageCode = Codes.Malayalam;
        }
        else if (characterLanguage == Character.UnicodeBlock.TELUGU) {
            languageCode = Codes.Telugu;
        }
        return languageCode;
    }

    static boolean isTypefaceAvailable(Context context, String code, String typefaceName) {
        return new File(context.getFilesDir() + "/fonts/" + code + "/" + typefaceName).exists();
    }

    static String getFontPath(Context context, String languageCode, String fontName) {
        if (languageCode.equals(Codes.English)) {
            return "res/" + fontName;
        }
        return context.getFilesDir() + "/fonts/" + languageCode + "/" + fontName;
    }

    static Typeface getTypeface(Context context, String languageCode, String typefaceName) {
        if (languageCode.equals(Codes.English)) {
            for (int i = 0; i < englishFontResourceName.length; i++) {
                if (englishFontResourceName[i].equals(typefaceName)) {
                    return ResourcesCompat.getFont(context, englishFontResourceId[i]);
                }
            }
            return null;
        }
        else {
            return Typeface.createFromFile(context.getFilesDir() + "/fonts/" + languageCode + "/" + typefaceName);
        }
    }

    static FontHolder getDefaultFont(Context context) {
        FontHolder fontHolder = new FontHolder();
        fontHolder.languageCode = Codes.English;
        fontHolder.typeface = ResourcesCompat.getFont(context, englishFontResourceId[0]);
        fontHolder.name = englishFontResourceName[0];
        fontHolder.path = "res/" + fontHolder.name;
        return fontHolder;
    }

    @Nullable
    static FontHolder newFontHolder(Context context, String languageCode, String fontName) {
        if (isLanguageDownloaded(context, languageCode)) {
            FontHolder fontHolder = new FontHolder();
            fontHolder.languageCode = languageCode;
            fontHolder.name = fontName;
            fontHolder.typeface = getTypeface(context, languageCode, fontName);
            fontHolder.path = getFontPath(context, languageCode, fontName);
            return fontHolder;
        }
        return null;
    }

    static Lang[] getActiveLanguages(Context context) {
        ArrayList<Lang> activeLanguages = new ArrayList<>();
        for (Lang lang : languages) {
            if (lang.isActive(context) || lang.code.equals("english")) {
                activeLanguages.add(lang);
            }
        }
        Lang[] result = new Lang[activeLanguages.size()];
        return activeLanguages.toArray(result);
    }

}