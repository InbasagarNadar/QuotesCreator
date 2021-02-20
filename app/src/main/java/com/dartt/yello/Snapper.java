package com.dartt.yello;

import android.view.View;
import android.widget.FrameLayout;

class Snapper {
    boolean snapping = false;
    boolean locked = false;
    float deltaSnap = 0;
    public int[] snappingIntervals;

    int snapIndexI = -1;
    int snapIndexJ = -1;

    int[] getSnappedValue(int[] viewPositions, int[][] indices, int deltaPosition) {
        final float snapDistance = 5;
        final float snapReleaseRequiredDelta = 20;
        if (snapping) {
            deltaSnap += deltaPosition;
            if (Math.abs(deltaSnap) > snapReleaseRequiredDelta) {
                if (Math.abs(deltaSnap) > snapReleaseRequiredDelta + snapDistance + 1) {
                    deltaSnap = 0;
                    snapIndexI = snapIndexJ = -1;
                    snapping = false;
                }
                locked = false;
                return new int[]{viewPositions[1] + deltaPosition, -100};
            }
            return new int[]{viewPositions[1], viewPositions[snapIndexI]};
        }

        for (int i = 0; i < indices.length; i++) {
            int viewPosition = viewPositions[i];
            for (int j = 0; j < indices[i].length; j++) {
                if (snapIndexJ == j && snapIndexI == i) {
                    continue;
                }
                int snappingPosition = snappingIntervals[indices[i][j]];
                if (Math.abs(viewPosition - snappingPosition) <= snapDistance) {
                    snapping = true;
                    locked = true;
                    snapIndexI = i;
                    snapIndexJ = j;
                    //Log.e("index", viewPosition + " " + snappingPosition + " " + i + " " + j + " " + indices[i][j]);
                    return new int[]{viewPositions[1] + snappingPosition - viewPosition, snappingPosition};
                }
            }
        }
        return new int[]{viewPositions[1] + deltaPosition, -100};
    }


    public void reset() {
        snapIndexI = -1;
        snapIndexJ = -1;
        deltaSnap = 0;
        snapping = false;
        locked = false;
    }
}

class PositionSnapper {

    Snapper snapperX = new Snapper();
    Snapper snapperY = new Snapper();

    PositionSnapper(View canvas, int margin) {
        resetMargins(canvas, margin);
    }

    void resetMargins(View view, int margin) {
        int leftMargin = margin;
        int rightMargin = view.getWidth() - margin;
        int topMargin = margin;
        int bottomMargin = view.getHeight() - margin;
        //CLR
        snapperX.snappingIntervals = new int[]{(leftMargin + rightMargin) / 2, leftMargin, rightMargin, 0, view.getMeasuredWidth()};
        //Log.e("Debug", Arrays.toString(snapperX.snappingIntervals));
        snapperX.deltaSnap = 0;
        snapperY.snappingIntervals = new int[]{(topMargin + bottomMargin) / 2, topMargin, bottomMargin, 0, view.getMeasuredHeight()};
        snapperY.deltaSnap = 0;
    }

    SnapResult getSnappedPosition(FrameLayout.LayoutParams params, Vector2D deltaPosition) {
        final int viewLeft = params.leftMargin;
        final int viewRight = viewLeft + params.width;
        final int viewCenterX = (viewLeft + viewRight) / 2;
        final int viewTop = params.topMargin;
        final int viewBottom = viewTop + params.height;
        final int viewCenterY = (viewTop + viewBottom) / 2;

        SnapResult result = new SnapResult();

        int[] outX = snapperX.getSnappedValue(new int[]{viewCenterX, viewLeft, viewRight}, new int[][]{{0}, {1, 3}, {2, 4}}, (int) deltaPosition.x);
        int[] outY = snapperY.getSnappedValue(new int[]{viewCenterY, viewTop, viewBottom}, new int[][]{{0}, {1, 3}, {2, 4}}, (int) deltaPosition.y);
        result.viewPosition = new Vector2D(outX[0], outY[0]);
        result.snapPosition = new Vector2D(outX[1], outY[1]);
        return result;
    }

    public void reset() {
        snapperX.reset();
        snapperY.reset();
    }

    static class SnapResult {
        Vector2D viewPosition;
        Vector2D snapPosition;
    }
}

class RotationSnapper {
    final float snapDistance = 2.5f;
    final float snapReleaseRequiredDelta = 5;
    final int[] snappingIntervals = {0, 45, 90, 135, 180, 225, 270, 315, 360};
    boolean snapping = false;
    boolean locked = false;
    float deltaSnap = 0;
    int snapIndex = -1;

    float getSnappedRotation(float viewRotation, float deltaRotation) {
        viewRotation = normalizeRotation(viewRotation);
        if (snapping) {
            deltaSnap += deltaRotation;
            if (Math.abs(deltaSnap) > snapReleaseRequiredDelta || !locked) {
                if (Math.abs(deltaSnap) > snapReleaseRequiredDelta + snapDistance + 1) {
                    deltaSnap = 0;
                    snapIndex = -1;
                    snapping = false;
                }
                locked = false;
                return viewRotation + deltaRotation;
            }
            return snappingIntervals[snapIndex];
        }
        for (int i = 0; i < snappingIntervals.length; i++) {
            if (i == snapIndex) {
                continue;
            }
            int snappingAngle = snappingIntervals[i];
            if (Math.abs(viewRotation - snappingAngle) <= snapDistance) {
                snapping = true;
                locked = true;
                snapIndex = i;
                return snappingAngle;
            }
        }
        return viewRotation + deltaRotation;
    }

    static float normalizeRotation(float angle) {
        return (((angle % 360) + 360) % 360);
    }

    public void reset() {
        deltaSnap = 0;
        snapIndex = -1;
        snapping = false;
        locked = false;
    }
}
