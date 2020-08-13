package com.dartt.yello;

public class TouchData {
    public int touchId;
    public Vector2D startPosition;
    public Vector2D lastPosition;
    public Vector2D currentPosition;

    public Vector2D getDeltaPosition() {
        return currentPosition.minus(lastPosition);
    }

    TouchData(int touchId, Vector2D position) {
        this.touchId = touchId;
        startPosition = lastPosition = currentPosition = position;
    }

    /**
     * sets lastPosition to currentPosition and currentPosition to newPosition
     */
    public void setCurrentPosition(Vector2D newPosition) {
        lastPosition = currentPosition;
        currentPosition = newPosition;
    }

    public void resetPositionToCurrentPosition() {
        startPosition = lastPosition = currentPosition;
    }
}
