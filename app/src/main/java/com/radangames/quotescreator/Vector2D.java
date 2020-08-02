package com.radangames.quotescreator;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

class Vector2D {
    float x, y;

    Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    Vector2D() {
    }

    static Vector2D getPositionRelativeToParent(View parent, MotionEvent motionEvent) {
        return new Vector2D((int) motionEvent.getRawX() - parent.getLeft(), (int) motionEvent.getRawY() - parent.getTop() - EditorActivity.statusBarHeight);
    }

    Vector2D minus(Vector2D rhs) {
        return new Vector2D(this.x - rhs.x, this.y - rhs.y);
    }

    Vector2D plus(Vector2D rhs) {
        return new Vector2D(this.x + rhs.x, this.y + rhs.y);
    }

    Vector2D rotate(float angle) {
        Vector2D result = new Vector2D();
        double radians = Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        result.x = cos * x - sin * y;
        result.y = cos * y + sin * x;
        return result;
    }

    public Vector2D(View view, MotionEvent motionEvent, int pointerIndex) {
        final int[] location = {0, 0};
        view.getLocationOnScreen(location);
        this.x = (int) (location[0] + motionEvent.getX(pointerIndex));
        this.y = (int) (location[1] + motionEvent.getY(pointerIndex));
        /*this.x = (int)(motionEvent.getRawX(pointerIndex));
        this.y = (int)(motionEvent.getRawY(pointerIndex));*/
    }

    @Override
    @NonNull
    public String toString() {
        return x + " " + y;
    }

    static float Distance(Vector2D A, Vector2D B) {
        return (float) Math.sqrt(Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2));
    }

    public Vector2D scalarDivide(float divisor){
        return new Vector2D(x/divisor, y/divisor);
    }
}