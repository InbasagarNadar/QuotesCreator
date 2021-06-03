package com.dartt.yello;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dartt.yello.views.CustomColorPickerView;
import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.ArrayList;

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
    CustomColorPickerView fontBackgroundColorPicker;
    ViewGroup constraintTextBackgroundParent;
    ImageButton textAlignmentImageButton;
    ImageButton textHighlightImageButton;
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
            return new ForegroundColorSpan(fontBackgroundColorPicker.getColor());
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
    RecentColorAdapter recentColorAdapter;

    FontHolder currentEditorFont;
    FontHolder oldEditorFont;
    int highlightOpacity = 0xFF000000;
    boolean foregroundColorWindowMode;
    Drawable[] alignmentDrawables;
    Drawable[] highlightOpacityDrawables;
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
        fontBackgroundColorPicker = fragment.findViewById(R.id.font_background_color_picker);
        constraintTextBackgroundParent = fragment.findViewById(R.id.constraintTextBackgroundPickerParent);
        fontPreviewText = fragment.findViewById(R.id.font_preview_text);
        typefacePanel = fragment.findViewById(R.id.font_typeface_panel);
        textAlignmentImageButton = fragment.findViewById(R.id.button_text_align);
        textHighlightImageButton = fragment.findViewById(R.id.imageButtonTextHighlightOpacity);
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
                editText.setShowSoftInputOnFocus(constraintTextBackgroundParent.getVisibility() != View.VISIBLE);
                return view.onTouchEvent(motionEvent);
            }
        });
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - _clickTime[0] < 1200) {
                    constraintTextBackgroundParent.setVisibility(View.GONE);
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
                // Fix for TextView bounds extending till placeholder/ hint.
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

        highlightOpacityDrawables = new Drawable[]{
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_outline_font_download_24, null),
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_twotone_font_download_24, null),
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_font_download_24, null)
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
                //foregroundColorWindowMode = true;
                toggleFontHighlightPanel(true);
            }
        });

        fragment.findViewById(R.id.button_toggle_text_highlight_color_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //foregroundColorWindowMode = false;
                toggleFontHighlightPanel(false);
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


        ((ToggleButton) fragment.findViewById(R.id.toggleCustomTextHighlightColor)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fragment.findViewById(R.id.constraintTextHighlightListParent).setVisibility(isChecked ? View.VISIBLE : View.GONE);
                fragment.findViewById(R.id.constraintTextHighlightCustomPickerParent).setVisibility(!isChecked ? View.VISIBLE : View.GONE);
            }
        });

        textHighlightImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (highlightOpacity == 0xFF000000) {
                    highlightOpacity = 0x88000000;
                }
                else if (highlightOpacity == 0x88000000) {
                    highlightOpacity = 0;
                }
                else {
                    highlightOpacity = 0xFF000000;
                }
                setHighlightColor(((ColorDrawable) editText.getBackground()).getColor());
            }
        });

        RecyclerView colorRecycler = fragment.findViewById(R.id.recyclerTextHighlightColor);
        colorRecycler.setAdapter(new ColorAdapter(getContext()) {
            @Override
            void onColorClick(int color) {
                setForegroundOrBackgroundColor(color);
            }
        });
        colorRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        recentColorAdapter = new RecentColorAdapter(getContext()) {
            @Override
            void onColorClick(int color) {
                setForegroundOrBackgroundColor(color);
            }
        };

        RecyclerView recentColorRecycler = fragment.findViewById(R.id.recyclerRecentHighlightColors);
        recentColorRecycler.setAdapter(recentColorAdapter);
        recentColorRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        /*fontForegroundColorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                insertStyling(colorSpan);
            }
        });*/
        fontBackgroundColorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                setForegroundOrBackgroundColor(newColor);
            }
        });

        fontBackgroundColorPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    recentColorAdapter.addRecentColor(fontBackgroundColorPicker.getColor());
                }
                return false;
            }
        });

        fragment.findViewById(R.id.button_toggle_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorDrawable colorDrawable = (ColorDrawable) fragment.getBackground();
                if (colorDrawable.getColor() == 0xCCFFFFEE) {
                    colorDrawable.setColor(0xDD111111);
                }
                else {
                    colorDrawable.setColor(0xCCFFFFEE);
                }
            }
        });

        return fragment;
    }

    void setForegroundOrBackgroundColor(int color) {
        fontBackgroundColorPicker.setColor(color | 0xFF000000, false);
        if (foregroundColorWindowMode) {
            insertStyling(colorSpan);
        }
        else {
            if (highlightOpacity == 0) {
                highlightOpacity = 0xFF000000;
            }
            setHighlightColor(color);
        }
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
        constraintTextBackgroundParent.setVisibility(View.GONE);
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
        int highlightColor = ((ColorDrawable) text.getBackground()).getColor();
        highlightOpacity = highlightColor & 0xFF000000;
        editText.setBackgroundColor(highlightColor);
        setHighlightColor(highlightColor);
        //((ColorDrawable)editText.getBackground()).setColor(highlightColor);
        //editText.setBackground(editText.getBackground());
        fontBackgroundColorPicker.setColor(highlightColor);
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
        if (activeTextViewContainer.getView().getParent() != null) {
            activity.removeElement(activeTextViewContainer);
        }
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
            constraintTextBackgroundParent.setVisibility(View.GONE);
            fontOptionGrid.setVisibility(View.VISIBLE);
        }
    }


    public void toggleFontHighlightPanel(boolean foregroundColorWindowMode) {
        if (constraintTextBackgroundParent.getVisibility() == View.VISIBLE && foregroundColorWindowMode == this.foregroundColorWindowMode) {
            constraintTextBackgroundParent.setVisibility(View.GONE);
        }
        else {
            this.foregroundColorWindowMode = foregroundColorWindowMode;
            textHighlightImageButton.setVisibility(foregroundColorWindowMode ? View.GONE : View.VISIBLE);
            constraintTextBackgroundParent.setVisibility(View.VISIBLE);
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

    final ColorStateList darkColorStateList = new ColorStateList(new int[][]{
            new int[]{android.R.attr.state_enabled}}, new int[]{0xFF222222});
    final ColorStateList lightColorStateList = new ColorStateList(new int[][]{
            new int[]{android.R.attr.state_enabled}}, new int[]{0xFFFFFFFF});

    void setHighlightColor(int color) {
        color = (color & 0x00FFFFFF) | highlightOpacity;
        editText.setBackgroundColor(color);
        textHighlightImageButton.setImageTintList(new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_enabled}}, new int[]{color | 0xFF000000}));
        if (GraphicsHelper.luminanceOf(color) > 100) {
            textHighlightImageButton.setBackgroundTintList(darkColorStateList);
        }
        else {
            textHighlightImageButton.setBackgroundTintList(lightColorStateList);
        }
        switch (highlightOpacity) {
            case 0:
                textHighlightImageButton.setImageDrawable(highlightOpacityDrawables[0]);
                break;
            case 0x88000000:
                textHighlightImageButton.setImageDrawable(highlightOpacityDrawables[1]);
                break;
            default:
                textHighlightImageButton.setImageDrawable(highlightOpacityDrawables[2]);
                break;
        }
    }

    public void showTypefacePanel() {
        if (typefacePanel.getVisibility() != View.VISIBLE) {
            oldEditorFont = currentEditorFont;
            inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            editText.clearFocus();
            constraintTextBackgroundParent.setVisibility(View.GONE);
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

    static abstract class ColorAdapter extends RecyclerView.Adapter<ColorViewHolder> {

        Context context;

        public ColorAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageButton view = (ImageButton) LayoutInflater.from(context).inflate(R.layout.item_color_row, parent, false);
            return new ColorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ColorViewHolder holder, final int position) {
            //holder.colorButton.setBackgroundColor(Variables.Colors[position]);
            holder.colorButton.setBackground(Variables.newColorDrawable(context, Variables.Colors[position], Variables.CORNER_DP));
            holder.colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onColorClick(Variables.Colors[position]);
                }
            });
        }

        @Override
        public int getItemCount() {
            return Variables.Colors.length;
        }

        abstract void onColorClick(int color);
    }

    static abstract class RecentColorAdapter extends RecyclerView.Adapter<ColorViewHolder> {
        Context context;
        ArrayList<Integer> recentColors;

        public RecentColorAdapter(Context context) {
            this.context = context;
            recentColors = new ArrayList<>();
            recentColors.add(0xFFFFFFFF);
            recentColors.add(0xFF000000);
        }

        @NonNull
        @Override
        public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageButton view = (ImageButton) LayoutInflater.from(context).inflate(R.layout.item_color_row, parent, false);
            return new ColorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ColorViewHolder holder, final int position) {
            //holder.colorButton.setBackgroundColor(recentColors.get(position));
            holder.colorButton.setBackground(Variables.newColorDrawable(context, recentColors.get(position), Variables.CORNER_DP));
            holder.colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onColorClick(recentColors.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return recentColors.size();
        }

        void addRecentColor(int color) {
            color = color | 0xFF000000;
            if (!recentColors.contains(color)) {
                recentColors.add(0, color);
                if (recentColors.size() > 20) {
                    recentColors.remove(recentColors.size() - 1);
                }
                notifyDataSetChanged();
            }
        }

        abstract void onColorClick(int color);

    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        ImageButton colorButton;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorButton = (ImageButton) itemView;
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