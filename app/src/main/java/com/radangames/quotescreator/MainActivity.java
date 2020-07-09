package com.radangames.quotescreator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public FrameLayout canvas;
    // public ImageView tool;
    ConstraintLayout mainmenu;
    ConstraintLayout textEditor;
    static ViewContainer selectedViewContainer;
    EditText editTextInput;
    SeekBar seekBarfontSize;
    InputMethodManager imm;

    static final int IMAGE_REQUEST_CODE = 1;
    static int statusBarHeight;
    final int defaultWidth = 400;
    final int defaultHeight = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);*/
        setContentView(R.layout.activity_main);
        //tool = findViewById(R.id.tool);
        canvas = findViewById(R.id.canvas);
        mainmenu = findViewById(R.id.mainmenu);
        textEditor = findViewById(R.id.textEditor);
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

/*
        ViewTreeObserver viewTreeObserver = canvas.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                canvasWidth = canvas.getWidth();
                canvasHeight = canvas.getHeight();
            }
        });*/

    }

    public void onAddTextClick(View view) {
        final ViewContainer vc = new ViewContainer();
        vc.minHeight = vc.minWidth = 100;
        TextView textView = new AppCompatTextView(MainActivity.this);
        textView.setText("This is a test text\nhello hi hi\nyudsf");
        textView.setGravity(Gravity.CENTER);
        //textView.setPadding(10, 10, 10, 10);
        Random rnd = new Random();
        //textView.setTextColor(Color.argb(255, 100 + rnd.nextInt(156), 100 + rnd.nextInt(156), 100 + rnd.nextInt(156)));
        textView.setTextColor(Color.argb(255,255,255,255));
        vc.view = textView;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        vc.textSizePx = textView.getTextSize();
        /*textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectView(vc);
            }
        });
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                onSelectView(vc);
                //canvas.onTouchEvent(motionEvent);
                return false;
            }
        });*/
        ViewOnTouchListener touchListener = new ViewOnTouchListener();
        touchListener.viewContainer = vc;
        textView.setOnTouchListener(touchListener);
        //textView.setOnTouchListener(new CustomOnTouchListener());
        addViewToCanvas(textView);
        textView.performClick();
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

    static void onSelectView(ViewContainer vc) {
        selectedViewContainer = vc;
        //vc.view.bringToFront();
        vc.view.setClickable(false);
        Log.e("Debug", "View Selected");
    }

    public void onTextCommit(View view) {
        textEditor.clearFocus();
        textEditor.setVisibility(View.GONE);
        mainmenu.setVisibility(View.VISIBLE);
        TextView selectedTextView = (TextView) selectedViewContainer.view;
        String textContent = editTextInput.getText().toString();
        imm.hideSoftInputFromWindow(editTextInput.getWindowToken(), 0);
        if (textContent.equals("")) {
            canvas.removeView(selectedTextView);
            return;
        }
        selectedTextView.setTextColor(editTextInput.getCurrentTextColor());
        selectedTextView.setText(textContent);
        selectedTextView.setTypeface(editTextInput.getTypeface());
        selectedTextView.setGravity(editTextInput.getGravity());
        selectedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextInput.getTextSize() * selectedViewContainer.scale);
        selectedViewContainer.textSizePx = editTextInput.getTextSize();
        selectedViewContainer.width = editTextInput.getWidth() + 25;
        selectedViewContainer.height = editTextInput.getHeight() + 25;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedViewContainer.view.getLayoutParams();
        int newWidth = (int) ((editTextInput.getWidth() + 25) * selectedViewContainer.scale);
        int newHeight = (int) ((editTextInput.getHeight() + 25) * selectedViewContainer.scale);
        params.leftMargin += (params.width - newWidth) / 2;
        params.topMargin += (params.height - newHeight) / 2;
        params.width = newWidth;
        params.height = newHeight;
        selectedTextView.setLayoutParams(params);
        /*//TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(selectedTextView, 10, (int) editTextInput.getTextSize(), 2, TypedValue.COMPLEX_UNIT_PX);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //selectedTextView.setAutoSizeTextTypeUniformWithConfiguration(10,(int)editTextInput.getTextSize(),2,TypedValue.COMPLEX_UNIT_PX);
        }
        else {

        }*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            ImageView imageView = new ImageView(MainActivity.this);
            imageView.setImageURI(imageUri);
            final ViewContainer vc = new ViewContainer();
            vc.minHeight = vc.minWidth = 100;
            vc.view = imageView;
            /*imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    onSelectView(vc);
                    //canvas.onTouchEvent(motionEvent);
                    return false;
                }
            });*/
            ViewOnTouchListener touchListener = new ViewOnTouchListener();
            touchListener.viewContainer = vc;
            imageView.setOnTouchListener(touchListener);
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
            //imageView.setPadding(25, 25, 25, 25);
            imageView.performClick();
        }
    }
    public void ShowColorPicker(View view){
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
                        //((TextView)selectedViewContainer.view).setTextColor(selectedColor);
                        editTextInput.setTextColor(selectedColor);
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
    public void onAlignmentClick(View view){
        x = (x+1)%3;
        editTextInput.setGravity(alignments[x]);
        Log.e("dsdfs","sdfsdfsdfs"+x);
    }
    public void onFontClick(View view){
        int fontName;
        if(x == 0)
            fontName = R.font.bungee_regular;
        else if (x==1)
            fontName = R.font.merianda_bold;
        else
            fontName = R.font.montserrat_light;
        x = (x+1)%3;
        editTextInput.setTypeface(ResourcesCompat.getFont(this, fontName));
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
                    ViewOnTouchListener.selectEnabled = true;
                    if (motionEvent.getPointerCount() == 2) {
                        //FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedViewContainer.view.getLayoutParams();
                        startScale = selectedViewContainer.scale / Vector2D.Distance(activePointers.valueAt(0), activePointers.valueAt(1));
                        click = false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    click = false;
                    int pointerId0 = motionEvent.getPointerId(0);
                    Vector2D lastPosition0 = activePointers.get(pointerId0);
                    activePointers.put(pointerId0, new Vector2D(view, motionEvent, 0));
                    Vector2D touchPosition0 = activePointers.get(pointerId0);

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
                        Vector2D touchPosition1 = activePointers.get(pointerId1);
                        double deltaRotation = Math.atan2((touchPosition1.y - touchPosition0.y), (touchPosition1.x - touchPosition0.x)) - Math.atan2((lastPosition1.y - lastPosition0.y), (lastPosition1.x - lastPosition0.x));
                        selectedViewContainer.view.setRotation(selectedViewContainer.view.getRotation() + (float) Math.toDegrees(deltaRotation));
                        float newScale = startScale * Vector2D.Distance(touchPosition0, touchPosition1);
                        if (newScale < .2f || newScale > 3) {
                            return true;
                        }
                        Vector2D lastMidPoint = lastPosition0.plus(lastPosition1).scalarDivide(2);
                        Vector2D midPoint = touchPosition0.plus(touchPosition1).scalarDivide(2);
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
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    activePointers.remove(currentPointerId);
                    if (motionEvent.getPointerCount() == 1) {
                        if (click) {
                            canvas.performClick();
                            selectedViewContainer.view.setClickable(true);
                            if (selectedViewContainer.view instanceof TextView) {
                                mainmenu.setVisibility(View.GONE);
                                textEditor.setVisibility(View.VISIBLE);
                                TextView textView = (TextView) selectedViewContainer.view;
                                editTextInput.setGravity(textView.getGravity());
                                editTextInput.setTextColor(textView.getTextColors());
                                editTextInput.setText(textView.getText());
                                editTextInput.requestFocus();
                                editTextInput.setTypeface(textView.getTypeface());
                                imm.showSoftInput(editTextInput, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
    }
/*
    class CustomOnTouchListener1 implements View.OnTouchListener {

        int edgeHandleWidth = 50;

        //int x, y;
        //int marginX, marginY;
        Vector2D touchSide;
        Vector2D touchPosition;
        float lastRotation;
        float viewStartRotation;
        boolean dragging = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tool.getLayoutParams();
            //Position with centre anchor
            Vector2D viewPosition = new Vector2D(params.leftMargin + (float) params.width / 2, params.topMargin + (float) params.height / 2);
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    touchPosition = Vector2D.getPositionRelativeToParent(canvas, motionEvent);
                    touchSide = getTouchSide(tool, motionEvent);
                    lastRotation = (float) Math.toDegrees(Math.atan2(-viewPosition.x + touchPosition.x, -viewPosition.y + touchPosition.y));
                    viewStartRotation = selectedViewContainer.view.getRotation();
                    break;

                case MotionEvent.ACTION_MOVE:
                    dragging = true;
                    Vector2D touchDelta = Vector2D.getPositionRelativeToParent(canvas, motionEvent).minus(touchPosition);
                    touchPosition = Vector2D.getPositionRelativeToParent(canvas, motionEvent);
                    if (touchSide.x == -1 && touchSide.y == 1 || touchSide.x == 1 && touchSide.y == -1) {
                        float rotationDelta = lastRotation - (float) (Math.toDegrees(Math.atan2(-viewPosition.x + touchPosition.x, -viewPosition.y + touchPosition.y)));
                        selectedViewContainer.view.setRotation(viewStartRotation + rotationDelta);
                        tool.setRotation(selectedViewContainer.view.getRotation());
                    }
                    else if (touchSide.x == 1 && touchSide.y == 1) {
                        if (touchPosition.x > canvas.getMeasuredWidth() || touchPosition.y > canvas.getMeasuredHeight() || touchPosition.x < 0 || touchPosition.y < 0) {
                            return false;
                        }
                        Vector2D touchDeltaRotated = touchDelta.rotate(-selectedViewContainer.view.getRotation() + 0);
                        if (params.width + touchDeltaRotated.x * 2 < selectedViewContainer.minWidth || params.height + touchDeltaRotated.y * 2 < selectedViewContainer.minHeight) {
                            return true;
                        }
                        params.width += (int) touchDeltaRotated.x * 2;
                        params.leftMargin -= (int) touchDeltaRotated.x;
                        params.height += (int) touchDeltaRotated.y * 2;
                        params.topMargin -= (int) touchDeltaRotated.y;
                    }
                    else {
                        if (touchPosition.x > canvas.getMeasuredWidth() || touchPosition.y > canvas.getMeasuredHeight() || touchPosition.x < 0 || touchPosition.y < 0) {
                            return false;
                        }
                        params.leftMargin += touchDelta.x;
                        params.topMargin += touchDelta.y;
                    }

                    /*if (touchSide.x == -1 && params.width - touchDelta.x > selectedViewContainer.minWidth) {
                        params.width -= touchDelta.x;
                        params.leftMargin += touchDelta.x;
                    }
                    else if (touchSide.x == 1 && params.width + touchDelta.x > selectedViewContainer.minWidth) {
                        params.width += touchDelta.x;
                    }
                    if (touchSide.y == -1 && params.height - touchDelta.y > selectedViewContainer.minHeight) {
                        params.height -= touchDelta.y;
                        params.topMargin += touchDelta.y;
                    }
                    else if (touchSide.y == 1 && params.height + touchDelta.y > selectedViewContainer.minHeight) {
                        params.height += touchDelta.y;
                    }
                    selectedViewContainer.view.setLayoutParams(params);
                    tool.setLayoutParams(params);
                    //Log.e("TouchPosition", touchPosition.toString());
                    break;

                case MotionEvent.ACTION_UP:
                    if (!dragging) {
                        if (touchSide.x == -1 && touchSide.y == -1) {
                            canvas.removeView(selectedViewContainer.view);
                            tool.setVisibility(View.GONE);
                        }
                        else {
                            //view.performClick();
                            if (selectedViewContainer.view instanceof TextView) {
                                mainmenu.setVisibility(View.GONE);
                                textEditor.setVisibility(View.VISIBLE);
                                editTextInput.setText(((TextView) selectedViewContainer.view).getText());
                                editTextInput.requestFocus();
                                imm.showSoftInput(editTextInput, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                        //tool.performClick();
                    }
                    dragging = false;
                    break;

            }
            return true;
        }

        Vector2D getTouchSide(View view, MotionEvent motionEvent) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            final int width = params.width;
            final int height = params.height;
            int touchPositionX = (int) motionEvent.getX();
            int touchPositionY = (int) motionEvent.getY();

            Vector2D vector2D = new Vector2D();
            if (touchPositionY > -10 && touchPositionY < edgeHandleWidth) {
                vector2D.y = -1;
            }
            else if (touchPositionY < height - edgeHandleWidth) {
                vector2D.y = 0;
            }
            else if (touchPositionY < height + 10) {
                vector2D.y = 1;
            }

            if (touchPositionX > -10 && touchPositionX < edgeHandleWidth) {
                vector2D.x = -1;
            }
            else if (touchPositionX < width - edgeHandleWidth) {
                vector2D.x = 0;
            }
            else if (touchPositionX < width + 10) {
                vector2D.x = 1;
            }
            return vector2D;
        }
    }*/
}
class ViewOnTouchListener implements View.OnTouchListener{

    public ViewContainer viewContainer;
    public static Boolean selectEnabled = true;
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(!selectEnabled)
            return false;
        MainActivity.onSelectView(viewContainer);
        selectEnabled = false;
        return false;
    }
}