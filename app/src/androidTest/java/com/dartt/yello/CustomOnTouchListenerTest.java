package com.dartt.yello;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CustomOnTouchListenerTest {

    private View testView;
    private CustomOnTouchListener listener;
    private boolean dragStarted = false;
    private boolean oneFingerDragged = false;
    private boolean twoFingerDragged = false;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getTargetContext();
        testView = new View(context);
        testView.layout(0, 0, 500, 500);

        listener = new CustomOnTouchListener() {
            @Override
            void onClick(TouchData touchData) {}

            @Override
            void onFingerDown(int fingerCount, TouchData touchData, PinchData pinchData) {}

            @Override
            void onOneFingerDrag(TouchData touchData) {
                oneFingerDragged = true;
            }

            @Override
            void onTwoFingerDrag(PinchData pinchData) {
                twoFingerDragged = true;
            }

            @Override
            void onFingerUp(int fingerCount, TouchData touchData, boolean click) {}

            @Override
            void onDragStart(TouchData touchData) {
                dragStarted = true;
            }
        };
    }

    /**
     * Test Case 1: ACTION_MOVE received when activeTouches does not contain the pointer
     * (e.g. after gesture cancel, parent layout interception, or missed ACTION_DOWN).
     * In current code, this immediately crashes with NullPointerException at line 74.
     */
    @Test
    public void testActionMoveWithoutRegisteredPointerCrashes() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent moveEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_MOVE,
                150f,
                150f,
                0
        );

        try {
            listener.onTouch(testView, moveEvent);
            // If it didn't crash, the listener handled the missing pointer safely
        } catch (NullPointerException e) {
            // This proves the bug!
            System.out.println("REPRODUCED BUG: NullPointerException in onTouch during ACTION_MOVE: " + e);
            throw e; // Re-throw to demonstrate the test failure on unpatched code
        } finally {
            moveEvent.recycle();
        }
    }

    /**
     * Test Case 2: Multi-touch pointer transition (lifting one finger and moving with another)
     */
    @Test
    public void testMultiTouchLiftAndMove() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent.PointerProperties[] pp = new MotionEvent.PointerProperties[2];
        pp[0] = new MotionEvent.PointerProperties();
        pp[0].id = 0;
        pp[1] = new MotionEvent.PointerProperties();
        pp[1].id = 1;

        MotionEvent.PointerCoords[] pc = new MotionEvent.PointerCoords[2];
        pc[0] = new MotionEvent.PointerCoords();
        pc[0].x = 100f; pc[0].y = 100f;
        pc[1] = new MotionEvent.PointerCoords();
        pc[1].x = 200f; pc[1].y = 200f;

        // 1. Finger 0 down
        MotionEvent down0 = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        listener.onTouch(testView, down0);
        down0.recycle();

        // 2. Finger 1 down (Pinch begins)
        int actionPointerDown1 = MotionEvent.ACTION_POINTER_DOWN | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        MotionEvent down1 = MotionEvent.obtain(downTime, eventTime, actionPointerDown1, 2, pp, pc, 0, 0, 1f, 1f, 0, 0, 0, 0);
        listener.onTouch(testView, down1);
        down1.recycle();

        // 3. Finger 0 lifted
        int actionPointerUp0 = MotionEvent.ACTION_POINTER_UP | (0 << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        MotionEvent up0 = MotionEvent.obtain(downTime, eventTime, actionPointerUp0, 2, pp, pc, 0, 0, 1f, 1f, 0, 0, 0, 0);
        listener.onTouch(testView, up0);
        up0.recycle();

        // 4. Finger 1 moves (now only 1 pointer remaining, pointer ID is 1)
        MotionEvent.PointerProperties[] ppSingle = new MotionEvent.PointerProperties[1];
        ppSingle[0] = new MotionEvent.PointerProperties();
        ppSingle[0].id = 1;

        MotionEvent.PointerCoords[] pcSingle = new MotionEvent.PointerCoords[1];
        pcSingle[0] = new MotionEvent.PointerCoords();
        pcSingle[0].x = 250f; pcSingle[0].y = 250f;

        MotionEvent move1 = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 1, ppSingle, pcSingle, 0, 0, 1f, 1f, 0, 0, 0, 0);
        try {
            listener.onTouch(testView, move1);
        } finally {
            move1.recycle();
        }
    }
}
