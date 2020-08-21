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
    public static Orientation[] GradientsOrientation = {
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

    public static int[] Colors = {
            0xFFFFD3B6,
            0xFF355C7D,
            0xFFFFBF00,
            0xFF6C5B7B,
            0xFFC06C84,
            0xFFF67280,
            0xFFF8B195,
            0xFFE8175D,
            0xFFA8E6CE,
            0xFFDCEDC1,
            0xFF22C5CC,
            0xFFF7E5DE,
            0xFFFF69B4,
            0xFFF4951B
    };
    public static int currentColor = Colors[0];

    public static int[] BackgroundImages = {
            R.drawable.background_0,
            R.drawable.background_1,
            R.drawable.background_2,
            R.drawable.background_3,
            R.drawable.background_4,
            R.drawable.background_5,
            R.drawable.background_6,
            R.drawable.background_6,
            R.drawable.background_6,
            R.drawable.background_6,
    };

    //public static float density = -1;

    public static float pixelToDensity(Context context, int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int densityToPixel(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int[] fontResourceId = {
            R.font.montserrat_light,
            R.font.bungee_regular,
            R.font.grandhotel_regular,
            R.font.merianda_bold,
            R.font.hindi_rozha_one,
            R.font.hindi_rozha_one,
            R.font.hindi_rozha_one,
            R.font.hindi_rozha_one,
            R.font.hindi_rozha_one,
    };
    public static String[] fontResourceName = {
            "Montserrat Light",
            "Bungee Regular",
            "GrandHotel Regular",
            "Merianda Bold",
            "Rozha One",
            "Rozha One",
            "Rozha One",
            "Rozha One",
            "Rozha One",
    };

    public static int[] stickerResourceId = {
            R.drawable.sticker_cat,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_cat,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_cat,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_cat,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_cat,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
            R.drawable.sticker_flower,
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
