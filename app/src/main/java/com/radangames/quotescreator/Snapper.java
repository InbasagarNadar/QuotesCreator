package com.radangames.quotescreator;

import android.view.View;
import android.widget.FrameLayout;

public class Snapper {
    boolean snapping = false;
    boolean locked = false;
    float deltaSnap = 0;
    float snapDistance = 2.5f;
    float snapReleaseRequiredDelta = 5;
    public int[] snappingIntervals = new int[]{0, 45, 90, 135, 180, 225, 270, 315, 360};
    int snapIndex = -1;

    float getSnappedValue(float viewRotation, float deltaRotation) {
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


    float getSnappedValue(int[] viewPositions, float deltaPosition) {
        if (snapping) {
            deltaSnap += deltaPosition;
            if (Math.abs(deltaSnap) > snapReleaseRequiredDelta) {
                if (Math.abs(deltaSnap) > snapReleaseRequiredDelta + snapDistance + 1) {
                    deltaSnap = 0;
                    snapIndex = -1;
                    snapping = false;
                }
                locked = false;
                return viewPositions[1] + deltaPosition;
            }
            return viewPositions[1];
        }
        for (int i = 0; i < snappingIntervals.length; i++) {
            if (i == snapIndex) {
                continue;
            }
            int snappingPosition = snappingIntervals[i];
            int viewPosition = viewPositions[i];
            if (Math.abs(viewPosition - snappingPosition) <= snapDistance) {
                snapping = true;
                locked = true;
                snapIndex = i;
                return viewPositions[1] + snappingPosition - viewPosition;
            }
        }
        return viewPositions[1] + deltaPosition;
    }

    static float normalizeRotation(float angle) {
        return (((angle % 360) + 360) % 360);
    }
}

class PositionSnapper {

    Snapper snapperX = new Snapper();
    Snapper snapperY = new Snapper();
    View xAlignmentGuide, yAlignmentGuide;

    /*PositionSnapper(View view, int margin) {
        resetMargins(view, margin);
    }*/

    PositionSnapper(View view, int margin, View xAlignmentGuide, View yAlignmentGuide) {
        this.xAlignmentGuide = xAlignmentGuide;
        this.yAlignmentGuide = yAlignmentGuide;
        resetMargins(view, margin);
    }

    void resetMargins(View view, int margin) {
        int leftMargin = margin;
        int rightMargin = view.getWidth() - margin;
        int topMargin = margin;
        int bottomMargin = view.getHeight() - margin;
        /*int leftMargin = view.getLeft() + margin;
        int rightMargin = view.getLeft() + view.getWidth() - margin;
        int topMargin = view.getTop() + margin;
        int bottomMargin = view.getTop() + view.getHeight() - margin;*/
        //CLR
        snapperX.snappingIntervals = new int[]{(leftMargin + rightMargin) / 2, leftMargin, rightMargin};
        snapperX.snapDistance = 5;
        snapperX.snapReleaseRequiredDelta = 20;
        snapperX.deltaSnap = 0;
        snapperY.snappingIntervals = new int[]{(topMargin + bottomMargin) / 2, topMargin, bottomMargin};
        snapperY.snapDistance = 5;
        snapperY.snapReleaseRequiredDelta = 20;
        snapperY.deltaSnap = 0;
    }

    Vector2D getSnappedPosition(FrameLayout.LayoutParams params, Vector2D deltaPosition) {
        final int viewLeft = params.leftMargin;
        final int viewRight = viewLeft + params.width;
        final int viewCenterX = (viewLeft + viewRight) / 2;
        final int viewTop = params.topMargin;
        final int viewBottom = viewTop + params.height;
        final int viewCenterY = (viewTop + viewBottom) / 2;

        Vector2D viewPosition = new Vector2D();
        viewPosition.x = snapperX.getSnappedValue(new int[]{viewCenterX, viewLeft, viewRight}, deltaPosition.x);
        viewPosition.y = snapperY.getSnappedValue(new int[]{viewCenterY, viewTop, viewBottom}, deltaPosition.y);

        if (xAlignmentGuide != null) {
            if (!snapperX.locked) {
                xAlignmentGuide.setVisibility(View.INVISIBLE);
            }
            else {
                xAlignmentGuide.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams xParams = (FrameLayout.LayoutParams) xAlignmentGuide.getLayoutParams();
                if (snapperX.snapIndex == 0) {
                    xParams.leftMargin = ((int) viewPosition.x + params.width / 2);
                }
                else if (snapperX.snapIndex == 1) {
                    xParams.leftMargin = ((int) viewPosition.x);
                }
                else if (snapperX.snapIndex == 2) {
                    xParams.leftMargin = ((int) viewPosition.x + params.width);
                }
                xAlignmentGuide.setLayoutParams(xParams);
            }

        }
        if (yAlignmentGuide != null) {
            if (!snapperY.locked) {
                yAlignmentGuide.setVisibility(View.INVISIBLE);
            }
            else {
                yAlignmentGuide.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams yParams = (FrameLayout.LayoutParams) yAlignmentGuide.getLayoutParams();
                if (snapperY.snapIndex == 0) {
                    yParams.topMargin = ((int) viewPosition.y + params.height / 2);
                }
                else if (snapperY.snapIndex == 1) {
                    yParams.topMargin = ((int) viewPosition.y);
                }
                else if (snapperY.snapIndex == 2) {
                    yParams.topMargin = ((int) viewPosition.y + params.height);
                }
                yAlignmentGuide.setLayoutParams(yParams);
            }

        }

        return viewPosition;
    }
}


class SnapperUpdated {
    boolean snapping = false;
    boolean locked = false;
    float deltaSnap = 0;
    float snapDistance = 2.5f;
    float snapReleaseRequiredDelta = 5;
    public int[] snappingIntervals = new int[]{0, 45, 90, 135, 180, 225, 270, 315, 360};
    int snapIndex = -1;

    float getSnappedValue(float viewRotation, float deltaRotation) {
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

    int snapIndexI = -1;
    int snapIndexJ = -1;

    int[] getSnappedValue(int[] viewPositions, int[][] indices, int deltaPosition) {
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
/*
        for (int i = 0; i < snappingIntervals.length; i++) {

            for (int j = 0; j < indices.length; j++) {
                if (indices[j] == i && snapIndex != j) {
                    int snappingPosition = snappingIntervals[indices[j]];
                    int viewPosition = viewPositions[j];
                    if (Math.abs(viewPosition - snappingPosition) <= snapDistance) {
                        snapping = true;
                        locked = true;
                        snapIndex = j;
                        return viewPositions[1] + snappingPosition - viewPosition;
                    }
                }
            }
        }*/
/*
        for (int i = 0; i < snappingIntervals.length; i++) {
            if (i == snapIndex) {
                continue;
            }
            int snappingPosition = snappingIntervals[i];
            int viewPosition = viewPositions[i];
            if (Math.abs(viewPosition - snappingPosition) <= snapDistance) {
                snapping = true;
                locked = true;
                snapIndex = i;
                return viewPositions[1] + snappingPosition - viewPosition;
            }
        }*/
        return new int[]{viewPositions[1] + deltaPosition, -100};
    }

    static float normalizeRotation(float angle) {
        return (((angle % 360) + 360) % 360);
    }
}

class PositionSnapperUpdated {

    SnapperUpdated snapperX = new SnapperUpdated();
    SnapperUpdated snapperY = new SnapperUpdated();

    /*PositionSnapper(View view, int margin) {
        resetMargins(view, margin);
    }*/

    PositionSnapperUpdated(View canvas, int margin) {
        //  this.xAlignmentGuide = xAlignmentGuide;
//        this.yAlignmentGuide = yAlignmentGuide;
        resetMargins(canvas, margin);
    }

    void resetMargins(View view, int margin) {
        int leftMargin = margin;
        int rightMargin = view.getWidth() - margin;
        int topMargin = margin;
        int bottomMargin = view.getHeight() - margin;
        /*int leftMargin = view.getLeft() + margin;
        int rightMargin = view.getLeft() + view.getWidth() - margin;
        int topMargin = view.getTop() + margin;
        int bottomMargin = view.getTop() + view.getHeight() - margin;*/
        //CLR
        snapperX.snappingIntervals = new int[]{(leftMargin + rightMargin) / 2, leftMargin, rightMargin, 0, view.getMeasuredWidth()};
        //Log.e("Debug", Arrays.toString(snapperX.snappingIntervals));
        snapperX.snapDistance = 5;
        snapperX.snapReleaseRequiredDelta = 20;
        snapperX.deltaSnap = 0;
        snapperY.snappingIntervals = new int[]{(topMargin + bottomMargin) / 2, topMargin, bottomMargin, 0, view.getMeasuredHeight()};
        snapperY.snapDistance = 5;
        snapperY.snapReleaseRequiredDelta = 20;
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

        //viewPosition.y = snapperY.getSnappedValue(new int[]{viewCenterY, viewTop, viewBottom}, new int[][]{{0}, {1, 3}, {2, 4}}, (int) deltaPosition.y);

/*
        if (xAlignmentGuide != null) {
            if (!snapperX.locked) {
                result.snapPosition.x = -100;
            }
            else {
                xAlignmentGuide.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams xParams = (FrameLayout.LayoutParams) xAlignmentGuide.getLayoutParams();
                if (snapperX.snapIndex == 0) {
                    result.snapPosition.x = (viewPosition.x + params.width / 2f);
                }
                else if (snapperX.snapIndex == 1) {
                    result.snapPosition.x = (viewPosition.x);
                }
                else if (snapperX.snapIndex == 2) {
                    xParams.leftMargin = ((int) viewPosition.x + params.width);
                }
                xAlignmentGuide.setLayoutParams(xParams);
            }

        }
        if (yAlignmentGuide != null) {
            if (!snapperY.locked) {
                yAlignmentGuide.setVisibility(View.INVISIBLE);
            }
            else {
                yAlignmentGuide.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams yParams = (FrameLayout.LayoutParams) yAlignmentGuide.getLayoutParams();
                if (snapperY.snapIndex == 0) {
                    yParams.topMargin = ((int) viewPosition.y + params.height / 2);
                }
                else if (snapperY.snapIndex == 1) {
                    yParams.topMargin = ((int) viewPosition.y);
                }
                else if (snapperY.snapIndex == 2) {
                    yParams.topMargin = ((int) viewPosition.y + params.height);
                }
                yAlignmentGuide.setLayoutParams(yParams);
            }

        }
*/
        return result;
    }

    static class SnapResult {
        Vector2D viewPosition;
        Vector2D snapPosition;
    }
}

