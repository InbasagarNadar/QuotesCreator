package com.dartt.yello;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.google.android.gms.ads.rewarded.RewardedAdCallback;

@SuppressLint("ClickableViewAccessibility")
public class CreateBackgroundActivity extends AppCompatActivity {

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    DrawerLayout drawerLayout;
    Background Background;

    FrameLayout canvasFrameLayout;

    EditorFragment editorFragment;
    TextEditorFragment textEditorFragment;
    StickerEditorFragment stickerEditorFragment;

    FunctionParameter canvasLayoutChangeAction;
    boolean init = false;

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    RewardedAd supportUsAd;
    AdView bannerAdView;

    FirebaseAnalytics mFirebaseAnalytics;

    ImageButton btnUndo;
    ImageButton btnRedo;

    final Undo undo = new Undo() {
        @Override
        void changeButtonState(boolean undoEnabled, boolean redoEnabled) {
            btnUndo.setEnabled(undoEnabled);
            btnRedo.setEnabled(redoEnabled);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_background);
        Background = new Background();
        initFragments();
        initViews();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            boolean adsEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ad_enabled", true);
            mFirebaseAnalytics.setUserProperty("ads_enabled", String.valueOf(adsEnabled ? 1 : 0));
            Log.d("Analytics", "ads_enabled:" + String.valueOf(adsEnabled ? 1 : 0));
            if (adsEnabled) {
                bannerAdView.setVisibility(View.VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().build();
                bannerAdView.loadAd(adRequest);
                //supportUsAd = createAndLoadRewardedAd(getString(R.string.ad_unit_support_us));
            }
            else {
                bannerAdView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.d("Ads", "Something went wrong while loading ads");
        }
    }

    void initFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        editorFragment = (EditorFragment) fragmentManager.findFragmentById(R.id.frame_editor_fragment);
        textEditorFragment = (TextEditorFragment) fragmentManager.findFragmentById(R.id.text_editor_fragment);
        stickerEditorFragment = (StickerEditorFragment) fragmentManager.findFragmentById(R.id.sticker_editor_fragment);
    }

    void initViews() {
        canvasFrameLayout = findViewById(R.id.canvas);
        btnUndo = findViewById(R.id.btn_undo);
        btnRedo = findViewById(R.id.btn_redo);
        btnUndo.setEnabled(false);
        btnRedo.setEnabled(false);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(4);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        bannerAdView = findViewById(R.id.adView);
    }

    public void setEditorBackground(int resourceId) {
        findViewById(R.id.button_filter).setVisibility(View.VISIBLE);
        editorFragment.setBackground(resourceId);
    }

    public void setEditorBackground(String url) {
        findViewById(R.id.button_filter).setVisibility(View.VISIBLE);
        editorFragment.setBackground(url);
    }

    public void setEditorBackground(Uri imageUri) {
        findViewById(R.id.button_filter).setVisibility(View.VISIBLE);
        editorFragment.setBackground(imageUri);
    }

    public void onRemoveBackgroundClick(View view) {
        findViewById(R.id.button_filter).setVisibility(View.GONE);
        editorFragment.removeBackground();
    }

    public void setEditorArtBoardFill(int color) {
        editorFragment.setArtBoardFill(color);
        if (editorFragment.isArtBoardHidden()) {
            Toast.makeText(this, "Zoom out background image to set background color", Toast.LENGTH_LONG).show();
        }
    }

    public void setEditorArtBoardFill(int[] gradient, GradientDrawable.Orientation orientation) {
        editorFragment.setArtBoardFill(gradient, orientation);
        if (editorFragment.isArtBoardHidden()) {
            Toast.makeText(this, "Zoom out background image to set background gradient", Toast.LENGTH_LONG).show();
        }
    }

    public void beforeFilterChange() {
        undo.beforeEdit(editorFragment.backgroundViewContainer);
    }

    public void afterFilterChange() {
        undo.afterEdit(editorFragment.backgroundViewContainer);
    }

    public void setContrastBrightnessSaturation(float contrast, float brightness, float saturation) {
        editorFragment.setContrastBrightnessSaturation(contrast, brightness, saturation);
    }


    ///////////////////////////////////////////////////////////////////////////

    public void onShowStickerSheetClick(View view) {
        Background.setFragment(5);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("sticker_dialog");
        if (prev == null) {
            StickerBottomSheet dialogFragment = new StickerBottomSheet();
            dialogFragment.listener = index -> editorFragment.addElement(Variables.stickerResourceId[index]);
            dialogFragment.show(ft, "sticker_dialog");
        }
    }


    ///////////////////////////////////////////////////////////////////////////


    public void onElementClicked(ViewContainer viewContainer) {
        if (viewContainer instanceof StickerViewContainer) {
            stickerEditorFragment.edit((StickerViewContainer) viewContainer);
        }
        if (viewContainer instanceof TextViewContainer) {
            textEditorFragment.edit((TextViewContainer) viewContainer);
        }
    }

    public void removeElement(ViewContainer viewContainer) {
        undo.beforeEdit(viewContainer);
        editorFragment.removeElement(viewContainer);
        undo.afterEdit(viewContainer);
    }

    //////////////////////////////////////////////////////////////////////////


    public void onAddTextClick(View view) {
        Background.setFragment(4);
        TextViewContainer viewContainer = new TextViewContainer(editorFragment);
        //editorFragment.addElement(viewContainer);
        textEditorFragment.edit(viewContainer);
        //undo.onElementAdded(view);
    }


    //////////////////////////////////////////////////////////////////////////


    public void onUndoClick(View view) {
        undo.onUndoClick();
        updateFragmentStates();
    }

    public void onRedoClick(View view) {
        undo.onRedoClick();
        updateFragmentStates();
    }

    void updateFragmentStates() {
        //Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 3);
        //((FilterFragment) fragment).resetFilter(editorFragment.backgroundViewContainer);
        viewPagerAdapter.filterFragment.resetFilter(editorFragment.backgroundViewContainer);
        viewPagerAdapter.backgroundImageFragment.resetState(editorFragment.backgroundViewContainer);
        findViewById(R.id.button_filter).setVisibility(editorFragment.backgroundViewContainer.currentBackgroundType == ImageViewContainer.BACKGROUND_TYPE_NULL ? View.GONE : View.VISIBLE);
    }

    /////////////////////////////////////////////////////////////////////////


    public void onRatioClick(View view) {
        editorFragment.setRatio(((Button) view).getText().toString());
    }

    public void onShowBackgroundImagesFragmentClick(View view) {
        viewPagerAdapter.backgroundImageFragment.resetState(editorFragment.backgroundViewContainer);
        Background.setFragment(1);
    }

    public void onShowBackgroundColorFragmentClick(View view) {
        Background.setFragment(0);
    }

    public void onShowRatioFragmentClick(View view) {
        Background.setFragment(2);
    }

    public void onShowFilterFragmentClick(View view) {
        Background.setFragment(3);
    }

    /*
    public RewardedAd createAndLoadRewardedAd(String adUnitId) {
        RewardedAd rewardedAd = new RewardedAd(this, adUnitId);
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                Log.e("SupportAd", "Support Us Loaded");
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                Log.e("SupportAd", "Support Us Ad failed to load. " + adError.toString());
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }
    */

    public void onHamClick(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (textEditorFragment.isVisible()) {
            textEditorFragment.commit();
            return;
        }
        if (stickerEditorFragment.isVisible()) {
            stickerEditorFragment.commit();
            stickerEditorFragment.hide();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Save changes to image before quitting?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> saveDraftAsync(this::finish))
                .setNeutralButton("Cancel", (dialog, id) -> dialog.cancel())
                .setNegativeButton("No", (dialog, id) -> finish());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onSaveClick(View view) {
        saveDraftAsync(null);
    }

    void saveDraftAsync(FunctionParameter callBack) {
        ProgressDialog p = new ProgressDialog(this);
        p.setTitle("Saving. Please wait...");
        p.setCancelable(false);
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.show();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            saveDraft();
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    try {
                        p.dismiss();
                    } catch (Exception ignored) {}
                }
                if (callBack != null) {
                    callBack.execute();
                }
            });
        });
    }

    void saveDraft() {
        try {
            //TODO bring last modified to top/ based on name/ something on the similar lines
            final JSONObject editableJSON = editorFragment.toJSON();
            final String draftKey = editorFragment.draftKey;
            final File directory = new File(getFilesDir() + "/" + draftKey);
            directory.mkdir();
            final String backgroundFileName = directory + "/background.png";
            if (editorFragment.backgroundViewContainer.currentBackgroundType != ImageViewContainer.BACKGROUND_TYPE_NULL) {
                Drawable drawable = editorFragment.backgroundViewContainer.getView().getDrawable();
                Bitmap backgroundBitmap = null;
                if (drawable instanceof BitmapDrawable) {
                    backgroundBitmap = ((BitmapDrawable) drawable).getBitmap();
                } else if (drawable != null) {
                    int w = Math.max(drawable.getIntrinsicWidth(), 1);
                    int h = Math.max(drawable.getIntrinsicHeight(), 1);
                    backgroundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(backgroundBitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                }

                if (backgroundBitmap != null) {
                    saveBitmapToLocation(backgroundBitmap, backgroundFileName);
                    //overwriting background type
                    editableJSON.getJSONObject("background").put("type", ImageViewContainer.BACKGROUND_TYPE_URL);
                    editableJSON.getJSONObject("background").put("background", backgroundFileName);
                }
            }
            else {
                File backgroundImageFile = new File(backgroundFileName);
                if (backgroundImageFile.exists()) {
                    backgroundImageFile.delete();
                }
            }
            final String thumbFileName = directory + "/thumb.png";
            saveBitmapToLocation(editorFragment.getBitmap(), thumbFileName);
            JSONObject draftJSON = new JSONObject();
            draftJSON.put("thumbnail", thumbFileName);
            draftJSON.put("last_modified", System.currentTimeMillis());
            draftJSON.put("name", new java.util.Date(System.currentTimeMillis()));
            draftJSON.put("editable_json", editableJSON); // Maybe toString()?
            SharedPreferences sharedPref = getSharedPreferences("drafts", Context.MODE_PRIVATE);
            JSONArray draftKeys = new JSONArray(sharedPref.getString("draft_keys", "[]"));
            for (int i = 0; i < draftKeys.length(); i++) {
                if (draftKeys.get(i).equals(draftKey)) {
                    draftKeys.remove(i);
                    String oldDraftJson = sharedPref.getString(draftKey, "{}");
                    draftJSON.put("name", new JSONObject(oldDraftJson).getString("name"));
                }
            }
            Utilities.addToPos(0, draftKey, draftKeys);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("draft_keys", draftKeys.toString());
            editor.putString(draftKey, draftJSON.toString());
            editor.apply();
            Log.d("Export", draftJSON.toString());

        } catch (JSONException e) {
            Log.e("onSaveClick - JSON", e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.e("onSaveClick - File", e.getMessage());
            e.printStackTrace();
        }
    }

    public void onExportClick(View view) {
        ProgressDialog p = new ProgressDialog(this);
        p.setTitle("Exporting. Please wait...");
        p.setCancelable(false);
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.show();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            try {
                saveBitmapToLocation(editorFragment.getBitmap(), getFilesDir() + "/export.png");
                saveDraft();
                runOnUiThread(() -> {
                    Intent intent = new Intent(CreateBackgroundActivity.this, SaveActivity.class);
                    startActivity(intent);
                    p.dismiss();
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(CreateBackgroundActivity.this, "Export fail. Please contact developer", Toast.LENGTH_LONG).show();
                    p.dismiss();
                });
            }
        });
    }

    private void saveBitmapToLocation(Bitmap bitmap, String location) throws FileNotFoundException {
        try (FileOutputStream out = new FileOutputStream(location)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class Background {
        public final FrameLayout canvas = findViewById(R.id.canvas);

        public final ImageButton[] menuButtons = new ImageButton[]{
                findViewById(R.id.button_canvas_fill),
                findViewById(R.id.button_canvas_image),
                findViewById(R.id.button_canvas_ratio),
                findViewById(R.id.button_filter),
                findViewById(R.id.button_add_text),
                findViewById(R.id.button_add_sticker)
        };


        Background() {
            final ViewTreeObserver viewTreeObserver = canvas.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(() -> {
                if (canvasLayoutChangeAction != null) {
                    canvasLayoutChangeAction.execute();
                }
                if (init) {
                    return;
                }
                init = true;
                View fixedParent = findViewById(R.id.fixedParent);
                fixedParent.getLayoutParams().width = fixedParent.getWidth();
                fixedParent.getLayoutParams().height = fixedParent.getHeight();
            });
        }

        void setFragment(int index) {
            int accentColor = getResources().getColor(R.color.colorAccent);
            for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
                if (i == index) {
                    menuButtons[i].setColorFilter(accentColor);
                }
                else {
                    menuButtons[i].setColorFilter(0xFF000000);
                }
            }
            if (index < viewPager.getAdapter().getCount()) {
                if (index == viewPager.getCurrentItem() && viewPager.getVisibility() != View.INVISIBLE) {
                    viewPager.setVisibility(View.INVISIBLE);
                    menuButtons[index].setColorFilter(0xFF000000);
                }
                else {
                    viewPager.setCurrentItem(index, viewPager.getVisibility() == View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                }
            }
            else {
                viewPager.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void OnNavigationItemClick(View view) {
        int id = view.getId();
        if (id == R.id.button_navigation_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if (id == R.id.button_navigation_fonts) {
            startActivity(new Intent(this, ManageLanguageActivity.class));
        }
        else if (id == R.id.button_navigation_rate_app) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.getPackageName())));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
            }
        }
        else if (id == R.id.button_navigation_contact_us) {
            openMailApp();
        }
        else if (id == R.id.button_navigation_support_us) {
            showSupportUsAdDialog();
        }
        else if (id == R.id.button_navigation_facebook) {
            Toast.makeText(this, "Facebook page coming soon.", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.button_navigation_get_featured || id == R.id.button_navigation_instagram) {
            showInstagramPage();
        }
        else if (id == R.id.button_faq) {
            startActivity(new Intent(this, FaqActivity.class));
        }
    }

    void openMailApp() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Yello.Darttdevelopers@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hello Yello, ");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
        else {
            Toast.makeText(this, "Send an email to\nYello.DarttDevelopers@gmail.com\nto get in touch with us. ", Toast.LENGTH_SHORT).show();
        }
    }

    void showInstagramPage() {
        Uri uri = Uri.parse("http://instagram.com/_u/yelloquotes");
        Intent instagramIntent = new Intent(Intent.ACTION_VIEW, uri);
        instagramIntent.setPackage("com.instagram.android");
        try {
            startActivity(instagramIntent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/yelloquotes")));
        }
    }

    void showSupportUsAdDialog() {
        /*if (!supportUsAd.isLoaded()) {
            //supportUsAd = createAndLoadRewardedAd(getString(R.string.ad_unit_support_us));
        }*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We'll now show an ad which will help us generate revenue.")
                .setCancelable(false)
                .setPositiveButton("Okay", (dialog, id) -> {
                    Toast.makeText(CreateBackgroundActivity.this, "Not Implemented", Toast.LENGTH_LONG).show();
                    /*if (supportUsAd.isLoaded()) {
                        supportUsAd.show(CreateBackgroundActivity.this, new RewardedAdCallback() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                Toast.makeText(CreateBackgroundActivity.this, "Thanks for supporting us :D", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onRewardedAdClosed() {
                                supportUsAd = createAndLoadRewardedAd(getResources().getString(R.string.ad_unit_support_us));
                            }
                        });
                    }
                    else {
                        Toast.makeText(CreateBackgroundActivity.this, "Failed to Load Ad. Please Try again later.", Toast.LENGTH_SHORT).show();
                        if (!supportUsAd.isLoaded()) {
                            // supportUsAd = createAndLoadRewardedAd(getString(R.string.ad_unit_support_us));
                        }
                    }*/
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        public BackgroundImageFragment backgroundImageFragment;
        public BackgroundColorFragment backgroundFillFragment;
        public RatioFragment ratioFragment;
        public FilterFragment filterFragment;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            backgroundFillFragment = (BackgroundColorFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 0);
            if (backgroundFillFragment == null) {
                backgroundFillFragment = new BackgroundColorFragment();
            }
            backgroundImageFragment = (BackgroundImageFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 1);
            if (backgroundImageFragment == null) {
                backgroundImageFragment = new BackgroundImageFragment();
            }
            ratioFragment = (RatioFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 2);
            if (ratioFragment == null) {
                ratioFragment = new RatioFragment();
            }
            filterFragment = (FilterFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 3);
            if (filterFragment == null) {
                filterFragment = new FilterFragment();
            }
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (position == 0) {
                fragment = backgroundFillFragment;
            }
            else if (position == 1) {
                fragment = backgroundImageFragment;
            }
            else if (position == 2) {
                fragment = ratioFragment;
            }
            else {
                fragment = filterFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    int getImageRotationFromExif(Uri imageUri) {
        int rotation = 0;
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return rotation;
            }
            ExifInterface exifInterface = new ExifInterface(inputStream);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
            }
        } catch (IOException e) {
            Log.e("ExifException", "Something went wrong");
        }
        return rotation;
    }

    String getID(int resId) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getResources().getResourcePackageName(resId) + '/' + getResources().getResourceTypeName(resId) + '/' + getResources().getResourceEntryName(resId);
    }

}
