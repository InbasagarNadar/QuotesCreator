package com.dartt.yello;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

public class Variables {
    public static int[][] Gradients = {
            {0xFFFDC05B, 0xFFDD2D75, 0xFFA734B4,},
            {0xFFFF6E7F, 0xFFBFE9FF},
            {0xFFE44D87, 0xFF5FC3E4},
            {0xFFEF3B36, 0xFFFFFFFF},
            {0XFFc6ffdd, 0xFFfbd786, 0xFFf7797d},
            {0xFFABBAAB, 0xFFFFFFFF},
            {0xFFFFFFFF, 0xFF000000},
            {0xFF005AA7, 0xFFFFFDE4},
            {0xFF014871, 0xFFD7EDE2},
            {0xFFCC95C0, 0xFFDBD4B4, 0xFF7AA1D2},
    };
    public static final Orientation[] GradientsOrientation = {
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
            Orientation.TL_BR,
    };
    public static int[] currentGradient = Gradients[0];
    public static Orientation currentGradientOrientation = GradientsOrientation[0];

    public static final int[] Colors = {
            0xFFF7E5DE,
            0xFFFFBF00,
            0xFF355C7D,
            0xFF6C5B7B,
            0xFFC06C84,
            0xFFF67280,
            0xFFF8B195,
            0xFFE8175D,
            0xFFA8E6CE,
            0xFFFFD3B6,
            0xFFDCEDC1,
            0xFF22C5CC,
            0xFFFF69B4,
            0xFFF4951B
    };
    public static int currentColor = Colors[0];

    public static final int[] BackgroundImages = {
            R.drawable.background_00,
            R.drawable.background_01,
            R.drawable.background_02,
            R.drawable.background_03,
            R.drawable.background_04,
            R.drawable.background_05,
            R.drawable.background_06,
            R.drawable.background_07,
            R.drawable.background_08,
            R.drawable.background_09,
            R.drawable.background_10,
            R.drawable.background_11,
            R.drawable.background_12,
            R.drawable.background_13,
            R.drawable.background_14,
            R.drawable.background_15,
            R.drawable.background_16,
            R.drawable.background_17,
            R.drawable.background_18,
            R.drawable.background_19,
            R.drawable.background_20,
    };

    public static float pixelToDensity(Context context, int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int densityToPixel(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int[] stickerResourceId = {
            R.drawable.sticker_01_quote,
            R.drawable.sticker_02_heart,
            R.drawable.sticker_03_hash,
            R.drawable.sticker_04_spring,
            R.drawable.sticker_05_diamond,
            R.drawable.sticker_06_star,
            R.drawable.sticker_07_chill,
            R.drawable.sticker_08_comet,
            R.drawable.sticker_09_rainbow,
            R.drawable.sticker_10_pixel_heart,
            R.drawable.sticker_11_asterik,
            R.drawable.sticker_12_quote,
            R.drawable.sticker_13_quote,
            R.drawable.sticker_14_quote,
            R.drawable.sticker_15_quote,
            R.drawable.sticker_16_ligntning,
            R.drawable.sticker_17_pizza,
            R.drawable.sticker_18_quote,
            R.drawable.sticker_19_relax,
            R.drawable.sticker_20_crown,
            R.drawable.sticker_21_mental,
            R.drawable.sticker_22_smiley,
            R.drawable.sticker_23_chutki,
            R.drawable.sticker_24_gets_better,
            R.drawable.sticker_25_well_done,
            R.drawable.sticker_26_like,
            R.drawable.sticker_27_xoxo,
            R.drawable.sticker_28_dice,
            R.drawable.sticker_29_cat,
            R.drawable.sticker_30_flower
    };

    public static final int CORNER_DP = 5;

    public static GradientDrawable newColorDrawable(Context context, int color, float dp) {
        GradientDrawable colorDrawable = new GradientDrawable();
        colorDrawable.setColor(color);
        colorDrawable.setCornerRadius(Variables.densityToPixel(context, dp));
        return colorDrawable;
    }

    public static GradientDrawable newGradientDrawable(Context context, int[] gradient, Orientation orientation, float dp) {
        GradientDrawable gradientDrawable = new GradientDrawable(orientation, gradient);
        gradientDrawable.setCornerRadius(Variables.densityToPixel(context, dp));
        return gradientDrawable;
    }
}
