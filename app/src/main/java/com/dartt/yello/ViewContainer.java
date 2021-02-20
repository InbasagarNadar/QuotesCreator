package com.dartt.yello;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/*
 * Base Class for elements (Image, Text, Sticker etc)
 * */


public class ViewContainer implements JsonSerializable {
    public View view;
    public float scale = 1;
    public float width;
    public float height;
    //public SpannableStringBuilder text;

    public void detachFromParent() {
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

    public void setDimension(Vector2D dimension, boolean resetScale) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        int newWidth = (int) (scale * dimension.x);
        int newHeight = (int) (scale * dimension.y);
        params.leftMargin -= (newWidth - params.width) / 2;
        params.topMargin -= (newHeight - params.height) / 2;
        params.width = newWidth;
        params.height = newHeight;
        width = dimension.x;
        height = dimension.y;
        if (resetScale) {
            scale = 1;
        }
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

    public JSONObject toJSON() throws JSONException {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("x", params.leftMargin);
        jsonObject.put("y", params.topMargin);
        jsonObject.put("width", params.width);
        jsonObject.put("height", params.height);
        jsonObject.put("scale_x", view.getScaleX());
        jsonObject.put("scale_y", view.getScaleY());
        jsonObject.put("rotation", view.getRotation());
        jsonObject.put("alpha", view.getAlpha());
        jsonObject.put("visible", view.getVisibility() == View.VISIBLE);
        return jsonObject;
    }

    public void copyAttrFromJSON(JSONObject jsonObject) throws JSONException {
        if (jsonObject != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.leftMargin = jsonObject.getInt("x");
            params.topMargin = jsonObject.getInt("y");
            width = params.width = jsonObject.getInt("width");
            height = params.height = jsonObject.getInt("height");
            view.setScaleX((float) jsonObject.getDouble("scale_x"));
            view.setScaleY((float) jsonObject.getDouble("scale_y"));
            view.setRotation((float) jsonObject.getDouble("rotation"));
            view.setAlpha((float) jsonObject.getDouble("alpha"));
            view.setVisibility(jsonObject.getBoolean("visible") ? View.VISIBLE : View.GONE);
            scale = 1;
            view.setLayoutParams(params);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}


@SuppressLint("ClickableViewAccessibility")
class StickerViewContainer extends ViewContainer {
    static final float MIN_SCALE = .1f;
    final int defaultWidth;
    int resourceId;

    StickerViewContainer(EditorFragment editorFragment) {
        ImageView imageView = new ImageView(editorFragment.activity);
        imageView.setOnTouchListener(new EditorFragment.ViewOnTouchListener(editorFragment) {
            @Override
            ViewContainer getViewContainer() {
                return StickerViewContainer.this;
            }
        });
        view = imageView;
        defaultWidth = Variables.densityToPixel(editorFragment.activity, 200);
    }

    StickerViewContainer(EditorFragment editorFragment, Drawable drawable, int resourceId) {
        this(editorFragment);
        getView().setImageDrawable(drawable);
        this.resourceId = resourceId;
    }

    void resetScale() {
        Bitmap bitmap = ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap();
        float imageRatio = bitmap.getWidth() / (float) bitmap.getHeight();
        setDimension(new Vector2D(defaultWidth, defaultWidth / imageRatio), true);
        scale = 1;
    }

    ImageView getView() {
        return (ImageView) view;
    }

    float getAlpha() {
        return getView().getAlpha();
    }

    void setAlpha(float opacity) {
        getView().setAlpha(opacity);
    }

    @Override
    public void setScale(float newScale) {
        if (newScale < MIN_SCALE) {
            return;
        }
        super.setScale(newScale);
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = super.toJSON();
        jsonObject.put("type", "sticker");
        jsonObject.put("resourceId", resourceId);
        return jsonObject;
    }

    @Override
    public void copyAttrFromJSON(JSONObject jsonObject) throws JSONException {
        super.copyAttrFromJSON(jsonObject);
        if (jsonObject != null) {
            resourceId = jsonObject.getInt("resourceId");
            Glide.with(view).load(resourceId).into(getView());
        }
    }
}

class ImageViewContainer extends ViewContainer {

    static final int BACKGROUND_TYPE_NULL = 0;
    static final int BACKGROUND_TYPE_RESOURCE_ID = 1;
    static final int BACKGROUND_TYPE_URI = 2;
    static final int BACKGROUND_TYPE_URL = 3;

    Bitmap imageBitmap;
    Bitmap imageBitmapResized;

    int currentBackgroundType = BACKGROUND_TYPE_NULL;
    int currentBackgroundResourceId;
    Uri currentBackgroundUri;
    String currentBackgroundUrl;
    FunctionParameter callback;

    boolean fillCentre = true;

    CustomTarget<Drawable> target = new CustomTarget<Drawable>() {
        @Override
        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
            setDrawable(resource);
            if (fillCentre) {
                fillParent();
            }
            if (callback != null) {
                callback.execute();
                callback = null;
            }
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {

        }
    };

    @SuppressLint("ClickableViewAccessibility")
    ImageViewContainer(EditorFragment editorFragment) {
        ImageView imageView = new ImageView(editorFragment.activity);
        imageView.setOnTouchListener(new EditorFragment.ViewOnTouchListener(editorFragment) {
            @Override
            ViewContainer getViewContainer() {
                return ImageViewContainer.this;
            }
        });
        view = imageView;
    }

    void removeImage() {
        getView().setImageDrawable(null);
        currentBackgroundType = BACKGROUND_TYPE_NULL;
    }

    void setImage(final Uri imageUri, FunctionParameter callback, boolean fillCentre) {
        this.fillCentre = fillCentre;
        this.callback = callback;
        currentBackgroundType = BACKGROUND_TYPE_URI;
        currentBackgroundUri = imageUri;
        Glide.with(getView()).load(imageUri).into(target);
    }

    void setImage(final int resourceId, FunctionParameter callback, boolean fillCentre) {
        this.fillCentre = fillCentre;
        this.callback = callback;
        currentBackgroundType = BACKGROUND_TYPE_RESOURCE_ID;
        currentBackgroundResourceId = resourceId;
        Glide.with(getView()).load(resourceId).into(target);
    }

    void setImage(final String url, FunctionParameter callback, boolean fillCentre) {
        this.fillCentre = fillCentre;
        this.callback = callback;
        currentBackgroundType = BACKGROUND_TYPE_URL;
        currentBackgroundUrl = url;
        Glide.with(getView()).load(url).into(target);
    }

    void setDrawable(Drawable resource) {
        ImageView imageView = getView();
        imageBitmap = ((BitmapDrawable) resource).getBitmap();
        imageBitmapResized = SaveActivity.getResizedBitmap(imageBitmap, 512);
        imageView.setImageBitmap(imageBitmap);

    }

    void fillParent() {
        ImageView imageView = getView();
        View parent = (View) imageView.getParent();
        int[] imageDimension = new int[]{imageBitmap.getWidth(), imageBitmap.getHeight()};
        int[] canvasDimension = new int[]{parent.getWidth(), parent.getHeight()};
        float imageRatio = (float) imageDimension[0] / (float) imageDimension[1];
        float canvasRatio = (float) canvasDimension[0] / (float) canvasDimension[1];

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
        params.leftMargin = params.topMargin = 0;
        //greater the image ratio, wider the image
        if (imageRatio == canvasRatio) {
            params.width = canvasDimension[0];
            params.height = canvasDimension[1];
        } else if (imageRatio < canvasRatio) {
            params.width = canvasDimension[0];
            params.height = (int) Math.ceil(canvasDimension[0] / imageRatio);
            params.topMargin = (canvasDimension[1] - params.height) / 2;
        } else {
            params.width = (int) Math.ceil(canvasDimension[1] * imageRatio);
            params.height = canvasDimension[1];
            params.leftMargin = (canvasDimension[0] - params.width) / 2;
        }
        height = params.height;
        width = params.width;
        scale = 1;
        imageView.setRotation(0);
        imageView.setLayoutParams(params);
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject background = super.toJSON();
        background.put("type", currentBackgroundType);
        switch (currentBackgroundType) {
            case BACKGROUND_TYPE_URL:
                background.put("background", currentBackgroundUrl);
                break;
            case BACKGROUND_TYPE_URI:
                background.put("background", currentBackgroundUri.toString());
                break;
            case BACKGROUND_TYPE_RESOURCE_ID:
                background.put("background", currentBackgroundResourceId);
                break;
        }
        return background;
    }

    @Override
    public void copyAttrFromJSON(JSONObject jsonObject) throws JSONException {
        currentBackgroundType = jsonObject.getInt("type");
        switch (currentBackgroundType) {
            case BACKGROUND_TYPE_URL:
                setImage(jsonObject.getString("background"), null, false);
                break;
            case BACKGROUND_TYPE_URI:
                setImage(Uri.parse(jsonObject.getString("background")), null, false);
                break;
            case BACKGROUND_TYPE_RESOURCE_ID:
                setImage(jsonObject.getInt("background"), null, false);
                break;
            case BACKGROUND_TYPE_NULL:
                getView().setImageDrawable(null);
        }
        super.copyAttrFromJSON(jsonObject);
    }

    ImageView getView() {
        return (ImageView) view;
    }
}


@SuppressLint("ClickableViewAccessibility")
class TextViewContainer extends ViewContainer {
    FontHolder fontHolder;
    SpannableStringBuilder textContentOriginal;
    final int PADDING = 20;
    final float MIN_SCALE = .25f;
    final int DEFAULT_TEXT_SIZE_DP = 35;

    float textSizePx;
    //int fontIndex = 15;

    TextViewContainer(final EditorFragment editorFragment) {
        fontHolder = LanguageManager.getDefaultFont(editorFragment.getContext());
        TextView textView = new AppCompatTextView(editorFragment.activity);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TEXT_SIZE_DP);
        textSizePx = Variables.densityToPixel(editorFragment.activity, DEFAULT_TEXT_SIZE_DP);
        textView.setTextColor(0xFFFFFFFF);
        textView.setTypeface(fontHolder.typeface);
        textView.setOnTouchListener(new EditorFragment.ViewOnTouchListener(editorFragment) {
            @Override
            ViewContainer getViewContainer() {
                return TextViewContainer.this;
            }
        });

        view = textView;
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
        textContentOriginal = new SpannableStringBuilder(source.getText());
        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) source.getText();
        Layout layout = source.getLayout();
        for (int lineCount = source.getLineCount() - 1; lineCount > 0; --lineCount) {
            int end = layout.getLineStart(lineCount);
            if (stringBuilder.charAt(end - 1) != '\n') {
                stringBuilder.insert(end, "\n");
            }
        }
        getView().setText(stringBuilder);
    }


    public void updateContent(EditText source) {
        TextView textView = getView();
        textView.setLetterSpacing(source.getLetterSpacing());
        textView.setLineSpacing(0, source.getLineSpacingMultiplier());
        textView.setGravity(source.getGravity());
        textView.setTypeface(source.getTypeface());
        textView.setVisibility(View.VISIBLE);
        setText(source);
        setTextSizePx((int) source.getTextSize());
        setDimension(new Vector2D((source.getWidth() + PADDING), (source.getHeight() + PADDING)), false);
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        TextView textView = getView();
        JSONObject jsonObject = super.toJSON();
        if (textContentOriginal != null) {
            jsonObject.put("type", "text");
            jsonObject.put("scale", scale);
            jsonObject.put("languageCode", fontHolder.languageCode);
            jsonObject.put("fontName", fontHolder.name);
            jsonObject.put("textSizePx", textSizePx);
            jsonObject.put("textContentOriginal", Html.toHtml(textContentOriginal));
            jsonObject.put("textViewContent", Html.toHtml(new SpannableStringBuilder(textView.getText())));
            int newLineCount = 0;
            while (textContentOriginal.charAt(textContentOriginal.length() - 1 - newLineCount) == '\n') {
                newLineCount++;
            }
            jsonObject.put("newLineCount", newLineCount);
        }
        return jsonObject;
    }

    @Override
    public void copyAttrFromJSON(JSONObject jsonObject) throws JSONException {
        super.copyAttrFromJSON(jsonObject);
        if (jsonObject != null) {
            TextView textView = getView();
            if (jsonObject.has("textContentOriginal")) {
                String languageCode = jsonObject.getString("languageCode");
                String fontName = jsonObject.getString("fontName");
                fontHolder = LanguageManager.newFontHolder(view.getContext(), languageCode, fontName);
                if (fontHolder == null) {
                    fontHolder = LanguageManager.getDefaultFont(view.getContext());
                }
                textView.setTypeface(fontHolder.typeface);

                scale = (float) jsonObject.getDouble("scale");
                setTextSizePx(jsonObject.getInt("textSizePx"));
                textContentOriginal = new SpannableStringBuilder(Html.fromHtml(jsonObject.getString("textContentOriginal")));
                SpannableStringBuilder textViewContent = new SpannableStringBuilder(Html.fromHtml(jsonObject.getString("textViewContent")));

                // New Line Fix. Basically fixing p tag stuff which added unnecessary new line
                int currentNewLineCount = 0;
                int targetNewLineCount = jsonObject.getInt("newLineCount");
                while (textContentOriginal.charAt(textContentOriginal.length() - 1 - currentNewLineCount) == '\n') {
                    currentNewLineCount++;
                }
                if (currentNewLineCount > targetNewLineCount) {
                    textContentOriginal.delete(textContentOriginal.length() - currentNewLineCount + targetNewLineCount, textContentOriginal.length());
                    textViewContent.delete(textViewContent.length() - currentNewLineCount + targetNewLineCount, textContentOriginal.length());
                } else {
                    while (targetNewLineCount != currentNewLineCount) {
                        textContentOriginal.append("\n");
                        textViewContent.append("\n");
                        currentNewLineCount++;
                    }
                }
                textView.setText(textViewContent);
            }
        }
    }
}

class ArtBoardViewContainer extends ViewContainer {

    final static int FILL_TYPE_COLOR = 0;
    final static int FILL_TYPE_GRADIENT = 1;

    int currentFillType = 0;
    int currentColor;
    int[] currentGradient;
    GradientDrawable.Orientation currentGradientOrientation;

    int oldLeft;
    boolean shiftChildren = false;
    FunctionParameter delegate;

    ArtBoardViewContainer(final View view) {
        this.view = view;
        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (shiftChildren) {
                    shiftChildren(oldLeft);
                    oldLeft = ArtBoardViewContainer.this.view.getLeft();
                    if (delegate != null) {
                        delegate.execute();
                        delegate = null;
                    }
                    shiftChildren = false;
                }
            }
        });
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fillType", currentFillType);
        jsonObject.put("left", view.getLeft());
        if (currentFillType == FILL_TYPE_COLOR) {
            jsonObject.put("color", currentColor);
        } else {
            JSONArray gradient = new JSONArray();
            for (int color : currentGradient) {
                gradient.put(color);
            }
            jsonObject.put("gradient", gradient);
            jsonObject.put("gradientOrientation", currentGradientOrientation.toString());
        }
        jsonObject.put("dimensionRatio", ((ConstraintLayout.LayoutParams) view.getLayoutParams()).dimensionRatio);
        return jsonObject;
    }

    @Override
    public void copyAttrFromJSON(JSONObject jsonObject) throws JSONException {
        currentFillType = jsonObject.getInt("fillType");
        setRatio(jsonObject.getString("dimensionRatio"), false, null);
        if (currentFillType == FILL_TYPE_COLOR) {
            setArtBoardFill(jsonObject.getInt("color"));
        } else {
            JSONArray gradientJson = jsonObject.getJSONArray("gradient");
            int[] gradient = new int[gradientJson.length()];
            for (int i = 0; i < gradientJson.length(); i++) {
                gradient[i] = gradientJson.getInt(i);
            }
            setArtBoardFill(gradient, GradientDrawable.Orientation.valueOf(jsonObject.getString("gradientOrientation")));
        }
    }

    //Runnable called only if ratio not equal to old ratio
    void setRatio(String ratio, boolean shiftChildren, FunctionParameter delegate) {
        oldLeft = view.getLeft();
        this.shiftChildren = shiftChildren;
        this.delegate = delegate;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.dimensionRatio = ratio;
        view.setLayoutParams(params);
    }

    void shiftChildren(int oldLeft) {
        int deltaLeft = oldLeft - view.getLeft();
        ViewGroup viewGroup = (ViewGroup) view;
        int length = viewGroup.getChildCount();
        for (int i = 0; i < length; i++) {
            View obj = viewGroup.getChildAt(i);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) obj.getLayoutParams();
            int width = params.width;
            params.leftMargin += deltaLeft;
            obj.setLayoutParams(params);
            obj.setLeft(obj.getLeft() + deltaLeft);
            obj.setRight(obj.getLeft() + width);
            obj.setLayoutParams(params);
        }
    }

    void setArtBoardFill(int color) {
        currentFillType = FILL_TYPE_COLOR;
        currentColor = Variables.currentColor = color;
        view.setBackgroundColor(color);
    }

    void setArtBoardFill(int[] gradient, GradientDrawable.Orientation orientation) {
        currentFillType = FILL_TYPE_GRADIENT;
        currentGradient = Variables.currentGradient = gradient;
        currentGradientOrientation = Variables.currentGradientOrientation = orientation;
        GradientDrawable gradientDrawable = Variables.newGradientDrawable(view.getContext(), gradient, orientation, 0);
        view.setBackground(gradientDrawable);
    }

    public String getRatio() {
        return ((ConstraintLayout.LayoutParams) view.getLayoutParams()).dimensionRatio;
    }
}

interface JsonSerializable {
    JSONObject toJSON() throws JSONException;

    void copyAttrFromJSON(JSONObject jsonObject) throws JSONException;
}