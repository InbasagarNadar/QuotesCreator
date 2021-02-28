package com.dartt.yello;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class GraphicsHelper {

    public static final float DEFAULT_CONTRAST = 1;
    public static final float DEFAULT_BRIGHTNESS = 0;
    public static final float DEFAULT_SATURATION = 1;

    public static Bitmap changeBitmapContrastBrightnessSaturation(Bitmap bmp, float contrast, float brightness, float saturation) {
        if (bmp == null) {
            return null;
        }
        else {
            boolean setContrastBrightness = contrast != DEFAULT_CONTRAST || brightness != DEFAULT_BRIGHTNESS;
            boolean setSaturation = saturation != DEFAULT_SATURATION;
            if (setContrastBrightness || setSaturation) {
                ColorMatrix cm = null;
                Paint paint = new Paint();
                Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
                Canvas canvas = new Canvas(ret);
                if (setContrastBrightness) {
                    cm = new ColorMatrix(new float[]{contrast, 0, 0, 0,
                            brightness, 0, contrast, 0, 0, brightness, 0, 0, contrast,
                            0, brightness, 0, 0, 0, 1, 0});
                    paint.setColorFilter(new ColorMatrixColorFilter(cm));
                    canvas.drawBitmap(bmp, 0, 0, paint);
                }
                if (setSaturation) {
                    if (cm == null) {
                        cm = new ColorMatrix();
                    }
                    cm.setSaturation(saturation);
                    paint.setColorFilter(new ColorMatrixColorFilter(cm));
                    if (setContrastBrightness) {
                        canvas.drawBitmap(ret, 0, 0, paint);
                    }
                    else {
                        canvas.drawBitmap(bmp, 0, 0, paint);
                    }
                }
                return ret;
            }
            else {
                return bmp;
            }
        }
    }

}
