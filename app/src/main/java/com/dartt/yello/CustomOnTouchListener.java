package com.dartt.yello;

import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class CustomOnTouchListener implements View.OnTouchListener {
    SparseArray<TouchData> activeTouches = new SparseArray<>();
    boolean click = true;
    final float minDrag = 10;
    float dragThreshold = 0;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int currentPointerIndex = motionEvent.getActionIndex();
        int currentPointerId = motionEvent.getPointerId(currentPointerIndex);
        int pointerCount = motionEvent.getPointerCount();

        switch (motionEvent.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                TouchData currentTouchData = new TouchData(currentPointerId, new Vector2D(view, motionEvent, currentPointerIndex));
                activeTouches.put(currentPointerId, currentTouchData);
                click = true;

                switch (pointerCount) {
                    case 1:
                        dragThreshold = 0;
                        onFingerDown(pointerCount, currentTouchData, null);
                        break;
                    case 2:
                        dragThreshold = minDrag + 1;
                        TouchData touchData0 = activeTouches.get(motionEvent.getPointerId(0));
                        TouchData touchData1 = activeTouches.get(motionEvent.getPointerId(1));
                        touchData0.resetPositionToCurrentPosition();
                        touchData1.resetPositionToCurrentPosition();
                        PinchData pinchData = new PinchData(touchData0, touchData1);
                        onFingerDown(pointerCount, currentTouchData, pinchData);
                        click = false;
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                /*
                dragThreshold++;
                if (dragThreshold > minDrag ) {
                    int pointerId0 = motionEvent.getPointerId(0);
                    TouchData touchData0 = activeTouches.get(pointerId0);
                    touchData0.setCurrentPosition(new Vector2D(view, motionEvent, 0));
                    if(click){
                        //To reset drag, caused because of ignoring drag withing drag threshold
                        touchData0.setCurrentPosition(new Vector2D(view, motionEvent, 0));
                        onDragStart(touchData0);
                    }
                    click = false;
                    switch (motionEvent.getPointerCount()) {
                        case 1:
                            onOneFingerDrag(touchData0);
                            break;
                        case 2:
                            int pointerId1 = motionEvent.getPointerId(1);
                            TouchData touchData1 = activeTouches.get(pointerId1);
                            touchData1.setCurrentPosition(new Vector2D(view, motionEvent, 1));
                            onTwoFingerDrag(new PinchData(touchData0, touchData1));
                            break;
                    }
                }
                */
                int pointerId0 = motionEvent.getPointerId(0);
                TouchData touchData0 = activeTouches.get(pointerId0);
                touchData0.setCurrentPosition(new Vector2D(view, motionEvent, 0));
                dragThreshold += Math.abs(touchData0.getDeltaPosition().x) + Math.abs(touchData0.getDeltaPosition().y);
                if (dragThreshold > minDrag) {
                    if (click) {
                        //To reset drag, caused because of ignoring drag withing drag threshold
                        //touchData0.setCurrentPosition(new Vector2D(view, motionEvent, 0));
                        onDragStart(touchData0);
                        click = false;
                    }
                    switch (motionEvent.getPointerCount()) {
                        case 1:
                            onOneFingerDrag(touchData0);
                            break;
                        case 2:
                            int pointerId1 = motionEvent.getPointerId(1);
                            TouchData touchData1 = activeTouches.get(pointerId1);
                            touchData1.setCurrentPosition(new Vector2D(view, motionEvent, 1));
                            onTwoFingerDrag(new PinchData(touchData0, touchData1));
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pointerCount == 1) {
                    if (click) {
                        onClick(activeTouches.get(currentPointerId));
                    }
                }
                onFingerUp(pointerCount, activeTouches.get(currentPointerId), click);
                activeTouches.remove(currentPointerId);
                break;
        }
        return true;
    }

    abstract void onClick(TouchData touchData);

    abstract void onFingerDown(int fingerCount, TouchData touchData, @Nullable PinchData pinchData);

    abstract void onOneFingerDrag(TouchData touchData);

    abstract void onTwoFingerDrag(PinchData pinchData);

    abstract void onFingerUp(int fingerCount, TouchData touchData, boolean click);

    abstract void onDragStart(TouchData touchData);
}
