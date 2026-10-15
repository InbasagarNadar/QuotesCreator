package com.dartt.yello;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class CrashReproducerTest {

    /**
     * Verifies that ImageSearchActivity.onRetryClick NO LONGER crashes with IndexOutOfBoundsException
     * when searchHistory is empty.
     */
    @Test
    public void testImageSearchRetrySafelyHandlesEmptyHistory() {
        Context context = InstrumentationRegistry.getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ImageSearchActivity activity = new ImageSearchActivity();
            try {
                Field historyField = ImageSearchActivity.class.getDeclaredField("searchHistory");
                historyField.setAccessible(true);
                ArrayList<String> history = (ArrayList<String>) historyField.get(activity);
                if (history != null) {
                    history.clear();
                }

                // Should not crash even when history is empty
                activity.onRetryClick(new View(context));
            } catch (Exception e) {
                Assert.fail("onRetryClick threw unexpected exception: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that ServerImagesActivity.onBackPressed NO LONGER crashes with NullPointerException
     * when showingImagesPanel is true and request is null.
     */
    @Test
    public void testServerImagesOnBackPressedSafelyHandlesNullRequest() {
        Context context = InstrumentationRegistry.getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ServerImagesActivity activity = new ServerImagesActivity();
            try {
                Field imageRecycler = ServerImagesActivity.class.getDeclaredField("imageRecyclerView");
                imageRecycler.setAccessible(true);
                imageRecycler.set(activity, new RecyclerView(context));

                Field catRecycler = ServerImagesActivity.class.getDeclaredField("categoryRecyclerView");
                catRecycler.setAccessible(true);
                catRecycler.set(activity, new RecyclerView(context));

                Field showingImagesField = ServerImagesActivity.class.getDeclaredField("showingImagesPanel");
                showingImagesField.setAccessible(true);
                showingImagesField.setBoolean(activity, true);

                Field requestField = ServerImagesActivity.class.getDeclaredField("request");
                requestField.setAccessible(true);
                requestField.set(activity, null);

                // Should not crash even when request is null
                activity.onBackPressed();
            } catch (Exception e) {
                Assert.fail("onBackPressed threw unexpected exception: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that ImageViewContainer.setDrawable NO LONGER crashes with ClassCastException
     * when the drawable is not a BitmapDrawable (e.g. ColorDrawable or GradientDrawable).
     */
    @Test
    public void testViewContainerSetDrawableHandlesNonBitmapDrawables() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            try {
                EditorFragment fragment = new EditorFragment();
                ImageViewContainer container = new ImageViewContainer(fragment);

                // Test 1: ColorDrawable
                Drawable colorDrawable = new ColorDrawable(Color.RED);
                container.setDrawable(colorDrawable);
                Assert.assertNotNull("Bitmap should be created for ColorDrawable", container.imageBitmap);

                // Test 2: GradientDrawable
                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{Color.RED, Color.BLUE}
                );
                container.setDrawable(gradientDrawable);
                Assert.assertNotNull("Bitmap should be created for GradientDrawable", container.imageBitmap);
            } catch (Exception e) {
                Assert.fail("setDrawable threw unexpected exception: " + e.getMessage());
            }
        });
    }
}
