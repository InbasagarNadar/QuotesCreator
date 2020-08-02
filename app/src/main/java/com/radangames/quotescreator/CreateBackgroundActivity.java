package com.radangames.quotescreator;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.ParcelableSpan;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.radangames.quotescreator.views.CustomColorPickerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


@SuppressLint("ClickableViewAccessibility")
public class CreateBackgroundActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    Foreground Foreground;
    Background Background;
    TextEditor TextEditor;

    FrameLayout canvasFrameLayout;

    TabLayout tabLayout;
    FunctionParameter canvasLayoutChangeAction;
    boolean init = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_background);

        Foreground = new Foreground();
        Background = new Background();
        TextEditor = new TextEditor();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.drawer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        canvasFrameLayout = findViewById(R.id.backgroundCanvas);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        ViewPager viewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TR_BL, Variables.Gradients[1]);
        canvasFrameLayout.setBackground(gd);
    }

    public void onHamClick(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to discard?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onRatioClick(View view) {
        ConstraintLayout.LayoutParams params = ((ConstraintLayout.LayoutParams) canvasFrameLayout.getLayoutParams());
        final int oldLeft = canvasFrameLayout.getLeft();
        params.dimensionRatio = ((Button) view).getText().toString();
        canvasFrameLayout.setLayoutParams(params);
        canvasLayoutChangeAction = new FunctionParameter() {
            @Override
            public void execute() {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) Background.backgroundImage.getLayoutParams();
                int width = params.width;
                params.leftMargin += oldLeft - canvasFrameLayout.getLeft();
                Background.backgroundImage.setLayoutParams(params);
                Background.backgroundImage.setLeft(Background.backgroundImage.getLeft() + oldLeft - canvasFrameLayout.getLeft());
                Background.backgroundImage.setRight(Background.backgroundImage.getLeft() + width);
                Background.canvasTouchListener.positionSnapper.resetMargins(canvasFrameLayout, 75);
                canvasLayoutChangeAction = null;
            }
        };
    }

    public static Bitmap bitmap;

    public void onSaveClick(View view) {
        onShowForegroundEditorClick(null);
        Foreground.canvas.post(new Runnable() {
            @Override
            public void run() {
                Canvas bitmapCanvas = new Canvas();
                int scale = 1;
                bitmap = Bitmap.createBitmap(Foreground.canvas.getWidth() * scale, Foreground.canvas.getHeight() * scale, Bitmap.Config.ARGB_8888);

                bitmapCanvas.setBitmap(bitmap);
                bitmapCanvas.scale(scale, scale);
                Foreground.canvas.draw(bitmapCanvas);

                startActivity(new Intent(CreateBackgroundActivity.this, SaveActivity.class));
            }
        });

    /*
        File filesDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        assert filesDir != null;
        if (!filesDir.exists()) {
            if (filesDir.mkdirs()) {
            }
        }
        File file = new File(filesDir, "quote.jpg");
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Cant able to create file");
                }
            }
            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.close();
            Log.e("TAG", "File Path= " + file.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    int[] getImageRatio(Uri imageUri) {
        int rotation = getImageRotationFromExif(imageUri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(imageUri),
                    null,
                    options);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return new int[]{-1, 1};
        }
        if (rotation == 0 || rotation == 180) {
            return new int[]{options.outWidth, options.outHeight};
        }
        return new int[]{options.outHeight, options.outWidth};
    }

    int getImageRotationFromExif(Uri imageUri) {
        int rotation = 0;
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return rotation;
            }
            ExifInterface exifInterface = new ExifInterface(inputStream);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
            }
        }
        catch (IOException e) {
            Log.e("ExifException", "Something went wrong");
        }
        return rotation;
    }

    public void onImageSelectedFromAppClick(View view) {
        Uri uri = Uri.parse(getID(R.drawable.background_1));
        Background.setImageCentered(uri);
    }

    String getID(int resId) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getResources().getResourcePackageName(resId) + '/' + getResources().getResourceTypeName(resId) + '/' + getResources().getResourceEntryName(resId);
    }

    int SELECT_BACKGROUND_IMAGE_REQUEST_CODE = 0;

    public void onSelectImageFromGalleryClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_BACKGROUND_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_BACKGROUND_IMAGE_REQUEST_CODE) {
                Background.setImageCentered(resultIntent.getData());
            }
        }
    }

    class CanvasTouchListener extends CustomOnTouchListener {

        Vector2D startDimension = new Vector2D();
        View targetView = Background.backgroundImage;
        Snapper rotationSnapper = new Snapper();
        View xAlignmentGuideView = findViewById(R.id.snapperX);
        View yAlignmentGuideView = findViewById(R.id.snapperY);
        public PositionSnapper positionSnapper = new PositionSnapper(canvasFrameLayout, 75, xAlignmentGuideView, yAlignmentGuideView);

        @Override
        void onClick(TouchData touchData) {
        }

        @Override
        void onFingerDown(int fingerCount, TouchData touchData, @Nullable PinchData pinchData) {
            if (fingerCount == 2) {
                startDimension.x = targetView.getWidth();
                startDimension.y = targetView.getHeight();
                xAlignmentGuideView.setVisibility(View.INVISIBLE);
                yAlignmentGuideView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        void onOneFingerDrag(TouchData touchData) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) targetView.getLayoutParams();
            Vector2D deltaPosition = touchData.getDeltaPosition();
            Vector2D snappedPosition = positionSnapper.getSnappedPosition(params, deltaPosition);
            params.leftMargin = (int) snappedPosition.x;
            params.topMargin = (int) snappedPosition.y;
            targetView.setLayoutParams(params);
        }

        @Override
        void onTwoFingerDrag(PinchData pinchData) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) targetView.getLayoutParams();
            Vector2D deltaMidpoint = pinchData.getDeltaMidpoint();
            float currentScale = pinchData.getCurrentScale();
            float lastScale = pinchData.getLastScale();
            params.leftMargin += (int) (deltaMidpoint.x - ((startDimension.x * (currentScale - lastScale)) / 2));
            params.topMargin += (int) (deltaMidpoint.y - ((startDimension.y * (currentScale - lastScale)) / 2));
            params.width = (int) (startDimension.x * currentScale);
            params.height = (int) (startDimension.y * currentScale);
            targetView.setRotation(rotationSnapper.getSnappedValue(targetView.getRotation(), pinchData.getDeltaRotation()));
            targetView.setLayoutParams(params);
        }

        @Override
        void onFingerUp(int fingerCount, TouchData touchData, boolean click) {
            xAlignmentGuideView.setVisibility(View.INVISIBLE);
            yAlignmentGuideView.setVisibility(View.INVISIBLE);
        }
    }

    interface FunctionParameter {
        void execute();
    }


    public void onPickBackgroundColorClick(View view) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_color, (ViewGroup) findViewById(R.id.bottom_sheet_container));
        final ColorPickerView colorPickerView = bottomSheetView.findViewById(R.id.color_picker);
        colorPickerView.setOnColorChangedListener(
                new ColorPickerView.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int newColor) {
                        Background.setFillColor(newColor);
                    }
                }
        );

       /* dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });*/
        colorPickerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }


    class Background {
        public View parent;
        public FrameLayout canvas;
        public ImageView backgroundImage;
        public CanvasTouchListener canvasTouchListener;


        Background() {
            parent = findViewById(R.id.backgroundEditorParent);
            canvas = findViewById(R.id.backgroundCanvas);
            canvas.setDrawingCacheEnabled(true);

            backgroundImage = findViewById(R.id.bg_backgroundImageView);
            final ViewTreeObserver viewTreeObserver = canvas.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //canvasFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (canvasLayoutChangeAction != null) {
                        canvasLayoutChangeAction.execute();
                    }
                    if (init) {
                        return;
                    }

                    init = true;
                    canvasTouchListener = new CanvasTouchListener();
                    canvas.setOnTouchListener(canvasTouchListener);
                    canvasTouchListener.positionSnapper.resetMargins(canvasFrameLayout, 75);
                    View fixedParent = findViewById(R.id.fixedParent);
                    fixedParent.getLayoutParams().width = fixedParent.getWidth();
                    fixedParent.getLayoutParams().height = fixedParent.getHeight();

                }
            });

        }

        void show() {
            parent.setVisibility(View.VISIBLE);
        }

        void hide() {
            parent.setVisibility(View.INVISIBLE);
        }

        void setFillColor(int color) {
            canvas.setBackgroundColor(color);
            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setColor(color);
            colorDrawable.setCornerRadius(Variables.densityToPixel(5));
            findViewById(R.id.button_show_background_colors).setBackground(colorDrawable);
        }

        void setFillGradient(int[] gradient) {
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, gradient);
            Background.canvas.setBackground(gradientDrawable);
        }

        void setImageCentered(Uri imageUri) {
            Foreground.backgroundImage.setImageURI(imageUri);
            backgroundImage.setImageURI(imageUri);
            int[] imageDimension = getImageRatio(imageUri);
            int[] canvasDimension = new int[]{canvasFrameLayout.getMeasuredWidth(), canvasFrameLayout.getMeasuredHeight()};
            float imageRatio = (float) imageDimension[0] / (float) imageDimension[1];
            float canvasRatio = (float) canvasDimension[0] / (float) canvasDimension[1];

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) backgroundImage.getLayoutParams();
            params.leftMargin = params.topMargin = 0;
            //greater the image ratio, wider the image
            if (imageRatio == canvasRatio) {
                params.width = canvasDimension[0];
                params.height = canvasDimension[1];
            }
            else if (imageRatio > canvasRatio) {
                params.width = canvasDimension[0];
                params.height = (int) ((canvasDimension[0] / imageRatio));
                params.topMargin = (canvasDimension[1] - params.height) / 2;
            }
            else {
                params.width = (int) (canvasDimension[1] * imageRatio);
                params.height = canvasDimension[1];
                params.leftMargin = (canvasDimension[0] - params.width) / 2;
            }
            backgroundImage.setRotation(0);
            backgroundImage.setLayoutParams(params);
        }
    }

    public void onShowColorListClick(View view) {
        findViewById(R.id.colorScrollView).setVisibility(View.VISIBLE);
        findViewById(R.id.gradientScrollView).setVisibility(View.INVISIBLE);
    }

    public void onShowGradientListClick(View view) {
        findViewById(R.id.colorScrollView).setVisibility(View.INVISIBLE);
        findViewById(R.id.gradientScrollView).setVisibility(View.VISIBLE);
    }


    /*
     * TEXT EDITOR RELATED CODE
     */
    public void onAddTextClick(View view) {
        TextEditor.edit(new TextViewContainer(this, Foreground.canvas));
    }

    public void onCommitTextClick(View view) {
        TextEditor.commit();
    }

    public void onBoldTextClick(View view) {
        TextEditor.toggleBold();
    }

    public void onItalicTextClick(View view) {
        TextEditor.toggleItalic();
    }

    public void onUnderlineTextClick(View view) {
        TextEditor.toggleUnderline();
    }

    public void onTextAlignmentClick(View view) {
        TextEditor.nextAlignment();
    }

    public void onToggleFontPanelClick(View view) {
        TextEditor.toggleFontOptionGrid();
    }

    public void onToggleFontColorPicker(View view) {
        TextEditor.toggleFontOptionColorPanel();
    }

    private class TextEditor {
        final int fontSizeSeekBarMin = 10;
        View parent = findViewById(R.id.textEditor);
        EditText editText = findViewById(R.id.editTextInput);
        ScrollView editTextScrollView = findViewById(R.id.edit_text_scroll_view);
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        TextViewContainer activeTextViewContainer;
        SeekBar fontSizeSeekBar = findViewById(R.id.seekBarFontSize);
        SeekBar letterSpacingSeekBar = findViewById(R.id.seekbar_letter_spacing);
        SeekBar lineSpacingSeekBar = findViewById(R.id.seekbar_line_spacing);
        Spinner fontSpinner = findViewById(R.id.fontSpinner);
        GridLayout fontOptionGrid = findViewById(R.id.font_options_grid);
        CustomColorPickerView fontColorPicker = findViewById(R.id.font_color_picker);
        ViewGroup fontColorPickerParent = findViewById(R.id.font_color_picker_parent);
        SpanHelper boldSpan = new SpanHelper() {
            @Override
            boolean match(ParcelableSpan span) {
                if (span instanceof StyleSpan) {
                    return (((StyleSpan) span).getStyle() & Typeface.BOLD) == Typeface.BOLD;
                }
                return false;
            }

            @Override
            ParcelableSpan getNewSpan() {
                return new StyleSpan(Typeface.BOLD);
            }

            @Override
            Class<?> getSpanClass() {
                return StyleSpan.class;
            }

        };
        SpanHelper italicSpan = new SpanHelper() {
            @Override
            boolean match(ParcelableSpan span) {
                if (span instanceof StyleSpan) {
                    return (((StyleSpan) span).getStyle() & Typeface.ITALIC) == Typeface.ITALIC;
                }
                return false;
            }

            @Override
            ParcelableSpan getNewSpan() {
                return new StyleSpan(Typeface.ITALIC);
            }

            @Override
            Class<?> getSpanClass() {
                return StyleSpan.class;
            }
        };
        SpanHelper underlineSpan = new SpanHelper() {
            @Override
            boolean match(ParcelableSpan span) {
                return (span instanceof UnderlineSpan);
            }

            @Override
            ParcelableSpan getNewSpan() {
                return new UnderlineSpan();
            }

            @Override
            Class<?> getSpanClass() {
                return UnderlineSpan.class;
            }
        };
        SpanHelper colorSpan = new SpanHelper() {
            @Override
            boolean match(ParcelableSpan span) {
                return (span instanceof ForegroundColorSpan);
            }

            @Override
            ParcelableSpan getNewSpan() {
                return new ForegroundColorSpan(fontColorPicker.getColor());
            }

            @Override
            Class<?> getSpanClass() {
                return ForegroundColorSpan.class;
            }
        };

        /* ColorPickerSheet fontColorPicker = new ColorPickerSheet() {
             @Override
             void onChangeColor(int newColor) {
                 editText.setTextColor(newColor);
             }
         };
 */
        ImageButton textAlignmentImageButton = findViewById(R.id.button_text_align);
        final Drawable[] alignmentDrawables = new Drawable[]{
                ResourcesCompat.getDrawable(getResources(), R.drawable.align_left, null),
                ResourcesCompat.getDrawable(getResources(), R.drawable.align_center, null),
                ResourcesCompat.getDrawable(getResources(), R.drawable.align_right, null)
        };

        TextEditor() {
            fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                    setEditorFontSize(fontSizeSeekBarMin + value);
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

            fontSpinner.setAdapter(new FontSpinnerAdapter(CreateBackgroundActivity.this));
            fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    editText.setTypeface(ResourcesCompat.getFont(CreateBackgroundActivity.this, Variables.fontResourceId[i]));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            fontColorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
                @Override
                public void onColorChanged(int newColor) {
                    addStyling(colorSpan, true);
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
                    editText.setShowSoftInputOnFocus(fontColorPickerParent.getVisibility() != View.VISIBLE);
                    return view.onTouchEvent(motionEvent);
                }
            });
        }

        public void show() {
            parent.setVisibility(View.VISIBLE);
            editText.requestFocus();
            inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }

        public void hide() {
            parent.setVisibility(View.GONE);
            inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            editText.clearFocus();
            fontColorPickerParent.setVisibility(View.GONE);
        }

        public void edit(TextViewContainer textViewContainer) {
            activeTextViewContainer = textViewContainer;
            TextView text = textViewContainer.getView();
            copyTextStyle(text, editText);
            editText.setText(activeTextViewContainer.textContentOriginal);
            setEditorAlignment(text.getGravity());
            fontSpinner.setSelection(textViewContainer.fontIndex);
            fontSizeSeekBar.setProgress((int) Variables.pixelToDensity((int) textViewContainer.textSizePx) - fontSizeSeekBarMin);
            letterSpacingSeekBar.setProgress((int) text.getLetterSpacing() * 50);
            lineSpacingSeekBar.setProgress((int) ((text.getLineSpacingMultiplier() - 1) * 50));
            show();
        }

        public void commit() {
            if (activeTextViewContainer != null) {
                String text = editText.getText().toString().trim();
                if (text.equals("")) {
                    activeTextViewContainer.remove();
                }
                else {
                    activeTextViewContainer.updateContent(editText);
                    activeTextViewContainer.fontIndex = fontSpinner.getSelectedItemPosition();
                }
            }
            hide();
        }

        void setEditorFontSize(int fontSize) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        }

        void copyTextStyle(TextView source, TextView destination) {
            destination.setLetterSpacing(source.getLetterSpacing());
            destination.setLineSpacing(0, source.getLineSpacingMultiplier());
            destination.setGravity(source.getGravity());
            destination.setTypeface(source.getTypeface());
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
            addStyling(boldSpan, false);
        }

        void toggleItalic() {
            addStyling(italicSpan, false);
        }

        public void toggleUnderline() {
            addStyling(underlineSpan, false);
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

        public void toggleFontOptionColorPanel() {
            if (fontColorPickerParent.getVisibility() == View.VISIBLE) {
                fontColorPickerParent.setVisibility(View.GONE);
            }
            else {
                fontColorPickerParent.setVisibility(View.VISIBLE);
                fontOptionGrid.setVisibility(View.GONE);
                inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }

        void addStyling(SpanHelper spanHelper, boolean overwrite) {
            SpannableStringBuilder spannable = (SpannableStringBuilder) editText.getText();

            int selectionStart = 0;
            int selectionEnd = spannable.length();

            if (editText.hasSelection()) {
                selectionStart = editText.getSelectionStart();
                selectionEnd = editText.getSelectionEnd();
            }
            ParcelableSpan[] span = (ParcelableSpan[]) spannable.getSpans(selectionStart, selectionEnd, spanHelper.getSpanClass());

            boolean add = true;
            for (ParcelableSpan styleSpan : span) {
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
                        if (!overwrite) {
                            spannable.removeSpan(styleSpan);
                            spannable.setSpan(styleSpan, spanStart, selectionStart, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            spannable.setSpan(spanHelper.getNewSpan(), selectionEnd, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
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
            if (add || overwrite) {
                spannable.setSpan(spanHelper.getNewSpan(), selectionStart, selectionEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

        abstract class SpanHelper {
            abstract boolean match(ParcelableSpan span);

            abstract ParcelableSpan getNewSpan();

            abstract Class<?> getSpanClass();
        }
    }

    class TextViewContainer extends ViewContainer {
        SpannableStringBuilder textContentOriginal;
        final int PADDING = 25;
        final float MIN_SCALE = .25f;
        float textSizePx = Variables.densityToPixel(35);
        int fontIndex = 0;

        TextViewContainer(final CreateBackgroundActivity activity, ViewGroup parent) {
            TextView textView = new AppCompatTextView(activity);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32.5f);
            textView.setTextColor(0xFFFFFFFF);
            textView.setTypeface(ResourcesCompat.getFont(CreateBackgroundActivity.this, Variables.fontResourceId[fontIndex]));
            textView.setOnTouchListener(new ViewOnTouchListener() {
                @Override
                ViewContainer getViewContainer() {
                    return TextViewContainer.this;
                }
            });
            view = textView;
            parent.addView(view);
            alignToCenter();
        }

        public TextView getView() {
            return (TextView) view;
        }

        @Override
        public void setScale(float newScale) {
            if (newScale < MIN_SCALE) {
                return;
            }
            getView().setTextSize(TypedValue.COMPLEX_UNIT_PX, newScale * textSizePx);
            super.setScale(newScale);
        }

        public void setTextSizePx(int newTextSizePx) {
            textSizePx = newTextSizePx;
            getView().setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSizePx * scale);
        }

        public void setText(EditText source) {
            textContentOriginal = new SpannableStringBuilder((SpannableStringBuilder) source.getText());
            SpannableStringBuilder stringBuilder = (SpannableStringBuilder) source.getText();
            for (int lineCount = source.getLineCount() - 1; lineCount > 0; --lineCount) {
                int end = source.getLayout().getLineStart(lineCount);
                if (stringBuilder.charAt(end - 1) != '\n') {
                    stringBuilder.insert(end, "\n");
                }
            }
            getView().setText(stringBuilder);
        }


        public void updateContent(EditText source) {
            TextView textView = getView();
            TextEditor.copyTextStyle(source, textView);
            setText(source);
            setTextSizePx((int) source.getTextSize());
            setDimension(new Vector2D(source.getWidth() + PADDING, source.getHeight() + PADDING));

        }
    }

    static public class ViewContainer {
        public View view;
        public float scale = 1;
        public float width;
        public float height;
        public SpannableStringBuilder text;

        public void remove() {
            ((ViewGroup) view.getParent()).removeView(view);
        }

        public void move(Vector2D deltaPosition) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.leftMargin += (int) deltaPosition.x;
            params.topMargin += (int) deltaPosition.y;
            view.setLayoutParams(params);
        }

        public void alignToCenter() {
            View parent = (View) view.getParent();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.leftMargin = (parent.getWidth() - view.getWidth()) / 2;
            params.topMargin = (parent.getHeight() - view.getHeight()) / 2;
            view.setLayoutParams(params);
        }

        public void setPosition(Vector2D position) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.leftMargin = (int) position.x;
            params.topMargin = (int) position.y;
            view.setLayoutParams(params);
        }

        public void setDimension(Vector2D dimension) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            int newWidth = (int) (scale * dimension.x);
            int newHeight = (int) (scale * dimension.y);
            params.leftMargin -= (newWidth - params.width) / 2;
            params.topMargin -= (newHeight - params.height) / 2;
            params.width = newWidth;
            params.height = newHeight;
            width = dimension.x;
            height = dimension.y;
            view.setLayoutParams(params);
        }

        public void setScale(float newScale) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            int oldWidth = params.width;
            int oldHeight = params.height;
            int newWidth = (int) (width * newScale);
            int newHeight = (int) (height * newScale);
            params.width = newWidth;
            params.height = newHeight;
            params.leftMargin -= (newWidth - oldWidth) / 2;
            params.topMargin -= (newHeight - oldHeight) / 2;
            scale = newScale;
            view.setLayoutParams(params);
        }
    }

    /*
     * FOREGROUND EDITOR RELATED CODES
     */
    private class Foreground {
        public View parent;
        public FrameLayout canvas;
        public ImageView backgroundImage;
        public ForegroundCanvasTouchListener touchListener;

        Foreground() {
            parent = findViewById(R.id.foregroundEditorParent);
            canvas = findViewById(R.id.foregroundCanvas);
            backgroundImage = findViewById(R.id.fg_backgroundImageView);
            touchListener = new ForegroundCanvasTouchListener(canvas);
            canvas.setOnTouchListener(touchListener);
        }

        void show() {
            parent.setVisibility(View.VISIBLE);
            parent.post(new Runnable() {
                @Override
                public void run() {
                    touchListener.positionSnapper.resetMargins(canvas, 75);
                }
            });
        }

        void hide() {
            parent.setVisibility(View.INVISIBLE);
        }


        private class ForegroundCanvasTouchListener extends CustomOnTouchListener {
            public float viewInitialScale;
            final Vector2D invisible = new Vector2D(-100, -100);
            ViewContainer activeViewContainer;
            View xAlignmentGuide, yAlignmentGuide;
            PositionSnapperUpdated positionSnapper;
            //SnapperUpdated rotationSnapper;

            ForegroundCanvasTouchListener(ViewGroup canvas) {
                xAlignmentGuide = findViewById(R.id.foregroundAlignmentGuideX);
                yAlignmentGuide = findViewById(R.id.foregroundAlignmentGuideY);
                positionSnapper = new PositionSnapperUpdated(canvas, 75);
            }

            @Override
            void onClick(TouchData touchData) {
                if (activeViewContainer != null) {
                    if (activeViewContainer instanceof TextViewContainer) {
                        TextEditor.edit((TextViewContainer) activeViewContainer);
                    }
                }
            }

            @Override
            void onFingerDown(int fingerCount, TouchData touchData, @Nullable PinchData pinchData) {
                if (fingerCount == 2) {
                    if (activeViewContainer != null) {
                        viewInitialScale = activeViewContainer.scale;
                    }
                    setGuidePosition(invisible);
                }
            }

            @Override
            void onOneFingerDrag(TouchData touchData) {
                if (activeViewContainer != null) {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) activeViewContainer.view.getLayoutParams();
                    PositionSnapperUpdated.SnapResult result = positionSnapper.getSnappedPosition(params, touchData.getDeltaPosition());
                    activeViewContainer.setPosition(result.viewPosition);
                    setGuidePosition(result.snapPosition);
                }
            }

            @Override
            void onTwoFingerDrag(PinchData pinchData) {
                if (activeViewContainer != null) {
                    activeViewContainer.move(pinchData.getDeltaMidpoint());
                    activeViewContainer.view.setRotation(activeViewContainer.view.getRotation() + pinchData.getDeltaRotation());
                    activeViewContainer.setScale(viewInitialScale * pinchData.getCurrentScale());
                }
            }

            @Override
            void onFingerUp(int fingerCount, TouchData touchData, boolean click) {
                if (fingerCount == 1) {
                    activeViewContainer = null;
                }
                setGuidePosition(invisible);
            }

            void setGuidePosition(Vector2D position) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) xAlignmentGuide.getLayoutParams();
                params.leftMargin = (int) position.x - params.width / 2;
                xAlignmentGuide.setLayoutParams(params);
                params = (FrameLayout.LayoutParams) yAlignmentGuide.getLayoutParams();
                params.topMargin = (int) position.y - params.height / 2;
                yAlignmentGuide.setLayoutParams(params);
            }
        }

    }


    abstract class ViewOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //Log.e("ViewTouch",motionEvent.getPointerCount()+"");
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    //if (motionEvent.getPointerCount() == 1) {
                    if (Foreground.touchListener.activeViewContainer == null) {
                        Foreground.touchListener.activeViewContainer = getViewContainer();
                    }
                    //Log.e("ViewTouch", "" + (Foreground.touchListener.activeViewContainer == null));
                    //}
            }
            return false;
        }

        abstract ViewContainer getViewContainer();

    }


    public void onShowForegroundEditorClick(View view) {
        Foreground.show();
        Background.hide();
        copyBackgroundToForeground();
    }

    public void onShowBackgroundEditorClick(View view) {
        Foreground.hide();
        Background.show();
    }

    //TODO optimize new drawable being generated every switch
    void copyBackgroundToForeground() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) Foreground.canvas.getLayoutParams();
        String newDimensionRatio = ((ConstraintLayout.LayoutParams) Background.canvas.getLayoutParams()).dimensionRatio;
        Foreground.canvas.setBackground(Background.canvas.getBackground().getConstantState().newDrawable());
        params.dimensionRatio = newDimensionRatio;
        Foreground.canvas.setLayoutParams(params);
        Foreground.canvas.post(
                new Runnable() {
                    @Override
                    public void run() {
                        float scaleX = Foreground.canvas.getMeasuredWidth() / (float) Background.canvas.getMeasuredWidth();
                        float scaleY = Foreground.canvas.getMeasuredHeight() / (float) Background.canvas.getMeasuredHeight();
                        FrameLayout.LayoutParams fgImageParams = (FrameLayout.LayoutParams) Foreground.backgroundImage.getLayoutParams();
                        FrameLayout.LayoutParams bgImageParams = (FrameLayout.LayoutParams) Background.backgroundImage.getLayoutParams();
                        fgImageParams.leftMargin = (int) (bgImageParams.leftMargin * scaleX);
                        fgImageParams.width = (int) (bgImageParams.width * scaleX);
                        fgImageParams.topMargin = (int) (bgImageParams.topMargin * scaleY);
                        fgImageParams.height = (int) (bgImageParams.height * scaleX);
                        Foreground.backgroundImage.setRotation(Background.backgroundImage.getRotation());
                        Foreground.backgroundImage.setLayoutParams(fgImageParams);
                    }
                }
        );
    }

    abstract class ColorPickerSheet {
        BottomSheetDialog dialog;
        ColorPickerView colorPickerView;

        ColorPickerSheet() {
            dialog = new BottomSheetDialog(CreateBackgroundActivity.this, R.style.BottomSheetDialogTheme);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            View bottomSheetView = LayoutInflater.from(CreateBackgroundActivity.this).inflate(R.layout.bottom_sheet_color, (ViewGroup) findViewById(R.id.bottom_sheet_container));
            colorPickerView = bottomSheetView.findViewById(R.id.color_picker);
            colorPickerView.setOnColorChangedListener(
                    new ColorPickerView.OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int newColor) {
                            onChangeColor(newColor);
                        }
                    }
            );
            //To prevent accidental swipe down;
            colorPickerView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    return false;
                }
            });
            dialog.setContentView(bottomSheetView);
        }

        void show() {
            dialog.show();
        }

        void setAlphaSlider(boolean visible) {
            colorPickerView.setAlphaSliderVisible(visible);
        }

        void setColor(int color) {
            colorPickerView.setColor(color);
        }

        abstract void onChangeColor(int newColor);
    }
}

class FontSpinnerAdapter extends ArrayAdapter<String> {

    public FontSpinnerAdapter(@NonNull Context context) {
        super(context, R.layout.support_simple_spinner_dropdown_item, Variables.fontResourceName);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTypeface(ResourcesCompat.getFont(getContext(), Variables.fontResourceId[position]));
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTypeface(ResourcesCompat.getFont(getContext(), Variables.fontResourceId[position]));
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        return view;
    }
}


class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {

            fragment = new BackgroundImageFragment();
        }
        else if (position == 1) {
            fragment = new BackgroundColorFragment();
        }
        else if (position == 2) {
            fragment = new RatioFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0) {
            title = "Image";
        }
        else if (position == 1) {
            title = "Fill";
        }
        else if (position == 2) {
            title = "Crop";
        }
        return title;
    }
}
