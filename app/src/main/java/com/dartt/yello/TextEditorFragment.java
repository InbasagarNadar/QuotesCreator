package com.dartt.yello;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dartt.yello.views.CustomColorPickerView;
import com.jaredrummler.android.colorpicker.ColorPickerView;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class TextEditorFragment extends Fragment {


    int FONT_SEEK_BAR_MIN = 10;

    CreateBackgroundActivity activity;

    ConstraintLayout parent;

    EditText editText;
    ScrollView editTextScrollView;
    InputMethodManager inputManager;
    TextViewContainer activeTextViewContainer;
    SeekBar fontSizeSeekBar;
    SeekBar letterSpacingSeekBar;
    SeekBar lineSpacingSeekBar;
    GridLayout fontOptionGrid;
    CustomColorPickerView fontColorPicker;
    ViewGroup fontColorPickerParent;
    ImageButton textAlignmentImageButton;

    LinearLayout autoLanguageParent;
    TextView autoLanguageText;

    SpanHelper boldSpan = new SpanHelper() {
        @Override
        boolean match(CharacterStyle span) {
            if (span instanceof StyleSpan) {
                return (((StyleSpan) span).getStyle() & Typeface.BOLD) == Typeface.BOLD;
            }
            return false;
        }

        @Override
        CharacterStyle getNewSpan() {
            return new StyleSpan(Typeface.BOLD);
        }

        @Override
        Class<?> getSpanClass() {
            return StyleSpan.class;
        }

        @Override
        CharacterStyle clone(CharacterStyle reference) {
            return new StyleSpan(((StyleSpan) reference).getStyle());
        }

    };
    SpanHelper italicSpan = new SpanHelper() {
        @Override
        boolean match(CharacterStyle span) {
            if (span instanceof StyleSpan) {
                return (((StyleSpan) span).getStyle() & Typeface.ITALIC) == Typeface.ITALIC;
            }
            return false;
        }

        @Override
        CharacterStyle getNewSpan() {
            return new StyleSpan(Typeface.ITALIC);
        }

        @Override
        Class<?> getSpanClass() {
            return StyleSpan.class;
        }

        @Override
        CharacterStyle clone(CharacterStyle reference) {
            return new StyleSpan(((StyleSpan) reference).getStyle());
        }
    };
    SpanHelper underlineSpan = new SpanHelper() {
        @Override
        boolean match(CharacterStyle span) {
            return (span instanceof UnderlineSpan);
        }

        @Override
        CharacterStyle getNewSpan() {
            return new UnderlineSpan();
        }

        @Override
        Class<?> getSpanClass() {
            return UnderlineSpan.class;
        }

        @Override
        CharacterStyle clone(CharacterStyle reference) {
            return getNewSpan();
        }
    };
    SpanHelper colorSpan = new SpanHelper() {
        @Override
        boolean match(CharacterStyle span) {
            return (span instanceof ForegroundColorSpan);
        }

        @Override
        CharacterStyle getNewSpan() {
            return new ForegroundColorSpan(fontColorPicker.getColor());
        }

        @Override
        Class<?> getSpanClass() {
            return ForegroundColorSpan.class;
        }

        @Override
        CharacterStyle clone(CharacterStyle reference) {
            return new ForegroundColorSpan(((ForegroundColorSpan) reference).getForegroundColor());
        }
    };

    /*SpanHelper outlineSpan = new SpanHelper() {
        @Override
        boolean match(CharacterStyle span) {
            return span instanceof OutlineSpan;
        }

        @Override
        CharacterStyle getNewSpan() {
            return new OutlineSpan(fontColorPicker.getColor(), 20);
        }

        @Override
        Class<?> getSpanClass() {
            return OutlineSpan.class;
        }
    };
*/


    TextView fontPreviewText;
    ConstraintLayout typefacePanel;
    RecyclerView recyclerLanguage;
    RecyclerView recyclerFont;
    LanguageAdapter languageAdapter;
    TypefaceAdapter typefaceAdapter;

    FontHolder currentEditorFont;
    FontHolder oldEditorFont;

    Drawable[] alignmentDrawables;
    long[] _clickTime = {0, 0};


    public TextEditorFragment() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (CreateBackgroundActivity) getActivity();
        inputManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        final View fragment = inflater.inflate(R.layout.fragment_text_editor, container, false);
        parent = fragment.findViewById(R.id.parent);
        editText = fragment.findViewById(R.id.edit_text_input);
        editTextScrollView = fragment.findViewById(R.id.edit_text_scroll_view);

        fontSizeSeekBar = fragment.findViewById(R.id.seekBarFontSize);
        letterSpacingSeekBar = fragment.findViewById(R.id.seekbar_letter_spacing);
        lineSpacingSeekBar = fragment.findViewById(R.id.seekbar_line_spacing);
        fontOptionGrid = fragment.findViewById(R.id.font_options_grid);
        fontColorPicker = fragment.findViewById(R.id.font_color_picker);
        fontColorPickerParent = fragment.findViewById(R.id.font_color_picker_parent);
        fontPreviewText = fragment.findViewById(R.id.font_preview_text);
        typefacePanel = fragment.findViewById(R.id.font_typeface_panel);
        textAlignmentImageButton = fragment.findViewById(R.id.button_text_align);
        autoLanguageParent = fragment.findViewById(R.id.auto_language_parent);
        autoLanguageText = fragment.findViewById(R.id.auto_language_text);

        recyclerLanguage = fragment.findViewById(R.id.recycler_language);
        recyclerLanguage.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), RecyclerView.HORIZONTAL);
        recyclerLanguage.addItemDecoration(dividerItemDecoration);
        languageAdapter = new LanguageAdapter(getContext());
        languageAdapter.setOnLanguageSelectedListener(new LanguageAdapter.OnLanguageSelectedListener() {
            @Override
            public void onLanguageSelected(Lang language) {
                typefaceAdapter.setLanguage(language);
            }
        });
        recyclerLanguage.setAdapter(languageAdapter);

        recyclerFont = fragment.findViewById(R.id.font_recycler_view);
        recyclerFont.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        typefaceAdapter = new TypefaceAdapter(getContext(), recyclerFont, LanguageManager.languages[0]);
        typefaceAdapter.setOnItemSelectedListener(new TypefaceAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(FontHolder fontHolder) {
                setEditorTypeface(fontHolder);
            }
        });
        recyclerFont.setAdapter(typefaceAdapter);


        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                setEditorFontSize(FONT_SEEK_BAR_MIN + value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        letterSpacingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editText.setLetterSpacing((float) i / 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        lineSpacingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editText.setLineSpacing(1, 1 + (float) i / 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        fontColorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                insertStyling(colorSpan);
            }
        });
        parent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                typefacePanel.setVisibility(View.GONE);
                editText.setShowSoftInputOnFocus(fontColorPickerParent.getVisibility() != View.VISIBLE);
                return view.onTouchEvent(motionEvent);
            }
        });
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - _clickTime[0] < 1200) {
                    fontColorPickerParent.setVisibility(View.GONE);
                    show();
                }
                _clickTime[0] = _clickTime[1];
                _clickTime[1] = System.currentTimeMillis();

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() >= 4 || charSequence.length() < 1) {
                    autoLanguageParent.setVisibility(View.GONE);
                }
                else {
                    String textLanguageCode = LanguageManager.getLanguageCodeFromText(charSequence);
                    if (!languageAdapter.activeLanguages[languageAdapter.currentLanguageIndex].code.equals(textLanguageCode)) {
                        autoLanguageText.setText(String.format("Switch to %s%s?", textLanguageCode.substring(0, 1).toUpperCase(), textLanguageCode.substring(1)));
                        autoLanguageParent.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0) {
                    editText.setHint("Type...");
                }
                else {
                    editText.setHint("");
                }

            }
        });

        alignmentDrawables = new Drawable[]{
                ResourcesCompat.getDrawable(getResources(), R.drawable.align_left, null),
                ResourcesCompat.getDrawable(getResources(), R.drawable.align_center, null),
                ResourcesCompat.getDrawable(getResources(), R.drawable.align_right, null)
        };


        fragment.findViewById(R.id.font_typeface_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEditorTypeface(oldEditorFont);
                show();
            }
        });
        fragment.findViewById(R.id.font_typeface_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show();
            }
        });


        fragment.findViewById(R.id.button_commit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commit();
            }
        });

        fragment.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rollback();
            }
        });

        fragment.findViewById(R.id.button_delete_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteText();
            }
        });

        fragment.findViewById(R.id.button_text_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBold();
            }
        });

        fragment.findViewById(R.id.button_text_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleUnderline();
            }
        });

        fragment.findViewById(R.id.button_text_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleItalic();
            }
        });

        fragment.findViewById(R.id.button_text_align).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextAlignment();
            }
        });

        fragment.findViewById(R.id.button_text_toggle_font_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFontOptionGrid();
            }
        });

        fragment.findViewById(R.id.button_toggle_text_color_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFontColorPanel();
            }
        });

        fragment.findViewById(R.id.button_text_show_typeface).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (typefacePanel.getVisibility() == View.GONE) {
                    showTypefacePanel();
                }
                else {
                    setEditorTypeface(oldEditorFont);
                    show();
                }
            }
        });

        fragment.findViewById(R.id.auto_language_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageAdapter.setCurrentLanguage(LanguageManager.getLanguageCodeFromText(editText.getText()));
                setEditorTypeface(typefaceAdapter.fonts[0]);
                autoLanguageParent.setVisibility(View.GONE);
            }
        });

        fragment.findViewById(R.id.auto_language_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoLanguageParent.setVisibility(View.GONE);
            }
        });

        return fragment;
    }


    @Override
    public void onResume() {
        super.onResume();
        languageAdapter.refreshLanguages();
    }


    void setEditorTypeface(FontHolder fontHolder) {
        currentEditorFont = fontHolder;
        editText.setTypeface(fontHolder.typeface);
    }

    public boolean isKeyboardVisible() {
        int heightDiff = parent.getRootView().getHeight() - parent.getHeight();
        return heightDiff > Variables.densityToPixel(activity, 200);
    }

    public void show() {
        hideTypefacePanel();
        parent.setVisibility(View.VISIBLE);
        editText.requestFocus();
        inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

    }

    public void hide() {
        parent.setVisibility(View.GONE);
        inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        editText.clearFocus();
        fontColorPickerParent.setVisibility(View.GONE);
        hideTypefacePanel();
    }

    public void edit(TextViewContainer textViewContainer) {
        activeTextViewContainer = textViewContainer;
        TextView text = textViewContainer.getView();
        editText.setLetterSpacing(text.getLetterSpacing());
        editText.setLineSpacing(0, text.getLineSpacingMultiplier());
        editText.setGravity(text.getGravity());
        editText.setTypeface(text.getTypeface());
        editText.setText(activeTextViewContainer.textContentOriginal);
        setEditorAlignment(text.getGravity());
        currentEditorFont = textViewContainer.fontHolder;
        fontSizeSeekBar.setProgress((int) Variables.pixelToDensity(activity, (int) textViewContainer.textSizePx) - FONT_SEEK_BAR_MIN);
        letterSpacingSeekBar.setProgress((int) text.getLetterSpacing() * 50);
        lineSpacingSeekBar.setProgress((int) ((text.getLineSpacingMultiplier() - 1) * 50));
        show();
    }

    public void commit() {
        if (activeTextViewContainer != null) {
            editText.clearComposingText();
            String text = editText.getText().toString().trim();
            if (activeTextViewContainer.getView().getParent() == null) {
                if (!text.equals("")) {
                    activity.editorFragment.addElement(activeTextViewContainer);
                    activeTextViewContainer.updateContent(editText);
                    //activeTextViewContainer.fontIndex = fontIndex;
                    activeTextViewContainer.fontHolder = currentEditorFont;
                    activity.undo.onElementAdded(activeTextViewContainer);
                }
            }
            else {
                if (text.equals("")) {
                    activity.removeElement(activeTextViewContainer);
                }
                else {
                    activity.undo.beforeEdit(activeTextViewContainer);
                    activeTextViewContainer.updateContent(editText);
                    //activeTextViewContainer.fontIndex = fontIndex;
                    activeTextViewContainer.fontHolder = currentEditorFont;
                    activity.undo.afterEdit(activeTextViewContainer);
                }
            }
        }
        activeTextViewContainer = null;
        hide();
    }

    public void deleteText() {
        activity.removeElement(activeTextViewContainer);
        activeTextViewContainer = null;
        hide();
    }

    public void rollback() {
        activeTextViewContainer = null;
        hide();
    }

    void setEditorFontSize(int fontSize) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
    }

    void nextAlignment() {
        FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams) editTextScrollView.getLayoutParams());
        switch (editText.getGravity()) {
            case Gravity.START | Gravity.TOP:
                setEditorAlignment(Gravity.CENTER);
                break;
            case Gravity.CENTER:
                setEditorAlignment(Gravity.END);
                break;
            case Gravity.END | Gravity.TOP:
                setEditorAlignment(Gravity.START);
                break;
        }
        editTextScrollView.setLayoutParams(params);
    }

    void setEditorAlignment(int alignment) {
        FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams) editTextScrollView.getLayoutParams());
        switch (alignment) {
            case Gravity.START:
            case Gravity.START | Gravity.TOP:
                editText.setGravity(Gravity.START);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                textAlignmentImageButton.setImageDrawable(alignmentDrawables[0]);
                break;
            case Gravity.CENTER:
                editText.setGravity(Gravity.CENTER);
                params.gravity = Gravity.CENTER;
                textAlignmentImageButton.setImageDrawable(alignmentDrawables[1]);
                break;
            case Gravity.END:
            case Gravity.END | Gravity.TOP:
                editText.setGravity(Gravity.END);
                params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                textAlignmentImageButton.setImageDrawable(alignmentDrawables[2]);
                break;
        }
    }

    void toggleBold() {
        toggleStyling(boldSpan);
    }

    void toggleItalic() {
        toggleStyling(italicSpan);
    }

    public void toggleUnderline() {
        editText.clearComposingText();
        toggleStyling(underlineSpan);
    }

    public void toggleFontOptionGrid() {
        if (fontOptionGrid.getVisibility() == View.VISIBLE) {
            fontOptionGrid.setVisibility(View.GONE);
        }
        else {
            fontColorPickerParent.setVisibility(View.GONE);
            fontOptionGrid.setVisibility(View.VISIBLE);
        }
    }

    public void toggleFontColorPanel() {
        if (fontColorPickerParent.getVisibility() == View.VISIBLE) {
            fontColorPickerParent.setVisibility(View.GONE);
        }
        else {
            fontColorPickerParent.setVisibility(View.VISIBLE);
            fontOptionGrid.setVisibility(View.GONE);
            inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            Toast.makeText(activity, "Triple tap on text to show keyboard.", Toast.LENGTH_SHORT).show();
        }
    }

    void toggleStyling(SpanHelper spanHelper) {
        SpannableStringBuilder spannable = (SpannableStringBuilder) editText.getText();

        int selectionStart = 0;
        int selectionEnd = spannable.length();

        if (editText.hasSelection()) {
            selectionStart = editText.getSelectionStart();
            selectionEnd = editText.getSelectionEnd();
        }

        CharacterStyle[] span = (CharacterStyle[]) spannable.getSpans(selectionStart, selectionEnd, spanHelper.getSpanClass());

        boolean add = true;
        for (CharacterStyle styleSpan : span) {
            if (spanHelper.match(styleSpan)) {
                int spanStart = spannable.getSpanStart(styleSpan);
                int spanEnd = spannable.getSpanEnd(styleSpan);
                //Log.e("TextEditor", "spanStart " + spanStart + " selectionStart " + selectionStart + " spanEnd " + spanEnd + " selectionEnd" + selectionEnd);
                if (spanStart >= selectionStart && spanEnd <= selectionEnd) {
                    //Inside Selection
                    spannable.removeSpan(styleSpan);
                }
                else if (spanStart < selectionStart && spanEnd > selectionEnd) {
                    //Span longer than selection
                    spannable.removeSpan(styleSpan);
                    spannable.setSpan(styleSpan, spanStart, selectionStart, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    spannable.setSpan(spanHelper.getNewSpan(), selectionEnd, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                }
                else if (spanEnd > selectionStart && spanEnd <= selectionEnd) {
                    //Left Side
                    spannable.setSpan(styleSpan, spanStart, selectionStart, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                else {
                    //Right Side
                    spannable.setSpan(styleSpan, selectionEnd, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                add = false;
            }
        }
        if (add) {
            spannable.setSpan(spanHelper.getNewSpan(), selectionStart, selectionEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    void insertStyling(SpanHelper spanHelper) {
        SpannableStringBuilder spannable = (SpannableStringBuilder) editText.getText();

        int selectionStart = 0;
        int selectionEnd = spannable.length();

        if (editText.hasSelection()) {
            selectionStart = editText.getSelectionStart();
            selectionEnd = editText.getSelectionEnd();
        }

        CharacterStyle[] span = (CharacterStyle[]) spannable.getSpans(selectionStart, selectionEnd, spanHelper.getSpanClass());
        for (CharacterStyle styleSpan : span) {
            if (spanHelper.match(styleSpan)) {
                int spanStart = spannable.getSpanStart(styleSpan);
                int spanEnd = spannable.getSpanEnd(styleSpan);
                if (spanStart >= selectionStart && spanEnd <= selectionEnd) {
                    //Inside Selection
                    spannable.removeSpan(styleSpan);
                }
                else if (spanStart < selectionStart && spanEnd > selectionEnd) {
                    //Span longer than selection
                    spannable.removeSpan(styleSpan);
                    spannable.setSpan(styleSpan, spanStart, selectionStart, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    spannable.setSpan(spanHelper.clone(styleSpan), selectionEnd, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                }
                else if (spanEnd > selectionStart && spanEnd <= selectionEnd) {
                    //Left Side
                    spannable.setSpan(styleSpan, spanStart, selectionStart, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                else {
                    //Right Side
                    spannable.setSpan(styleSpan, selectionEnd, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
        spannable.setSpan(spanHelper.getNewSpan(), selectionStart, selectionEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

    }

    public void showTypefacePanel() {
        if (typefacePanel.getVisibility() != View.VISIBLE) {
            oldEditorFont = currentEditorFont;
            inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            editText.clearFocus();
            typefacePanel.setVisibility(View.VISIBLE);
            languageAdapter.setCurrentLanguage(currentEditorFont.languageCode);
            typefaceAdapter.setAdapterFont(currentEditorFont);
            typefaceAdapter.notifyDataSetChanged();
        }
    }

    public void hideTypefacePanel() {
        typefacePanel.setVisibility(View.GONE);
    }


    abstract static class SpanHelper {
        abstract boolean match(CharacterStyle span);

        abstract CharacterStyle getNewSpan();

        abstract Class<?> getSpanClass();

        abstract CharacterStyle clone(CharacterStyle reference);
    }


    static class TypefaceAdapter extends RecyclerView.Adapter<TypefaceAdapter.FontViewHolder> {
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


    static class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

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
}

/*
class OutlineSpan extends ReplacementSpan {
    int strokeColor;
    float strokeWidth;

    OutlineSpan(int strokeColor, float strokeWidth) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
    }


    //@Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fontMetrics) {
        if (fontMetrics != null && paint.getFontMetricsInt() != null) {
            Paint.FontMetricsInt paintFontMetricInt = paint.getFontMetricsInt();
            fontMetrics.bottom = paintFontMetricInt.bottom;
            fontMetrics.top = paintFontMetricInt.top;
            fontMetrics.descent = paintFontMetricInt.descent;
            fontMetrics.leading = paintFontMetricInt.leading;
        }
        return (int) paint.measureText(text.toString().substring(start, end));
    }


    //@Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        int originTextColor = paint.getColor();

        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);

        canvas.drawText(text, start, end, x, y, paint);

        paint.setColor(originTextColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        canvas.drawText(text, start, end, x, y, paint);
    }


}*/