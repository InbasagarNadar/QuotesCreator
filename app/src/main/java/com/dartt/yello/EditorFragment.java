package com.dartt.yello;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class EditorFragment extends Fragment {

    boolean init;
    CreateBackgroundActivity activity;
    FrameLayout frameArtBoard;
    EditorTouchListener touchListener;
    ImageViewContainer backgroundViewContainer;
    ArtBoardViewContainer artBoardViewContainer;
    ArrayList<ViewContainer> viewContainers = new ArrayList<>();

    private FunctionParameter canvasLayoutChangeAction;
    private final FunctionParameter onBackgroundChanged = new FunctionParameter() {
        @Override
        public void execute() {
            activity.undo.afterEdit(backgroundViewContainer);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_editor, container, false);
        activity = (CreateBackgroundActivity) getActivity();
        frameArtBoard = fragment.findViewById(R.id.frame_canvas);
        artBoardViewContainer = new ArtBoardViewContainer(frameArtBoard);
        backgroundViewContainer = new ImageViewContainer(this);
        frameArtBoard.addView(backgroundViewContainer.view);
        backgroundViewContainer.view.setOnTouchListener(new ViewOnTouchListener(this) {
            @Override
            ViewContainer getViewContainer() {
                return backgroundViewContainer;
            }
        });
        View alignmentGuideX = fragment.findViewById(R.id.alignment_guide_x);
        View alignmentGuideY = fragment.findViewById(R.id.alignment_guide_y);

        touchListener = new EditorTouchListener(frameArtBoard, alignmentGuideX, alignmentGuideY);
        frameArtBoard.setOnTouchListener(touchListener);

        final ViewTreeObserver viewTreeObserver = frameArtBoard.getViewTreeObserver();
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
                touchListener.positionSnapper.resetMargins(frameArtBoard, 75);
                if (savedInstanceState != null) {
                    fromJSON(savedInstanceState.getString("json"));
                }
                else {
                    /* Checking if image exists in intent, else setting default image*/
                    Intent intent = activity.getIntent();
                    String action = intent.getAction();
                    String type = intent.getType();
                    if (Intent.ACTION_SEND.equals(action) && type != null) {
                        if (type.startsWith("image/")) {
                            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                            if (imageUri != null) {
                                backgroundViewContainer.setImage(imageUri, null, true);
                            }
                        }
                    }
                    if (backgroundViewContainer.currentBackgroundType == ImageViewContainer.BACKGROUND_TYPE_NULL) {
                        // Uncomment this to have default image on startup
                        //backgroundViewContainer.setImage(Variables.BackgroundImages[0], null, true);
                    }
                    else {
                        backgroundViewContainer.fillParent();
                    }

                    final TextViewContainer tv = new TextViewContainer(EditorFragment.this);
                    tv.textContentOriginal = new SpannableStringBuilder(Variables.randomQuotes[new Random().nextInt(Variables.randomQuotes.length)]);
                    tv.textContentOriginal.setSpan(new ForegroundColorSpan(0xFF000000), 0, tv.textContentOriginal.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    tv.getView().setText(tv.textContentOriginal);
                    addElement(tv);
                    tv.setDimension(new Vector2D(frameArtBoard.getWidth(), frameArtBoard.getHeight()), false);
                    tv.getView().post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(tv.getView());
                        }
                    });
                }
            }

        });

        return fragment;
    }

    Bitmap getBitmap() {
        String dimensionRatio = ((ConstraintLayout.LayoutParams) frameArtBoard.getLayoutParams()).dimensionRatio;
        String[] dim = dimensionRatio.split(":");
        float imageRatio = Float.parseFloat(dim[0]) / Float.parseFloat(dim[1]);
        int targetWidth = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("export_resolution", "1080"));
        int targetHeight = (int) (targetWidth / imageRatio);
        float scale = targetWidth / ((float) frameArtBoard.getWidth());

        Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(scale, scale);
        frameArtBoard.draw(canvas);
        return bitmap;
    }

    void setArtBoardFill(int color) {
        artBoardViewContainer.setArtBoardFill(color);
    }

    void setArtBoardFill(int[] gradient, GradientDrawable.Orientation orientation) {
        artBoardViewContainer.setArtBoardFill(gradient, orientation);
    }

    boolean isArtBoardVisible() {
        if (backgroundViewContainer.currentBackgroundType == ImageViewContainer.BACKGROUND_TYPE_NULL) {
            return true;
        }
        else {
            //Log.d("ArtBoard", artBoardViewContainer.width + " "+ artBoardViewContainer.height);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) backgroundViewContainer.getView().getLayoutParams();
            float pivotX = layoutParams.width / 2f + layoutParams.leftMargin;
            float pivotY = layoutParams.height / 2f + layoutParams.topMargin;
            double radians = Math.toRadians(backgroundViewContainer.getView().getRotation());
            double sinTheta = Math.sin(radians);
            double cosTheta = Math.cos(radians);
            Vector2D a = new Vector2D(
                    (float) (cosTheta * (layoutParams.leftMargin - pivotX) - sinTheta * (layoutParams.topMargin - pivotY) + pivotX),
                    (float) (sinTheta * (layoutParams.leftMargin - pivotX) + cosTheta * (layoutParams.topMargin - pivotY) + pivotY)
            );
            Vector2D b = new Vector2D(
                    (float) (cosTheta * (layoutParams.leftMargin - pivotX + layoutParams.width) - sinTheta * (layoutParams.topMargin - pivotY) + pivotX),
                    (float) (sinTheta * (layoutParams.leftMargin - pivotX + layoutParams.width) + cosTheta * (layoutParams.topMargin - pivotY) + pivotY)
            );
            Vector2D c = new Vector2D(
                    (float) (cosTheta * (layoutParams.leftMargin - pivotX + layoutParams.width) - sinTheta * (layoutParams.topMargin - pivotY + layoutParams.height) + pivotX),
                    (float) (sinTheta * (layoutParams.leftMargin - pivotX + layoutParams.width) + cosTheta * (layoutParams.topMargin - pivotY + layoutParams.height) + pivotY)
            );
            Vector2D d = new Vector2D(
                    (float) (cosTheta * (layoutParams.leftMargin - pivotX) - sinTheta * (layoutParams.topMargin - pivotY + layoutParams.height) + pivotX),
                    (float) (sinTheta * (layoutParams.leftMargin - pivotX) + cosTheta * (layoutParams.topMargin - pivotY + layoutParams.height) + pivotY)
            );

            Vector2D[] frameCoordinates = new Vector2D[]{
                    new Vector2D(0, 0),
                    new Vector2D(frameArtBoard.getWidth(), 0),
                    new Vector2D(frameArtBoard.getWidth(), frameArtBoard.getHeight()),
                    new Vector2D(0, frameArtBoard.getHeight())
            };

            float rectArea = Math.abs((a.x * b.y - a.y * b.x) + (b.x * c.y - b.y * c.x) + (c.x * d.y - c.y * d.x) + (d.x * a.y - d.y * a.x)) / 2f;

            for (Vector2D frameCoordinate : frameCoordinates) {
                float pointArea = 0;
                pointArea += Math.abs(frameCoordinate.x * (a.y - b.y) + a.x * (b.y - frameCoordinate.y) + b.x * (frameCoordinate.y - a.y)) / 2f;
                pointArea += Math.abs(frameCoordinate.x * (b.y - c.y) + b.x * (c.y - frameCoordinate.y) + c.x * (frameCoordinate.y - b.y)) / 2f;
                pointArea += Math.abs(frameCoordinate.x * (c.y - d.y) + c.x * (d.y - frameCoordinate.y) + d.x * (frameCoordinate.y - c.y)) / 2f;
                pointArea += Math.abs(frameCoordinate.x * (d.y - a.y) + d.x * (a.y - frameCoordinate.y) + a.x * (frameCoordinate.y - d.y)) / 2f;
                if (pointArea > rectArea + 5) {
                    return true;
                }
            }
            return false;
            /*boolean leftSideVisible = layoutParams.leftMargin > 0;
            boolean rightSideVisible = (layoutParams.leftMargin + layoutParams.width) < artBoardViewContainer.view.getWidth();
            boolean topSideVisible = layoutParams.topMargin > 0;
            boolean bottomSideVisible = (layoutParams.topMargin + layoutParams.height) < artBoardViewContainer.view.getHeight();
            return leftSideVisible || rightSideVisible || topSideVisible || bottomSideVisible;*/
        }
    }

    void removeBackground() {
        activity.undo.beforeEdit(backgroundViewContainer);
        backgroundViewContainer.removeImage();
        activity.undo.afterEdit(backgroundViewContainer);
    }

    void setBackground(final Uri imageUri) {
        activity.undo.beforeEdit(backgroundViewContainer);
        backgroundViewContainer.resetFilter();
        backgroundViewContainer.setImage(imageUri, onBackgroundChanged, true);
    }

    void setBackground(final int resourceId) {
        activity.undo.beforeEdit(backgroundViewContainer);
        backgroundViewContainer.resetFilter();
        backgroundViewContainer.setImage(resourceId, onBackgroundChanged, true);
    }

    void setBackground(final String url) {
        activity.undo.beforeEdit(backgroundViewContainer);
        backgroundViewContainer.resetFilter();
        backgroundViewContainer.setImage(url, onBackgroundChanged, true);
    }

    void setRatio(final String ratio) {
        final String oldRatio = artBoardViewContainer.getRatio();
        artBoardViewContainer.setRatio(ratio, true, new FunctionParameter() {
            @Override
            public void execute() {
                touchListener.positionSnapper.resetMargins(frameArtBoard, 75);
                activity.undo.addState(new RatioUndoable(oldRatio, ratio));
            }
        });
    }

    void addElement(Drawable resource, int resourceId) {
        StickerViewContainer stickerViewContainer = new StickerViewContainer(this, resource, resourceId);
        frameArtBoard.addView(stickerViewContainer.view);
        stickerViewContainer.alignToCenter();
        stickerViewContainer.resetScale();
        viewContainers.add(stickerViewContainer);
        activity.undo.onElementAdded(stickerViewContainer);
    }

    //Sticker or ImageDrawable
    public void addElement(final int resourceId) {
        Glide.with(activity).load(resourceId).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                addElement(resource, resourceId);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    public void addElement(ViewContainer viewContainer) {
        frameArtBoard.addView(viewContainer.view);
        viewContainer.alignToCenter();
        viewContainers.add(viewContainer);
    }

    void onElementClicked(ViewContainer viewContainer) {
        activity.onElementClicked(viewContainer);
    }

    public void removeElement(ViewContainer viewContainer) {
        viewContainer.view.setVisibility(View.GONE);
    }

    JSONObject toJSON() throws JSONException {
        JSONArray elements = new JSONArray();
        for (int i = 0; i < viewContainers.size(); i++) {
            if (viewContainers.get(i).view.getVisibility() == View.VISIBLE) {
                elements.put(viewContainers.get(i).toJSON());
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("elements", elements);
        jsonObject.put("background", backgroundViewContainer.toJSON());
        jsonObject.put("artBoard", artBoardViewContainer.toJSON());
        return jsonObject;
    }

    void fromJSON(String json) {
        Log.e("EditorFragment", backgroundViewContainer.currentBackgroundType + "");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray elements = jsonObject.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                ViewContainer viewContainer;
                if (elements.getJSONObject(i).getString("type").equals("sticker")) {
                    viewContainer = new StickerViewContainer(this);
                }
                else {
                    viewContainer = new TextViewContainer(this);
                }
                frameArtBoard.addView(viewContainer.view);
                viewContainers.add(viewContainer);
                viewContainer.copyAttrFromJSON(elements.getJSONObject(i));
            }
            //Checking currentBackgroundType to handle low memory between activity result
            if (backgroundViewContainer.currentBackgroundType == ImageViewContainer.BACKGROUND_TYPE_NULL) {
                JSONObject background = jsonObject.getJSONObject("background");
                backgroundViewContainer.copyAttrFromJSON(background);
            }
            else {
                // To fix 0dp height width I guess on activity result
                backgroundViewContainer.fillParent();
            }
            artBoardViewContainer.copyAttrFromJSON(jsonObject.getJSONObject("artBoard"));
            //Toast.makeText(activity, "From JSON" + json.toString(), Toast.LENGTH_LONG).show();
            Log.e("EditorFragment", "Restored JSON : " + json);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EditorFragment", "Failed to read save state. JSON exception" + e.toString());
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            String jsonString = toJSON().toString();
            outState.putString("json", jsonString);
            Log.e("EditorFragment", "SaveInstanceState" + jsonString);
        } catch (Exception e) {
            Log.e("EditorFragment", "Failed to save state. JSON exception: " + e.toString());
        }
    }


    public void setContrastBrightnessSaturation(float contrast, float brightness, float saturation) {
        backgroundViewContainer.setContrastBrightnessSaturation(contrast, brightness, saturation);
    }

    /*public void setSaturation(float saturation) {
        backgroundViewContainer.setSaturation(saturation);
    }*/

    class RatioUndoable implements Undo.Undoable {
        final String oldDimensionRatio;
        final String newDimensionRatio;

        public RatioUndoable(String oldDimensionRatio, String newDimensionRatio) {
            this.oldDimensionRatio = oldDimensionRatio;
            this.newDimensionRatio = newDimensionRatio;
        }

        @Override
        public void undo() {
            artBoardViewContainer.setRatio(oldDimensionRatio, true, new FunctionParameter() {
                @Override
                public void execute() {
                    touchListener.positionSnapper.resetMargins(frameArtBoard, 75);
                }
            });
        }

        @Override
        public void redo() {
            artBoardViewContainer.setRatio(newDimensionRatio, true, new FunctionParameter() {
                @Override
                public void execute() {
                    touchListener.positionSnapper.resetMargins(frameArtBoard, 75);
                }
            });
        }
    }

    //Helper class to set activeViewContainer
    static abstract class ViewOnTouchListener implements View.OnTouchListener {

        EditorFragment editorFragment;

        ViewOnTouchListener(EditorFragment editorFragment) {
            this.editorFragment = editorFragment;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (editorFragment.touchListener.activeViewContainer == null) {
                        editorFragment.touchListener.activeViewContainer = getViewContainer();
                    }
            }
            return false;
        }

        abstract ViewContainer getViewContainer();
    }

    // Handles Touch controls and Alignment
    class EditorTouchListener extends CustomOnTouchListener {
        public float viewInitialScale;
        final Vector2D invisible = new Vector2D(-1000, -1000);
        ViewContainer activeViewContainer;
        View xAlignmentGuide, yAlignmentGuide;
        PositionSnapper positionSnapper;
        RotationSnapper rotationSnapper;

        EditorTouchListener(ViewGroup canvas, View xAlignmentGuide, View yAlignmentGuide) {
            this.xAlignmentGuide = xAlignmentGuide;
            this.yAlignmentGuide = yAlignmentGuide;
            positionSnapper = new PositionSnapper(canvas, 75);
            rotationSnapper = new RotationSnapper();
        }

        @Override
        void onClick(TouchData touchData) {
            if (activeViewContainer != null) {
                onElementClicked(activeViewContainer);
            }
        }

        @Override
        void onFingerDown(int fingerCount, TouchData touchData, @Nullable PinchData pinchData) {
            if (fingerCount == 1) {
                if (activeViewContainer != null) {
                    activity.undo.beforeEdit(activeViewContainer);
                    ScaleAnimation animation = new ScaleAnimation(1.01f, 1, 1.01f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration(150);
                    //animation.setFillAfter(true);
                    activeViewContainer.view.startAnimation(animation);
                    //activeViewContainer.view.setScaleX(.97f);
                    //activeViewContainer.view.setScaleY(.97f);
                }
            }
            else if (fingerCount == 2) {
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
                PositionSnapper.SnapResult result = positionSnapper.getSnappedPosition(params, touchData.getDeltaPosition());
                activeViewContainer.setPosition(result.viewPosition);
                setGuidePosition(result.snapPosition);
            }
        }

        @Override
        void onTwoFingerDrag(PinchData pinchData) {
            if (activeViewContainer != null) {
                activeViewContainer.move(pinchData.getDeltaMidpoint());
                activeViewContainer.view.setRotation(rotationSnapper.getSnappedRotation(activeViewContainer.view.getRotation(), pinchData.getDeltaRotation()));
                activeViewContainer.setScale(viewInitialScale * pinchData.getCurrentScale());
            }
        }

        @Override
        void onFingerUp(int fingerCount, TouchData touchData, boolean click) {

            if (fingerCount == 1) {
                if (activeViewContainer != null) {
                    activity.undo.afterEdit(activeViewContainer);
                    activeViewContainer = null;
                /*activeViewContainer.view.setScaleX(1f);
                activeViewContainer.view.setScaleY(1f);
                */
                }
                positionSnapper.reset();
            }
            else if (fingerCount == 2) {
                rotationSnapper.reset();
            }
            setGuidePosition(invisible);

        }

        @Override
        void onDragStart(TouchData touchData) {
            /*if (activeViewContainer != null) {
                activeViewContainer.view.setScaleX(1f);
                activeViewContainer.view.setScaleY(1f);
            }*/
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

