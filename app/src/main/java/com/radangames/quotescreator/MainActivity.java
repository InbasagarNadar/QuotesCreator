package com.radangames.quotescreator;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {

    public FrameLayout canvas;
    // public ImageView tool;
    ConstraintLayout mainmenu;
    ConstraintLayout textEditorOverlay;

    public ViewContainer selectedViewContainer;
    EditText editTextInput;
    SeekBar seekBarfontSize;
    InputMethodManager imm;

    static final int IMAGE_REQUEST_CODE = 1;
    static int statusBarHeight;
    final int defaultWidth = 400;
    final int defaultHeight = 400;

    static final int TEXT_SPACING = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);*/
        setContentView(R.layout.activity_main);
        //tool = findViewById(R.id.tool);
        canvas = findViewById(R.id.canvas);
        mainmenu = findViewById(R.id.mainmenu);
        textEditorOverlay = findViewById(R.id.textEditor);
        editTextInput = findViewById(R.id.editTextInput);
        seekBarfontSize = findViewById(R.id.seekBarFontSize);
        seekBarfontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editTextInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10 + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ((SeekBar) findViewById(R.id.kerningSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editTextInput.setLetterSpacing((float) i / 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
       /* canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tool.setVisibility(View.GONE);
                mainmenu.setVisibility(View.VISIBLE);
                selectedViewContainer = null;
            }
        });*/
        canvas.setDrawingCacheEnabled(true);
        canvas.setOnTouchListener(new CustomOnTouchListener());
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        statusBarHeight = getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"));
        /*tool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "kjdfglkjadlkj", Toast.LENGTH_SHORT).show();
            }
        });

*/
        ViewTreeObserver viewTreeObserver = canvas.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainmenu.getLayoutParams().width = mainmenu.getWidth();
                mainmenu.getLayoutParams().height = mainmenu.getHeight();
            }
        });

    }

    void addViewToCanvas(View view) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(canvas.getMeasuredWidth(), canvas.getHeight());
        params.leftMargin = canvas.getWidth() / 2 - 150;
        params.topMargin = canvas.getHeight() / 2 - 200;
        params.width = 300;
        params.height = 400;
        //params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        canvas.addView(view, params);
        //view.setPadding(50, 50, 50, 50);
    }

    public void onAddImageClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST_CODE);
    }

    public void onSaveClick(View view) {
        try {
            Bitmap b = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            canvas.draw(c);
            b.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(Environment.getExternalStorageDirectory() + "/Download/file.jpg"));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void onSelectView(ViewContainer vc) {
        selectedViewContainer = vc;
        //vc.view.setClickable(false);
        Log.e("Debug", "View Selected");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            ImageView imageView = new ImageView(MainActivity.this);
            imageView.setImageURI(imageUri);
            final ViewContainer vc = new ViewContainer();
            // vc.minHeight = vc.minWidth = 100;
            vc.view = imageView;
            /*imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    onSelectView(vc);
                    //canvas.onTouchEvent(motionEvent);
                    return false;
                }
            });*/
            imageView.setOnTouchListener(new ViewOnTouchListener(vc));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeStream(
                        MainActivity.this.getContentResolver().openInputStream(imageUri),
                        null,
                        options);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            float imageAspectRatio = (float) imageWidth / (float) imageHeight;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(canvas.getMeasuredWidth(), canvas.getHeight());
            if (imageAspectRatio < 1) {
                int height = (int) (defaultWidth / imageAspectRatio);
                params.leftMargin = canvas.getWidth() / 2 - defaultWidth / 2;
                params.topMargin = canvas.getHeight() / 2 - height / 2;
                params.width = defaultWidth;
                params.height = height;
            }
            else {
                int width = (int) (defaultHeight * imageAspectRatio);
                params.leftMargin = canvas.getWidth() / 2 - width / 2;
                params.topMargin = canvas.getHeight() / 2 - defaultHeight / 2;
                params.width = width;
                params.height = defaultHeight;
            }
            vc.width = params.width;
            vc.height = params.height;
            vc.scale = 1;
            canvas.addView(imageView, params);
            imageView.performClick();
        }
    }

    public void ShowColorPicker(View view) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(editTextInput.getCurrentTextColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .lightnessSliderOnly()
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        SpannableStringBuilder spannable = (SpannableStringBuilder) editTextInput.getText();

                        int selectionStart = 0;
                        int selectionEnd = spannable.length();

                        if (editTextInput.hasSelection()) {
                            selectionStart = editTextInput.getSelectionStart();
                            selectionEnd = editTextInput.getSelectionEnd();
                            spannable.setSpan(new ForegroundColorSpan(selectedColor), selectionStart, selectionEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        else {
                            editTextInput.setTextColor(selectedColor);
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build().show();
    }

    int[] alignments = {Gravity.CENTER, Gravity.LEFT, Gravity.RIGHT};
    int x = 0;

    public void onAlignmentClick(View view) {
        x = (x + 1) % 3;
        editTextInput.setGravity(alignments[x]);
    }

    public void onFontClick(View view) {
        int fontName;
        if (x == 0) {
            fontName = R.font.bungee_regular;
        }
        else if (x == 1) {
            fontName = R.font.merianda_bold;
        }
        else {
            fontName = R.font.montserrat_light;
        }
        x = (x + 1) % 3;
        editTextInput.setTypeface(ResourcesCompat.getFont(this, fontName));
    }

    public void onAddTextClick(View view) {
        final ViewContainer viewContainer = new ViewContainer();
        TextView textView = new AppCompatTextView(MainActivity.this);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32.5f);
        textView.setTextColor(Color.argb(255, 255, 255, 255));
        textView.setOnTouchListener(new ViewOnTouchListener(viewContainer));

        viewContainer.view = textView;
        viewContainer.textSizePx = textView.getTextSize();
        addViewToCanvas(textView);
        selectedViewContainer = viewContainer;
        showEditText(textView);
    }

    void showEditText(TextView text) {

        editTextInput.setGravity(text.getGravity());
        editTextInput.setTextColor(text.getTextColors());
        editTextInput.setText(selectedViewContainer.text);
        editTextInput.setTypeface(text.getTypeface());
        editTextInput.setLetterSpacing(text.getLetterSpacing());
        editTextInput.requestFocus();
        imm.showSoftInput(editTextInput, InputMethodManager.SHOW_IMPLICIT);

        seekBarfontSize.setProgress((int) (selectedViewContainer.textSizePx / getResources().getDisplayMetrics().density) - 10);
        //mainmenu.setVisibility(View.GONE);

        textEditorOverlay.setVisibility(View.VISIBLE);
    }

    public void onTextCommit(View view) {
        textEditorOverlay.clearFocus();
        textEditorOverlay.setVisibility(View.GONE);
        imm.hideSoftInputFromWindow(editTextInput.getWindowToken(), 0);

        TextView textView = (TextView) selectedViewContainer.view;
        String textContent = editTextInput.getText().toString();

        //If Text is empty, then remove the object
        if (textContent.equals("")) {
            canvas.removeView(textView);
            return;
        }
        selectedViewContainer.text = new SpannableStringBuilder((SpannableStringBuilder) editTextInput.getText());
        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) editTextInput.getText();

//        StringBuilder stringBuilder = new StringBuilder(editTextInput.getText().toString());

        for (int lineCount = editTextInput.getLineCount() - 1; lineCount > 0; --lineCount) {
            int end = editTextInput.getLayout().getLineStart(lineCount);
            if (stringBuilder.charAt(end - 1) != '\n') {
                stringBuilder.insert(end, "\n");
            }
        }

        textView.setLetterSpacing(editTextInput.getLetterSpacing());
        textView.setTextColor(editTextInput.getCurrentTextColor());
        textView.setText(stringBuilder);
        textView.setTypeface(editTextInput.getTypeface());
        textView.setGravity(editTextInput.getGravity());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextInput.getTextSize() * selectedViewContainer.scale);
        selectedViewContainer.textSizePx = editTextInput.getTextSize();

        selectedViewContainer.width = editTextInput.getWidth() + TEXT_SPACING;
        selectedViewContainer.height = editTextInput.getHeight() + TEXT_SPACING;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedViewContainer.view.getLayoutParams();
        int newWidth = (int) ((editTextInput.getWidth() + TEXT_SPACING) * selectedViewContainer.scale);
        int newHeight = (int) ((editTextInput.getHeight() + TEXT_SPACING) * selectedViewContainer.scale);
        params.leftMargin += (params.width - newWidth) / 2;
        params.topMargin += (params.height - newHeight) / 2;
        params.width = newWidth;
        params.height = newHeight;
        textView.setLayoutParams(params);
        textView.setZ(textView.getZ());
        selectEnabled = true;

    }

    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                startScale, endScale,
                startScale, endScale,
                Animation.RELATIVE_TO_SELF, .5f,
                Animation.RELATIVE_TO_SELF, .5f);
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(75);
        v.startAnimation(anim);
    }

    class CustomOnTouchListener implements View.OnTouchListener {

        SparseArray<Vector2D> activePointers = new SparseArray<>();
        float startScale = 1;
        boolean click = true;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int currentPointerIndex = motionEvent.getActionIndex();
            int currentPointerId = motionEvent.getPointerId(currentPointerIndex);
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    activePointers.put(currentPointerId, new Vector2D(view, motionEvent, currentPointerIndex));
                    click = true;
                    selectEnabled = true;

                    if (motionEvent.getPointerCount() == 1 && click) {
                        scaleView(selectedViewContainer.view, 1, .9f);
                    }
                    else if (motionEvent.getPointerCount() == 2) {
                        startScale = selectedViewContainer.scale / Vector2D.Distance(activePointers.valueAt(0), activePointers.valueAt(1));
                        click = false;
                    }
                    if (!click) {
                        scaleView(selectedViewContainer.view, .9f, 1);
                    }
                    //Log.e("Custom", "Down");
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (click) {
                        scaleView(selectedViewContainer.view, .9f, 1);
                    }
                    click = false;
                    int pointerId0 = motionEvent.getPointerId(0);
                    Vector2D lastPosition0 = activePointers.get(pointerId0);
                    activePointers.put(pointerId0, new Vector2D(view, motionEvent, 0));
                    Vector2D currentPosition0 = activePointers.get(pointerId0);

                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedViewContainer.view.getLayoutParams();
                    if (motionEvent.getPointerCount() == 1) {
                        Vector2D deltaPosition0 = activePointers.get(pointerId0).minus(lastPosition0);
                        params.leftMargin += (int) deltaPosition0.x;
                        params.topMargin += (int) deltaPosition0.y;

                        selectedViewContainer.view.setLayoutParams(params);
                    }
                    if (motionEvent.getPointerCount() == 2) {

                        int pointerId1 = motionEvent.getPointerId(1);
                        Vector2D lastPosition1 = activePointers.get(pointerId1);
                        activePointers.put(pointerId1, new Vector2D(view, motionEvent, 1));
                        Vector2D currentPosition1 = activePointers.get(pointerId1);
                        double deltaRotation = Math.atan2((currentPosition1.y - currentPosition0.y), (currentPosition1.x - currentPosition0.x)) - Math.atan2((lastPosition1.y - lastPosition0.y), (lastPosition1.x - lastPosition0.x));
                        selectedViewContainer.view.setRotation(selectedViewContainer.view.getRotation() + (float) Math.toDegrees(deltaRotation));
                        float newScale = startScale * Vector2D.Distance(currentPosition0, currentPosition1);
                        if (newScale < .2f || newScale > 3) {
                            return true;
                        }
                        Vector2D lastMidPoint = lastPosition0.plus(lastPosition1).scalarDivide(2);
                        Vector2D midPoint = currentPosition0.plus(currentPosition1).scalarDivide(2);
                        Vector2D deltaMidpoint = midPoint.minus(lastMidPoint);
                        params.leftMargin += (int) (deltaMidpoint.x - (selectedViewContainer.width * (newScale - selectedViewContainer.scale)) / 2);
                        params.topMargin += (int) (deltaMidpoint.y - (selectedViewContainer.height * (newScale - selectedViewContainer.scale)) / 2);
                        params.width = (int) (selectedViewContainer.width * newScale);
                        params.height = (int) (selectedViewContainer.height * newScale);
                        selectedViewContainer.view.setLayoutParams(params);
                        if (selectedViewContainer.view instanceof TextView) {
                            TextView textView = (TextView) selectedViewContainer.view;
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectedViewContainer.textSizePx * newScale);
                        }
                        selectedViewContainer.scale = newScale;
                    }
                    //Log.e("Custom", "Drag");
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    selectEnabled = true;
                    activePointers.remove(currentPointerId);
                    if (motionEvent.getPointerCount() == 1) {
                        if (click) {

                            scaleView(selectedViewContainer.view, .9f, 1);

                            canvas.performClick();
                            //selectedViewContainer.view.setClickable(true);
                            if (selectedViewContainer.view instanceof TextView) {
                                showEditText((TextView) selectedViewContainer.view);
                            }
                        }
                    }
                    //Log.e("Custom", "Up");
                    break;
            }
            // Log.e("selectEnabled", selectEnabled + "");
            return true;
        }
    }

    static public Boolean selectEnabled = true;

    class ViewOnTouchListener implements View.OnTouchListener {
        public ViewContainer viewContainer;

        public ViewOnTouchListener(ViewContainer vc) {
            viewContainer = vc;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //Log.e("hmm", ((TextView) view).getText().toString());
            if (selectEnabled) {
                onSelectView(viewContainer);
                selectEnabled = false;
            }
            return false;
        }
    }
}
