package com.radangames.quotescreator;

public class Variables {
    public static int[][] Gradients = {
            {0xFFFDC05B, 0xFFDD2D75, 0xFFA734B4},
            {0xFFFF6E7F, 0xFFBFE9FF},
            {0xFFE44D87, 0xFF5FC3E4},
            {0xFFEF3B36, 0xFFFFFFFF},
            {0XFFc6ffdd, 0xFFfbd786, 0xFFf7797d},
            {0xFFABBAAB, 0xFFFFFFFF},
            {0xFFFFFFFF, 0xFF000000},
    };

    public static int[] Colors = {
            0xFF355C7D,
            0xFF6C5B7B,
            0xFFC06C84,
            0xFFF67280,
            0xFFF8B195,
            0xFFE8175D,
            0xFFA8E6CE
    };
    public static float density = -1;

    public static float pixelToDensity(int px) {
        return px / density;
    }

    public static int densityToPixel(float dp) {
        return (int) (dp * density);
    }

    public static int[] fontResourceId = {
            R.font.montserrat_light,
            R.font.bungee_regular,
            R.font.grandhotel_regular,
            R.font.merianda_bold
    };
    public static String[] fontResourceName = {
            "Montserrat Light",
            "Bungee Regular",
            "GrandHotel Regular",
            "Merianda Bold"
    };
}
