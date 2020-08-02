package com.radangames.quotescreator;

public class PinchData {
    public TouchData touch0;
    public TouchData touch1;

    public PinchData(TouchData touchData0, TouchData touchData1) {
        touch0 = touchData0;
        touch1 = touchData1;
    }

    public float getCurrentScale() {
        return Vector2D.Distance(touch0.currentPosition, touch1.currentPosition) / Vector2D.Distance(touch0.startPosition, touch1.startPosition);
    }

    public float getLastScale() {
        return Vector2D.Distance(touch0.lastPosition, touch1.lastPosition) / Vector2D.Distance(touch0.startPosition, touch1.startPosition);
    }

    public float getCurrentRotation() {
        Vector2D slope = touch1.currentPosition.minus(touch0.currentPosition);
        return (float) Math.toDegrees(Math.atan2(slope.y, slope.x));
    }

    public float getLastRotation() {
        Vector2D slope = touch1.lastPosition.minus(touch0.lastPosition);
        return (float) Math.toDegrees(Math.atan2(slope.y, slope.x));
    }

    public float getDeltaRotation() {
        return getCurrentRotation() - getLastRotation();
    }

    public Vector2D getCurrentMidPoint() {
        return touch0.currentPosition.plus(touch1.currentPosition).scalarDivide(2);
    }

    public Vector2D getLastMidPoint() {
        return touch0.lastPosition.plus(touch1.lastPosition).scalarDivide(2);
    }

    public Vector2D getDeltaMidpoint() {
        return getCurrentMidPoint().minus(getLastMidPoint());
    }
}
